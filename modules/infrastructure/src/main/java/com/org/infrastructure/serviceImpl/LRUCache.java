package com.org.infrastructure.serviceImpl;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;


public class LRUCache<K, V> {
    private final int capacity;
    private final int k;
    private final Map<K, Entry<V>> map;


    public LRUCache(int capacity, int k) {
        this.capacity = capacity;
        if (k < 0) {
            throw new IllegalArgumentException("k < 0");
        }
        this.k = k;
        this.map = new HashMap<>(capacity);
    }

    public void put(K key, V value) {
        Entry<V> e = map.get(key);

        if (e != null) {
            e.value = value;

            recordAccess(e);
            return;
        }
        if (map.size() > capacity) {
            evict();
        }
        Entry<V> entry = new Entry<>(value, k);
        recordAccess(entry);
        map.put(key, entry);
    }

    private void evict() {
        K coldKey = null; /// create a gc-enabled object to be referenced later

        long cumulativeAccessTime = Long.MAX_VALUE;

        for (Map.Entry<K, Entry<V>> entry : map.entrySet()) {

            Entry<V> e = entry.getValue();

            long objectTimeProcess;

            if (e.accessTime.size() < k) {
                objectTimeProcess = Long.MIN_VALUE;
            } else {
                objectTimeProcess = e.accessTime.peekFirst();
            }

            if (objectTimeProcess < cumulativeAccessTime) {
                cumulativeAccessTime = objectTimeProcess;
                coldKey = entry.getKey();
            }
        }
        if (coldKey != null) {

            map.remove(coldKey);

        }
    }

    private void recordAccess(Entry<V> e) {
        long currSec = System.nanoTime();
        e.accessTime.addLast(currSec);

        if (e.accessTime.size() > k) {
            e.accessTime.removeFirst(); /// DQ-FIFO
        }
    }

    @Getter
    static class Entry<V> implements Serializable {
        Deque<Long> accessTime;
        V value;

        Entry(V value, int k) {
            this.accessTime = new ArrayDeque<Long>(k);
            this.value = value;
        }
    }


}
