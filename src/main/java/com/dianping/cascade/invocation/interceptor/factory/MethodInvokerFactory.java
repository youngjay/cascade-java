package com.dianping.cascade.invocation.interceptor.factory;

import com.dianping.cascade.*;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 1/23/16.
 */
public class MethodInvokerFactory implements InvocationInterceptorFactory {

    @Override
    public InvocationInterceptor create(final Method method, final Object target, final MethodParametersResolver methodParametersResolver) {
        return new InvocationInterceptor() {
            @Override
            public Object invoke(InvocationHandler invocationHandler, Field field, ContextParams contextParams) {
                try {
                    return method.invoke(target, methodParametersResolver.resolve(contextParams).toArray());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
