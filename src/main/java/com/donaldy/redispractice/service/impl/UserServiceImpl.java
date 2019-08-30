package com.donaldy.redispractice.service.impl;

import com.donaldy.redispractice.dao.UserDao;
import com.donaldy.redispractice.model.User;
import com.donaldy.redispractice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;


    @Override
    public User getUserInfo(Integer userId) {

        return userDao.selectUserInfoById(userId);
    }
}
