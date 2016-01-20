package com.dianping.cascade.invocation.method;

/**
 * Created by yangjie on 1/11/16.
 */
public interface MethodInvocationInterceptor {
    Object invoke(MethodInvocationHandler handler, Object[] args) throws Exception;
}
