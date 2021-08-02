package com.donaldy.redispractice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;
import sun.rmi.runtime.Log;

import java.util.List;

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

        // 最简单的基于 nx 选项实现的分布式锁
        String result = jedis.set("lock_test", "value_test",
                SetParams.setParams().nx());

        System.out.println("第一次加锁的结果：" + result);

        result = jedis.set("lock_test", "value_test",
                SetParams.setParams().nx());

        System.out.println("第二次加锁的结果：" + result);
    }


    @Test
    public void testBlogPush() {

        // 1. 博客的发布、修改与查看
        /*jedis.mset("article:1:title", "redis实战",
                "article:1:content", "jedis使用",
                "article:1:author", "donald",
                "article:1:time", "2021-07-31");*/
        Long publishBlogResult = jedis.msetnx("article:1:title", "redis实战",
                "article:1:content", "jedis使用",
                "article:1:author", "donald",
                "article:1:time", "2021-07-31");
        System.out.println("发布的博客的结果： " + publishBlogResult);

        // 批量获取
        List<String> blog = jedis.mget("article:1:title", "article:1:content",
                "article:1:author", "article:1:time");
        System.out.println("查看博客：" + blog);

        // 批量修改
        String updateBlogResult = jedis.mset("article:1:title", "修改了标题",
                "article:1:content", "修改后的文章内容");
        System.out.println("修改博客的结果：" + updateBlogResult);

        // 2. 统计字数, 注意中英文长度不同
        Long blogLen = jedis.strlen("article:1:content");
        System.out.println("博客的长度统计：" + blog);

        // 3. 文章预览
        String blogContentPreview = jedis.getrange("article:1:content", 0, 5);
        System.out.println("博客内容预览: " + blogContentPreview);
    }

    @Test
    public void testUserLog() {

        // 操作日志的审计功能
        jedis.setnx("operation_log_2021_07_31", "");

        for (int i = 0; i < 10; ++i) {
            jedis.append("operation_log_2021_07_31", "今天的第" + (i + 1) + "操作日志\n");
        }

        String operationLog = jedis.get("operation_log_2021_07_31");
        System.out.println("今天所有的操作日志: \n" + operationLog);
    }

    @Test
    public void testGenerateId() {

        // 唯一ID生成器
        for (int i = 0; i < 10; ++i) {
            Long orderId = jedis.incr("order_id_counter");
            System.out.println("生成的第" + (i + 1) + "个唯一ID" + orderId);
        }
    }

    @Test
    public void testBlogLike() {

        // 博客的点赞计数器
        for (int i = 0; i < 10; ++i) {
            jedis.incr("article:1:like");
        }
        Long likeCounter = Long.valueOf(jedis.get("article:1:like"));
        System.out.println("博客的点赞次数为：" + likeCounter);

        jedis.decr("article:1:like");
        likeCounter = Long.valueOf(jedis.get("article:1:like"));
        System.out.println("再次查看博客的点赞次数为：" + likeCounter);
    }

    private static final String X36 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String[] X36_ARRAY = "0,1,2,3,4,5,6,7,8,9,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z".split(",");

    /**
     * 获取短链接地址
     * @param url 原地址
     * @return 短链接
     */
    private String getShortUrl(String url) {
        long shortUrlSeed = jedis.incr("short_url_seed");
        StringBuffer buffer = new StringBuffer();
        while (shortUrlSeed > 0) {
            buffer.append(X36_ARRAY[(int)(shortUrlSeed % 36)]);
            shortUrlSeed = shortUrlSeed / 36;
        }

        String shortUrl = buffer.reverse().toString();
        jedis.hset("short_url_access_count", shortUrl, "0");
        jedis.hset("short_url_mapping", shortUrl, url);

        return shortUrl;
    }

    /**
     * 短链接访问次数增长
     * @param shortUrl 短链接
     */
    private void incrShortUrlAccessCount(String shortUrl) {
        jedis.hincrBy("short_url_access_count", shortUrl, 1);
    }

    private long getShortUrlAccessCount(String shortUrl) {
        return Long.valueOf(jedis.hget("short_url_access_count", shortUrl));
    }

    @Test
    public void shotUrl() {
        String shortUrl = getShortUrl("http://redis.com/index.html");
        System.out.println("页面上展示的短链接地址为：" + shortUrl);

        for (int i = 1; i < 155; ++i) {
            incrShortUrlAccessCount(shortUrl);
        }

        long accessCng = getShortUrlAccessCount(shortUrl);
        System.out.println("短连接被访问的次数为：" + accessCng);
    }

    @Test
    public void testSecKill() {

        for (int i = 0; i < 10; ++i) {

            enqueueSecKill("第" + (i + 1) + "个秒杀请求");
        }

        while (true) {
            String secKillRequest = dequeueSecKill();

            if (secKillRequest == null || "null".equals(secKillRequest)) {
                return;
            }
            System.out.println(secKillRequest);
        }
    }

    private void enqueueSecKill(String request) {

        // 秒杀抢购请求入队
        jedis.lpush("sec_kill_request_queue", request);
    }

    private String dequeueSecKill() {
        // 秒杀抢购出队列
        return jedis.rpop("sec_kill_request_queue");
    }
}
