package com.donaldy.redispractice.thread;

import com.donaldy.redispractice.request.CacheRequest;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

public class RequestProcessorThread implements Callable<Boolean> {

    /**
     * 监控的内存队列
     */
    private ArrayBlockingQueue<CacheRequest> queue;

    public RequestProcessorThread(ArrayBlockingQueue<CacheRequest> queue) {

        this.queue = queue;
    }

    @Override
    public Boolean call() throws Exception {

        try {

            while (!Thread.currentThread().isInterrupted()) {

                CacheRequest cacheRequest = queue.take();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return true;
    }
}
