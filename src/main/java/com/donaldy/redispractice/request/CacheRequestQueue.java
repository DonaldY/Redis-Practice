package com.donaldy.redispractice.request;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 缓存请求队列
 */
public class CacheRequestQueue {

    /**
     * 内存队列
     */
    private List<ArrayBlockingQueue<CacheRequest>> queues = new ArrayList<>();

    /**
     * 静态内部类的方式，去初始化单例
     */
    private static class Singleton {

        private static CacheRequestQueue instance;

        static {
            instance = new CacheRequestQueue();
        }

        static CacheRequestQueue getInstance() {
            return instance;
        }
    }

    /**
     * JVM 机制去保证多线程并发安全
     *
     * 内部类的初始化，一定只会发生一次，不管多少线程并去初始化
     *
     * @return 缓存队列
     */
    public static CacheRequestQueue getInstance(){

        return Singleton.getInstance();
    }

    /**
     * 添加一个内存队列
     *
     * @param queue 队列
     */
    public void addQueue(ArrayBlockingQueue<CacheRequest> queue) {

        this.queues.add(queue);
    }

    /**
     * 获取内存队列数
     *
     * @return 请求队列数
     */
    public int getSize() {

        return this.queues.size();
    }

    /**
     * 获取对应请求队列数
     *
     * @param index 请求队列
     * @return      请求队列
     */
    public ArrayBlockingQueue<CacheRequest> getQueue(int index) {

        return this.queues.get(index);
    }
}
