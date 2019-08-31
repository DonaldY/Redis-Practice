package com.donaldy.redispractice.service;

import com.donaldy.redispractice.request.CacheRequest;

/**
 * 请求异步执行
 */
public interface RequestAsyncProcessService {

    /**
     * 处理请求
     *
     * @param request 请求
     */
    void process(CacheRequest request);
}
