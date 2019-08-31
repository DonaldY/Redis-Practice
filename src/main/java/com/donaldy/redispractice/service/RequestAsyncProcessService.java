package com.donaldy.redispractice.service;

import com.donaldy.redispractice.request.CacheRequest;

/**
 * 请求异步执行
 */
public interface RequestAsyncProcessService {

    void process(CacheRequest request);
}
