package com.dianping.cascade.invocation.params.impl;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.ParameterResolvers;
import com.dianping.cascade.annotation.Cacheable;
import com.dianping.cascade.invocation.method.MethodInvocationHandler;
import com.dianping.cascade.invocation.method.MethodInvocationInterceptor;
import com.dianping.cascade.invocation.method.impl.CacheableMethodInvocationHandler;
import com.dianping.cascade.invocation.method.impl.DefaultMethodInvocationHandler;
import com.dianping.cascade.invocation.params.ContextParamsInvocationHandler;
import com.google.common.collect.Lists;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by yangjie on 1/11/16.
 */
public class DefaultContextParamsInvocationHandler implements ContextParamsInvocationHandler {
    private Object target;
    private Method method;
    private MethodInvocationHandler methodInvocationHandler;
    private ParameterResolvers parameterResolvers;


    public DefaultContextParamsInvocationHandler(Object target, Method method) {
        this.target = target;
        this.method = method;

        parameterResolvers = new ParameterResolvers(method);
        methodInvocationHandler = createMethodInvocationHandler();
    }

    private MethodInvocationHandler createMethodInvocationHandler() {
        List<MethodInvocationInterceptor> methodInvocationInterceptors = Lists.newArrayList();

        methodInvocationInterceptors.add(new DefaultMethodInvocationHandler());

        if (method.getAnnotation(Cacheable.class) != null) {
            methodInvocationInterceptors.add(new CacheableMethodInvocationHandler(method.getAnnotation(Cacheable.class)));
        }

        MethodInvocationHandler last = null;

        for (final MethodInvocationInterceptor interceptor: methodInvocationInterceptors) {
            final MethodInvocationHandler prev = last;
            last = new MethodInvocationHandler() {
                @Override
                public Object invoke(Object target, Method method, Object[] args) throws Exception {
                    return interceptor.invoke(prev, target, method, args);
                }
            };
        }

        return last;
    }


    @Override
    public Object invoke(ContextParams params) throws Exception {
        return methodInvocationHandler.invoke(target, method, parameterResolvers.resolve(params).toArray());
    }
}
