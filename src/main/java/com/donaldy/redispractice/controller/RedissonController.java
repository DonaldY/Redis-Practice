package com.donaldy.redispractice.controller;

import com.donaldy.redispractice.dao.RedisDao;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/redisson")
public class RedissonController {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisDao redisDao;

    @RequestMapping(value = "/sku/init")
    public String initSku() {

        redisDao.setString("product_sku_yy", "4000");

        return "初始化库存成功";
    }

    @RequestMapping("/sku/reduce")
    public void reduce() {
        RLock lock = redissonClient.getLock("product_sku");
        boolean locked = false;
        try {
            lock.lock( 10, TimeUnit.SECONDS);
            // locked = lock.tryLock(1, 10, TimeUnit.SECONDS);

            /*if (locked) {

                System.out.println("获取锁成功。。。");
            } else {

                System.out.println("获取锁失败。。。");
            }*/

            // TODO: handle NULL
            int sku = Integer.parseInt(redisDao.get("product_sku_yy"));

            if (--sku < 0) {
                System.out.println("库存不足。。。");
                return;
            }

            redisDao.setString("product_sku_yy", Integer.toString(sku));

            System.out.println("减库存成功： " + sku);

        } finally {
            /*if (!locked) {
                System.out.println("获取锁失败。。。");
            }*/
            lock.unlock();
        }
    }
}
