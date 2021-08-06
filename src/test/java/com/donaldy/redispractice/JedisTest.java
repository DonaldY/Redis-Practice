package com.donaldy.redispractice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ListPosition;
import redis.clients.jedis.params.SetParams;
import sun.rmi.runtime.Log;

import java.text.SimpleDateFormat;
import java.util.*;

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
    private void addTodoEvent(long userId, String todoEvent) {
        // 从左边添加
        jedis.lpush("todo_event::" + userId, todoEvent);
    }

    /**
     * 分页查询待办事项列表
     * @param userId 用户Id
     * @param pageNo 页号
     * @param pageSize 页大小
     * @return 列表
     */
    private List<String> findTodoEventByPage(long userId, int pageNo, int pageSize) {
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
    private void insertTodoEvent(long userId,
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
    private void updateTodoEvent(long userId, int index, String updatedTodoEvent) {
        jedis.lset("todo_event::" + userId, index, updatedTodoEvent);
    }

    /**
     * 完成一个待办事项
     * @param userId 用户Id
     * @param todoEvent 待办事项
     */
    private void finishTodoEvent(long userId, String todoEvent) {
        // count等于0的话，就不分往前还是往后后，直接指定的value(元素)都删
        jedis.lrem("todo_event::" + userId, 0, todoEvent);
    }

    @Test
    public void testTodoEvent() {
        // 0. 添加20个待办事项
        long userId = 2;
        for(int i = 0; i < 20; i++) {
            addTodoEvent(userId, "第" + (i + 1) + "个待办事项");
        }

        // 1. 查询第一页待办事项
        int pageNo = 1;
        int pageSize = 10;
        List<String> todoEventPage = findTodoEventByPage(userId, pageNo, pageSize);

        System.out.println("第一次查询第一页待办事项......");
        for(String todoEvent :todoEventPage) {
            System.out.println(todoEvent);
        }

        // 2.1 插入一个待办事项
        Random random = new Random();
        int index = random.nextInt(todoEventPage.size());
        String targetTodoEvent = todoEventPage.get(index);

        insertTodoEvent(userId, ListPosition.BEFORE,
                targetTodoEvent, "插入的待办事项");
        System.out.println("在" + targetTodoEvent + "前面插入了一个待办事项");

        // 2.2重新分页查询第一页待办事项
        todoEventPage = findTodoEventByPage(
                userId, pageNo, pageSize);

        System.out.println("第二次查询第一页待办事项......");
        for(String todoEvent :todoEventPage) {
            System.out.println(todoEvent);
        }

        // 3.修改一个待办事项
        index = random.nextInt(todoEventPage.size());
        updateTodoEvent(userId, index, "修改后的待办事项");

        // 4.完成一个待办事项
        finishTodoEvent(userId, todoEventPage.get(0));

        // 最后查询一次待办事项
        todoEventPage = findTodoEventByPage(
                userId, pageNo, pageSize);

        System.out.println("第三次查询第一页待办事项......");
        for(String todoEvent :todoEventPage) {
            System.out.println(todoEvent);
        }
    }

    // ===================== 发送邮箱 =====================

    /**
     * 让发送邮件任务入队列
     * @param sendMailTask 任务
     */
    private void enqueueSendMailTask(String sendMailTask) {
        jedis.lpush("send_mail_task_queue", sendMailTask);
    }

    /**
     * 阻塞式获取发送邮件任务
     * @return 队列
     */
    private List<String> takeSendMailTask() {
        return jedis.brpop(5, "send_mail_task_queue");
    }

    @Test
    public void testSendMail() {
        System.out.println("尝试阻塞式的获取发送邮件任务......");
        List<String> sendMailTasks = takeSendMailTask();

        enqueueSendMailTask("第一个邮件发送任务");
        sendMailTasks = takeSendMailTask();
        System.out.println(sendMailTasks);
    }

    // ===================== UV =====================
    /**
     * 添加一次用户访问记录
     */
    private void addUserAccess(long userId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd");
        String today = dateFormat.format(new Date());
        jedis.sadd("user_access::" + today, String.valueOf(userId));
    }

    /**
     * 获取当天的网站uv的值
     * @return UV值
     */
    private long getUV() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd");
        String today = dateFormat.format(new Date());
        return jedis.scard("user_access::" + today);
    }

    @Test
    public void testUV()  {

        // 100 个用户，每个用户访问10次
        for(int i = 0; i < 100; i++) {
            long userId = i + 1;

            for(int j = 0; j < 10; j++) {
                addUserAccess(userId);
            }
        }

        long uv = getUV();
        // 输出应为 100
        System.out.println("当日uv为：" + uv);
    }

    // ===================== 朋友圈 =====================
    /**
     * 对朋友圈进行点赞
     * @param userId 用户Id
     * @param momentId 动态Id
     */
    private void likeMoment(long userId, long momentId) {
        jedis.sadd("moment_like_users::" + momentId, String.valueOf(userId));
    }

    /**
     * 对朋友圈取消点赞
     * @param userId 用户Id
     * @param momentId 动态Id
     */
    private void dislikeMoment(long userId, long momentId) {
        jedis.srem("moment_like_users::" + momentId, String.valueOf(userId));
    }

    /**
     * 查看自己是否对某条朋友圈点赞过
     * @param userId 用户Id
     * @param momentId 动态Id
     * @return 是否
     */
    private boolean hasLikedMoment(long userId, long momentId) {
        return jedis.sismember("moment_like_users::" + momentId, String.valueOf(userId));
    }

    /**
     * 获取你的一条朋友圈有哪些人点赞了
     * @param momentId 动态Id
     * @return 哪些人
     */
    private Set<String> getMomentLikeUsers(long momentId) {
        return jedis.smembers("moment_like_users::" + momentId);
    }

    /**
     * 获取你的一条朋友圈被几个人点赞了
     * @param momentId  动态Id
     * @return 点赞数
     */
    private long getMomentLikeUsersCount(long momentId) {
        return jedis.scard("moment_like_users::" + momentId);
    }

    @Test
    public void testMoment() {
        // 你的用户id
        long userId = 11;
        // 你的朋友圈id
        long momentId = 151;
        // 你的朋友1的用户id
        long friendId = 12;
        // 你的朋友2的用户id
        long otherFriendId = 13;

        // 你的朋友1对你的朋友圈进行点赞，再取消点赞
        likeMoment(friendId, momentId);
        dislikeMoment(friendId, momentId);
        boolean hasLikedMoment = hasLikedMoment(friendId, momentId);
        System.out.println("朋友1刷朋友圈，看到是否对你的朋友圈点赞过：" + (hasLikedMoment ? "是" : "否"));

        // 你的朋友2对你的朋友圈进行点赞
        likeMoment(otherFriendId, momentId);
        hasLikedMoment = hasLikedMoment(otherFriendId, momentId);
        System.out.println("朋友2刷朋友圈，看到是否对你的朋友圈点赞过：" + (hasLikedMoment ? "是" : "否"));

        // 你自己刷朋友圈，看自己的朋友圈的点赞情况
        Set<String> momentLikeUsers = getMomentLikeUsers(momentId);
        long momentLikeUsersCount = getMomentLikeUsersCount(momentId);
        System.out.println("你自己刷朋友圈，看到自己发的朋友圈被" + momentLikeUsersCount + "个人点赞了，点赞的用户为：" + momentLikeUsers);
    }

    // ===================== 投票 =====================
    /**
     * 投票
     * @param userId 用户Id
     * @param voteItemId 投票事项
     */
    private void vote(long userId, long voteItemId) {
        jedis.sadd("vote_item_users::" + voteItemId, String.valueOf(userId));
    }

    /**
     * 检查用户对投票项是否投过票
     * @param userId 用户Id
     * @param voteItemId 投票事项
     * @return 是否
     */
    private boolean hasVoted(long userId, long voteItemId) {
        return jedis.sismember("vote_item_users::" + voteItemId, String.valueOf(userId));
    }

    /**
     * 获取一个投票项被哪些人投票了
     * @param voteItemId 投票事项
     * @return 列表
     */
    private Set<String> getVoteItemUsers(long voteItemId) {
        return jedis.smembers("vote_item_users::" + voteItemId);
    }

    /**
     * 获取一个投票项被多少人投票了
     * @param voteItemId 投票事项
     * @return 总数
     */
    private long getVoteItemUsersCount(long voteItemId) {
        return jedis.scard("vote_item_users::" + voteItemId);
    }

    @Test
    public void testVote() {

        // 定义用户id
        long userId = 1;
        // 定义投票项id
        long voteItemId = 110;

        // 进行投票
        vote(userId, voteItemId);
        // 检查我是否投票过
        boolean hasVoted = hasVoted(userId, voteItemId);
        System.out.println("用户查看自己是否投票过：" +(hasVoted ? "是" : "否"));
        // 归票统计
        Set<String> voteItemUsers = getVoteItemUsers(voteItemId);
        long voteItemUsersCount = getVoteItemUsersCount(voteItemId);
        System.out.println("投票项有哪些人投票：" + voteItemUsers + "，有几个人投票：" + voteItemUsersCount);
    }

    // ===================== 微博 =====================
    /**
     * 关注别人
     * @param userId 用户Id
     * @param followUserId 跟随用户Id
     */
    private void follow(long userId, long followUserId) {
        jedis.sadd("user::" + followUserId + "::followers", String.valueOf(userId));
        jedis.sadd("user::" + userId + "::follow_users", String.valueOf(followUserId));
    }

    /**
     * 取消关注别人
     * @param userId 用户Id
     * @param followUserId 跟随用户Id
     */
    public void unfollow(long userId, long followUserId) {
        jedis.srem("user::" + followUserId + "::followers", String.valueOf(userId));
        jedis.srem("user::" + userId + "::follow_users", String.valueOf(followUserId));
    }

    /**
     * 查看有哪些人关注了自己
     * @param userId 用户Id
     * @return 用户列表
     */
    private Set<String> getFollowers(long userId) {
        return jedis.smembers("user::" + userId + "::followers");
    }

    /**
     * 查看关注了自己的人数
     * @param userId 用户Id
     * @return 人数
     */
    private long getFollowersCount(long userId) {
        return jedis.scard("user::" + userId + "::followers");
    }

    /**
     * 查看自己关注了哪些人
     * @param userId 用户Id
     * @return 人
     */
    private Set<String> getFollowUsers(long userId) {
        return jedis.smembers("user::" + userId + "::follow_users");
    }

    /**
     * 查看自己关注的人数
     * @param userId 用户Id
     * @return 人数
     */
    private long getFollowUsersCount(long userId) {
        return jedis.scard("user::" + userId + "::follow_users");
    }

    /**
     * 获取用户跟其他用户之间共同关注的人有哪些
     * @param userId 用户Id
     * @param otherUserId 其他用户Id
     * @return 人
     */
    private Set<String> getSameFollowUsers(long userId, long otherUserId) {
        return jedis.sinter("user::" + userId + "::follow_users",
                "user::" + otherUserId + "::follow_users");
    }

    /**
     * 获取给我推荐的可关注人
     * 我关注的某个好友关注的一些人，我没关注那些人，此时推荐那些人给我
     * @param userId 用户Id
     * @return 人
     */
    private Set<String> getRecommendFollowUsers(long userId, long otherUserId) {
        return jedis.sdiff("user::" + otherUserId + "::follow_users",
                "user::" + userId + "::follow_users");
    }

    @Test
    public void testWeiBo()  {

        // 定义用户id
        long userId = 31;
        long friendId = 32;
        long superstarId = 33;
        long classmateId = 34;
        long motherId = 35;

        // 定义关注的关系链
        follow(userId, friendId);
        follow(userId, motherId);
        follow(userId, superstarId);
        follow(friendId, superstarId);
        follow(friendId, classmateId);

        // 明星看看自己被哪些人关注了
        Set<String> superstarFollowers = getFollowers(superstarId);
        long superstarFollowersCount = getFollowersCount(superstarId);
        System.out.println("明星被哪些人关注了：" + superstarFollowers + "，关注自己的人数为：" + superstarFollowersCount);

        // 朋友看看自己被哪些人关注了，自己关注了哪些人
        Set<String> friendFollowers = getFollowers(friendId);
        long friendFollowersCount = getFollowersCount(friendId);

        Set<String> friendFollowUsers = getFollowUsers(friendId);
        long friendFollowUsersCount = getFollowUsersCount(friendId);

        System.out.println("朋友被哪些人关注了：" + friendFollowers + "，被多少人关注了：" + friendFollowersCount
                + "，朋友关注了哪些人：" + friendFollowUsers + "，关注了多少人：" + friendFollowUsersCount);

        // 查看我自己关注了哪些
        Set<String> myFollowUsers = getFollowUsers(userId);
        long myFollowUsersCount = getFollowUsersCount(userId);
        System.out.println("我关注了哪些人：" + myFollowUsers + ", 我关注的人数：" + myFollowUsersCount);

        // 获取我和朋友共同关注的好友
        Set<String> sameFollowUsers = getSameFollowUsers(userId, friendId);
        System.out.println("我和朋友共同关注的人有哪些：" + sameFollowUsers);

        // 获取推荐给我的可以关注的人，就是我关注的人关注的其他人
        Set<String> recommendFollowUsers = getRecommendFollowUsers(userId, friendId);
        System.out.println("推荐给我的关注的人有哪些：" + recommendFollowUsers);
    }
}
