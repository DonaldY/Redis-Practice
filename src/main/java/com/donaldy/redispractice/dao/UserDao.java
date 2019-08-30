package com.donaldy.redispractice.dao;

import com.donaldy.redispractice.model.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao {

    /**
     * 根据用户ID，获取用户信息
     * @param userId 用户ID
     * @return       用户信息
     */
    User selectUserInfoById(@Param("userId") Integer userId);
}
