package com.dianping.cascade.invocation.method;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 1/11/16.
 */
public interface MethodInvocationInterceptor {
    Object invoke(MethodInvocationHandler handler, Object target, Method method, Object[] args) throws Exception;
}
