package com.donaldy.redispractice.request;

public interface CacheRequest {

    /**
     * 处理请求
     */
    void process();

    /**
     * 获取商品ID
     *
     * @return 商品ID
     */
    Integer getProductId();
}
