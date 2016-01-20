package com.dianping.cascade;

import com.dianping.cascade.invocation.method.MethodInvocationInterceptor;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 1/20/16.
 */
public interface MethodInvocationInterceptorFactory {
    MethodInvocationInterceptor create(Method method, Object target);
}
