package com.dianping.cascade.invocation.method.impl;

import com.dianping.cascade.invocation.method.MethodInvocationHandler;
import com.dianping.cascade.invocation.method.MethodInvocationInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by yangjie on 12/28/15.
 */
public class DefaultMethodInvocationHandler implements MethodInvocationInterceptor {
    @Override
    public Object invoke(MethodInvocationHandler nothing,  Object target, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }
}
