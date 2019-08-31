package com.donaldy.redispractice.service.impl;

import com.donaldy.redispractice.request.CacheRequest;
import com.donaldy.redispractice.request.CacheRequestQueue;
import com.donaldy.redispractice.service.RequestAsyncProcessService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;

@Service
public class RequestAsyncProcessServiceImpl implements RequestAsyncProcessService {

    @Override
    public void process(CacheRequest request) {
        // 做请求的路由，根据每个请求的商品ID，路由对应到内存队列中
        ArrayBlockingQueue<CacheRequest> queue = this.getRoutingQueue(request.getProductId());

        try {

            queue.put(request);

        } catch (InterruptedException e) {

            e.printStackTrace();
        }
    }

    /**
     * 获取路由的内存队列
     *
     * @param productId 商品ID
     * @return          内存队列
     */
    private ArrayBlockingQueue<CacheRequest> getRoutingQueue(Integer productId) {

        CacheRequestQueue requestQueue = CacheRequestQueue.getInstance();

        String key = String.valueOf(productId);

        int h;

        int hash = (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);

        // 对 hash 值取模， 将 hash 值路由到指定的内存队列中
        // 比如内存队列大小8, 用内存队列的数量对hash值取模之后，结果一定是 0 ～ 7之间
        // 所有任何一个商品ID都会被固定路由到对应队列中
        int index = (requestQueue.getSize() - 1) & hash;

        return requestQueue.getQueue(index);
    }
}
