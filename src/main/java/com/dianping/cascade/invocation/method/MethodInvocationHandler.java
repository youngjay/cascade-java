package com.dianping.cascade.invocation.method;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 12/28/15.
 */
public interface MethodInvocationHandler {
    Object invoke(Object target, Method method, Object[] args) throws Exception;
}
