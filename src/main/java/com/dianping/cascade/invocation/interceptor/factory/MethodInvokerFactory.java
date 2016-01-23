package com.dianping.cascade.invocation.interceptor.factory;

import com.dianping.cascade.*;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 1/23/16.
 */
public class MethodInvokerFactory implements InvocationInterceptorFactory {

    private static class MethodInvoker implements InvocationInterceptor {
        private Method method;
        private Object target;
        private ParameterResolvers parameterResolvers;

        MethodInvoker(Method method, Object target) {
            this.method = method;
            this.target = target;
            parameterResolvers = new ParameterResolvers(method);
        }

        @Override
        public Object invoke(InvocationHandler invocationHandler, Field field, ContextParams contextParams) {
            try {
                return method.invoke(target, parameterResolvers.resolve(contextParams).toArray());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public InvocationInterceptor create(Method method, Object target) {
        return new MethodInvoker(method, target);
    }
}
