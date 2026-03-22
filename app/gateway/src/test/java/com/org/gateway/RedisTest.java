package com.org.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void testSamples(){
        redisTemplate.opsForValue().set("hello","world");
       Object o =  redisTemplate.opsForValue().get("hello");

    }
}
