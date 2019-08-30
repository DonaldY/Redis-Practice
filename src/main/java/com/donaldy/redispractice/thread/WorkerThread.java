package com.donaldy.redispractice.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

public class WorkerThread implements Callable<Boolean> {

    /**
     * 监控的内存队列
     */
    private ArrayBlockingQueue<CacheRequest> queue;

    public WorkerThread(ArrayBlockingQueue<CacheRequest> queue) {

        this.queue = queue;
    }

    @Override
    public Boolean call() throws Exception {

        return true;
    }
}
