package com.donaldy.redispractice.controller;

import com.donaldy.redispractice.dao.RedisDao;
import com.donaldy.redispractice.model.User;
import com.donaldy.redispractice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    RedisDao redisDao;

    @Autowired
    private UserService userService;

    @GetMapping("/test")
    public void test(String key, String value) {

        this.redisDao.setString(key, value);
    }

    @GetMapping("/user")
    public User getUserInfoById(@RequestParam Integer userId) {

        return userService.getUserInfo(userId);
    }
}
