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

        test1(lock);

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        lock.unlock();
    }

    private void test1(RLock lock) {

        lock.lock();

        System.out.println("test1 加锁了");

        lock.unlock();
    }

    @Test
    public void test2() {

        RLock lock = redisson.getFairLock("fairLock");

        lock.lock();

        lock.unlock();
    }

    @Test
    public void test3() {

        RLock lock1 = redisson.getLock("lock1");
        RLock lock2 = redisson.getLock("lock2");
        RLock lock3 = redisson.getLock("lock3");

        RLock multiLock = redisson.getMultiLock(lock1, lock2, lock3);

        multiLock.lock();

        multiLock.unlock();
    }

    @Test
    public void test4() {

        RLock lock1 = redisson.getLock("lock1");
        RLock lock2 = redisson.getLock("lock2");
        RLock lock3 = redisson.getLock("lock3");

        RLock redLock = redisson.getRedLock(lock1, lock2, lock3);

        redLock.lock();
        redLock.unlock();
    }
}
