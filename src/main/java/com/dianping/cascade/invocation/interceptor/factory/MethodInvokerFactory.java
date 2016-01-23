package com.dianping.cascade.invocation.interceptor.factory;

import com.dianping.cascade.*;
import lombok.AllArgsConstructor;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 1/23/16.
 */
public class MethodInvokerFactory implements InvocationInterceptorFactory {

    @AllArgsConstructor
    private static class MethodInvoker implements InvocationInterceptor {
        private Method method;
        private Object target;
        private MethodParametersResolver methodParametersResolver;

        @Override
        public Object invoke(InvocationHandler invocationHandler, Field field, ContextParams contextParams) {
            try {
                return method.invoke(target, methodParametersResolver.resolve(contextParams).toArray());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public InvocationInterceptor create(Method method, Object target, MethodParametersResolver methodParametersResolver) {
        return new MethodInvoker(method, target, methodParametersResolver);
    }
}
