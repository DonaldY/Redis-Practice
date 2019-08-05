package com.donaldy.redispractice.controller;

import com.donaldy.redispractice.dao.RedisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    @Autowired
    RedisDao redisDao;

    @GetMapping("/test")
    public void test(String key, String value) {

        this.redisDao.setString(key, value);
    }
}
