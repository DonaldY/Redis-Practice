package com.donaldy.redispractice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @author donald
 * @date 2021/07/31
 */
public class JedisTest {

    private Jedis jedis;

    @Before
    public void setUp() {

        jedis = new Jedis("127.0.0.1", 6379);
    }

    @After
    public void after() {

        jedis.close();
    }

    @Test
    public void testCache() {

        // 最简单的设置缓存
        jedis.set("key1", "value1");

        System.out.println(jedis.get("key1"));
    }

    @Test
    public void testNx() {


    }
}
