package com.dianping.cascade;

/**
 * Created by yangjie on 12/5/15.
 */
public interface InvocationInterceptor {
    Object invoke(InvocationHandler invocationHandler, Field field, ContextParams contextParams);
}
