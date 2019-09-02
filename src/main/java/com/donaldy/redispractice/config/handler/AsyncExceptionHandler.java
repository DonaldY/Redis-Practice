package com.donaldy.redispractice.config.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.info("Async method: {} has uncaught exception,params:{}", method.getName(), params.toString());

        /*if (ex instanceof AsyncException) {
            AsyncException asyncException = (AsyncException) ex;
            log.info("asyncException:{}",asyncException.getErrorMessage());
        }*/

        log.info("异步发生异常啦！");

    }
}
