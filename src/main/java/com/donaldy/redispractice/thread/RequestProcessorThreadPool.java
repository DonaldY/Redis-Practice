package com.donaldy.redispractice.thread;


import com.donaldy.redispractice.request.CacheRequest;
import com.donaldy.redispractice.request.CacheRequestQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 请求线程池：单例
 */
public class RequestProcessorThreadPool {

    // 可配置
    private ExecutorService threadPool = Executors.newFixedThreadPool(10);

    private RequestProcessorThreadPool() {

        CacheRequestQueue requestQueue = CacheRequestQueue.getInstance();

        for (int i = 0; i < 10; ++i) {

            ArrayBlockingQueue<CacheRequest> queue = new ArrayBlockingQueue<>(100);
            requestQueue.addQueue(queue);
            threadPool.submit(new WorkerThread(queue));
        }
    }

    /**
     * 静态内部类的方式，去初始化单例
     */
    private static class Singleton {

        private static RequestProcessorThreadPool instance;

        static {
            instance = new RequestProcessorThreadPool();
        }

        static RequestProcessorThreadPool getInstance() {
            return instance;
        }
    }

    /**
     * JVM 机制去保证多线程并发安全
     *
     * 内部类的初始化，一定只会发生一次，不管多少线程并去初始化
     *
     * @return 线程池
     */
    private static RequestProcessorThreadPool getInstance(){

        return Singleton.getInstance();
    }

    public static void init() {

        getInstance();
    }
}
