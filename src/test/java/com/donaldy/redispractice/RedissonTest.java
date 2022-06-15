package com.donaldy.redispractice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author donald
 * @date 2022/06/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedissonTest {

    @Autowired
    private RedissonClient redisson;

    @Test
    public void test() {

        RLock lock = redisson.getLock("myLock");
        lock.lock();
        lock.unlock();
    }
}
