package com.donaldy.redispractice.model;

import lombok.Data;

@Data
public class User {

    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 帐号名
     */
    private String username;

    /**
     * 年龄
     */
    private Integer age;
}
