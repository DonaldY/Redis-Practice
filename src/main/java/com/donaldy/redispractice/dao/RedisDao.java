package com.donaldy.redispractice.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class RedisDao {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedisTemplate<Object, Object> redisTemplate;

    public void setString(String k, String v) {

        stringRedisTemplate.opsForValue().set(k, v);
    }


    public String get(String key) {

        return stringRedisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {

        stringRedisTemplate.delete(key);
    }
}
