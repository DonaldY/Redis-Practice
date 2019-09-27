package com.donaldy.redispractice.controller;

import com.donaldy.redispractice.dao.RedisDao;
import com.donaldy.redispractice.model.User;
import com.donaldy.redispractice.service.UserService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class UserController {

    @Autowired
    RedisDao redisDao;

    @Autowired
    private UserService userService;

    @Autowired
    private Redisson redisson;

    @GetMapping("/test")
    public void test(String key, String value) {

        this.redisDao.setString(key, value);
    }

    @GetMapping("/lock")
    public void lock(String key, String value) {

        RLock rLock = redisson.getLock("");

        rLock.lock(30, TimeUnit.SECONDS);

        rLock.unlock();
    }

    @GetMapping("/user")
    public User getUserInfoById(@RequestParam Integer userId) {

        return userService.getUserInfo(userId);
    }
}
