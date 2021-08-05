package com.donaldy.redispractice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ListPosition;
import redis.clients.jedis.params.SetParams;
import sun.rmi.runtime.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 发表一篇博客
     * @param id 文章Id
     * @param blog 文章信息
     * @return 是否发布
     */
    private boolean publishBlog(long id, Map<String, String> blog) {
        if(jedis.hexists("article::" + id, "title")) {
            return false;
        }
        blog.put("content_length", String.valueOf(blog.get("content").length()));

        jedis.hmset("article::" + id, blog);

        return true;
    }

    /**
     * 发表一篇博客
     * @param id 文章Id
     * @param blog 文章信息
     * @param tags 标签
     * @return 是否发布
     */
    public boolean publishBlog(long id, Map<String, String> blog, String[] tags) {
        if(jedis.hexists("article::" + id, "title")) {
            return false;
        }
        blog.put("content_length", String.valueOf(blog.get("content").length()));

        jedis.hmset("article::" + id, blog);
        jedis.lpush("blog_list", String.valueOf(id));
        jedis.sadd("article::" + id + "::tags", tags);

        return true;
    }

    /**
     * 查看一篇博客
     * @param id 文章Id
     * @return 文章信息
     */
    private Map<String, String> findBlogById(long id) {
        Map<String, String> blog = jedis.hgetAll("article::" + id);
        incrementBlogViewCount(id);
        return blog;
    }

    /**
     * 更新一篇博客
     * @param id 文章Id
     * @param updatedBlog 文章
     */
    private void updateBlog(long id, Map<String, String> updatedBlog) {
        String updatedContent = updatedBlog.get("content");
        if(updatedContent != null && !"".equals(updatedContent)) {
            updatedBlog.put("content_length", String.valueOf(updatedContent.length()));
        }

        jedis.hmset("article::" + id, updatedBlog);
    }

    /**
     * 对博客进行点赞
     * @param id 文章Id
     */
    private void incrementBlogLikeCount(long id) {
        jedis.hincrBy("article::" + id, "like_count", 1);
    }

    /**
     * 增加博客浏览次数
     * @param id 文章Id
     */
    private void incrementBlogViewCount(long id) {
        jedis.hincrBy("article::" + id, "view_count", 1);
    }

    @Test
    public void testBlogHash() {
        Map<String, String> blog = new HashMap<>();
        blog.put("id", String.valueOf(1000));
        blog.put("title", "我喜欢学习Redis");
        blog.put("content", "学习Redis是一件特别快乐的事情");
        blog.put("author", "donald");
        blog.put("time", "2020-01-01 10:00:00");

        publishBlog(1000, blog);

        // 更新一篇博客
        Map<String, String> updatedBlog = new HashMap<>();
        updatedBlog.put("title", "我特别的喜欢学习Redis");
        updatedBlog.put("content", "我平时喜欢到官方网站上去学习Redis");

        updateBlog(1000, updatedBlog);

        // 有别人点击进去查看你的博客的详细内容，并且进行点赞
        Map<String, String> blogResult = findBlogById(1000);
        System.out.println("查看博客的详细内容：" + blogResult);
        incrementBlogLikeCount(1000);

        // 你自己去查看自己的博客，看看浏览次数和点赞次数
        blogResult = findBlogById(1000);
        System.out.println("自己查看博客的详细内容：" + blogResult);
    }

    /**
     * 分页查询博客
     * @param pageNo 当前页
     * @param pageSize 页大小
     * @return 博客列表
     */
    private List<String> findBlogByPage(int pageNo, int pageSize) {
        int startIndex = (pageNo - 1) * pageSize;
        int endIndex = pageNo * pageSize - 1;
        return jedis.lrange("blog_list", startIndex, endIndex);
    }

    @Test
    public void testBlogList() {

        int id = 1001;

        // 构造20篇博客数据
        for(int i = 0; i < 20; i++) {

            id += i;

            Map<String, String> map = new HashMap<>();
            map.put("id", String.valueOf(id));
            map.put("title", "第" + (i + 1) + "篇博客");
            map.put("content", "学习第" + (i + 1) + "篇博客，是一件很有意思的事情");
            map.put("author", "donald");
            map.put("time", "2020-01-01 10:00:00");

            publishBlog(id, map);
        }

        // 有人分页浏览所有的博客，先浏览第一页
        int pageNo = 1;
        int pageSize = 10;

        List<String> blogPage = findBlogByPage(pageNo, pageSize);
        System.out.println("展示第一页的博客......");
        for(String blogId : blogPage) {
            Map<String, String>  map = findBlogById(Long.valueOf(blogId));
            System.out.println(map.toString());
        }

        pageNo = 2;

        blogPage = findBlogByPage(pageNo, pageSize);
        System.out.println("展示第二页的博客......");
        for(String blogId : blogPage) {
            Map<String, String>  map = findBlogById(Long.valueOf(blogId));
            System.out.println(map.toString());
        }
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


    // =============== OA ==================
    /**
     * 添加待办事项
     * @param todoEvent 代办事项
     */
    public void addTodoEvent(long userId, String todoEvent) {
        jedis.lpush("todo_event::" + userId, todoEvent);
    }

    /**
     * 分页查询待办事项列表
     * @param userId 用户Id
     * @param pageNo 页号
     * @param pageSize 页大小
     * @return 列表
     */
    public List<String> findTodoEventByPage(long userId, int pageNo, int pageSize) {
        int startIndex = (pageNo - 1) * pageSize;
        int endIndex = pageNo * pageSize - 1;
        return jedis.lrange("todo_event::" + userId, startIndex, endIndex);
    }

    /**
     * 插入待办事项
     * @param userId 用户Id
     * @param position 位置
     * @param targetTodoEvent 目标事项
     * @param todoEvent 待办事项
     */
    public void insertTodoEvent(long userId,
                                ListPosition position,
                                String targetTodoEvent,
                                String todoEvent) {
        jedis.linsert("todo_event::" + userId, position, targetTodoEvent, todoEvent);
    }

    /**
     * 修改一个待办事项
     * @param userId 用户Id
     * @param index 位置
     * @param updatedTodoEvent 更新事项
     */
    public void updateTodoEvent(long userId, int index, String updatedTodoEvent) {
        jedis.lset("todo_event::" + userId, index, updatedTodoEvent);
    }

    /**
     * 完成一个待办事项
     * @param userId 用户Id
     * @param todoEvent 待办事项
     */
    public void finishTodoEvent(long userId, String todoEvent) {
        jedis.lrem("todo_event::" + userId, 0, todoEvent);
    }
}
