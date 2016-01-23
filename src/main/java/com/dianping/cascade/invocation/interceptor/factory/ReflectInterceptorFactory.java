package com.dianping.cascade.invocation.interceptor.factory;

import com.dianping.cascade.InvocationInterceptor;
import com.dianping.cascade.InvocationInterceptorFactory;
import com.dianping.cascade.MethodParametersResolver;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 1/23/16.
 */
// 用于构造无参的InvocationInterceptor
public class ReflectInterceptorFactory implements InvocationInterceptorFactory {
    private final Class<? extends InvocationInterceptor> clazz;

    public ReflectInterceptorFactory(Class<? extends InvocationInterceptor> clazz) {
        this.clazz = clazz;
    }

    @Override
    public InvocationInterceptor create(Method method, Object target, MethodParametersResolver methodParametersResolver) {
        try {
            return clazz.newInstance();
        } catch (Throwable t) {
            throw new RuntimeException("Unable to create InvocationInterceptor from class " + clazz, t);
        }
    }
}
