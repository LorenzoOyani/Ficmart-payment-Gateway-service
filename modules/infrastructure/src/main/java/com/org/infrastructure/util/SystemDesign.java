package com.org.infrastructure.util;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class SystemDesign {

    /// Rate-limiting...
    static class TokenBucket {
        private final double fillRate;
        private final long capacity;
        private Instant lastFillRate;
        private double tokens;

        TokenBucket(double fillRate, long capacity, Instant lastFillRate, double tokens) {
            this.fillRate = fillRate;
            this.capacity = capacity;
            this.lastFillRate = lastFillRate;
            this.tokens = tokens;
        }

        public boolean allowedTokens(int tokens) {
            ///  first fill the bucket with token at a constant system elapsed time;
            ///  not enough tokens return false;
            ///  then consume token for each request

            refillTokenBucket();


            if (this.tokens < tokens) {
                return false; /// not enough tokens.
            }
            this.tokens -= tokens;
            return true;
        }

        private void refillTokenBucket() {
            Instant now = Instant.now();
            double refillBucketTokens = ((now.toEpochMilli() - lastFillRate.toEpochMilli()) * fillRate / 1000.0);
            this.tokens = Math.min(capacity, tokens + refillBucketTokens);
            this.lastFillRate = now;
        }

    }

    static class FixedWindowCounter {
        private final long windowsSizeInSeconds;
        private final long max_request;
        private long windowStartingCount;
        private int requestCount;


        FixedWindowCounter(long windowsSizeInSeconds, long maxRequest) {
            this.windowsSizeInSeconds = windowsSizeInSeconds;
            max_request = maxRequest;
            this.windowStartingCount = Instant.now().toEpochMilli();
            this.requestCount = 0;
        }

        public synchronized boolean allowedRequests() {

            long now = Instant.now().toEpochMilli();


            if (now - windowStartingCount >= windowsSizeInSeconds) {
                windowStartingCount = now;
                this.requestCount = 0;

            }
            ///  the request count should not be more than the maxRequest count not to break the window
            if (this.requestCount < max_request) {
                requestCount++;
                return true;
            }

            return false;
        }
    }

    static class LeakyBuckets {
        private final int capacity;
        private final Queue<Instant> timestampBucket;
        /// queue holding timestamps for bucket
        private Instant lastLeakedBucket;
        private final double leakRate;

        public LeakyBuckets(int capacity, double leakRate) {
            this.leakRate = leakRate;
            this.capacity = capacity;
            this.timestampBucket = new LinkedList<>();
            this.lastLeakedBucket = Instant.now();
        }

        public synchronized boolean allowedTimestampsRequests() {
            leak();

            if (timestampBucket.size() > capacity) {
                return false;
            } else {
                timestampBucket.offer(Instant.now());
                return true;
            }

        }

        private void leak() {
            Instant now = Instant.now();

            long elapsedLeakedTime = now.toEpochMilli() - lastLeakedBucket.toEpochMilli();
            double leakedItems = elapsedLeakedTime * leakRate / 1000.0;

            for (int i = 0; i < leakedItems && !timestampBucket.isEmpty(); i++) {
                timestampBucket.poll();
            }
            lastLeakedBucket = now;
        }
    }

    static class RoundRobin{

        private final List<String> server;

        private final AtomicInteger serverList;

        public  RoundRobin(List<String> server) {
            this.server = server;
            this.serverList = new AtomicInteger(-1);
        }

        private String getNextServer() {
            int current = serverList.getAndIncrement() % server.size();

            return server.get(current);
        }




        public static void main(String[] args){
            List<String> server = List.of("server1", "server2");
            List<Integer> weights = List.of(1, 2, 3);

            RoundRobin rb = new RoundRobin(server);
            WeightedRobins wb = new WeightedRobins(weights, server);

            System.out.printf("weighted Servers: %s ", wb.getNextServer());

            for (String s : server) {
                System.out.println("get server with name {} : "+ rb.getNextServer());
            }
        }

    }

    private static class WeightedRobins {
        private final List<Integer> weights;
        private final List<String> server;
        private int currenIndex;
        private int currentWeight;

        private WeightedRobins(List<Integer> weights, List<String> server) {
            this.weights = weights;
            this.server = server;
            this.currenIndex = -1;
            this.currentWeight = 0;
        }

        public String getNextServer() {
            while (true){
                currenIndex = (currenIndex + 1) % server.size();
                if(currenIndex == 0){
                    currentWeight--;
                    if(currentWeight <= 0){
                        currentWeight = getWeight();
                    }

                    if(this.currenIndex > currentWeight){
                        return this.server.get(currenIndex);
                    }
                }
            }
        }

        private int getWeight() {
            return weights.stream().max(Integer::compareTo).orElse(0);
        }
    }


    public void withMultiValueMap(){
        MultiValueMap<String, String> userTags = new LinkedMultiValueMap<>();

    }
}
