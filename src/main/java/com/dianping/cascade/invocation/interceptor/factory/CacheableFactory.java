package com.dianping.cascade.invocation.interceptor.factory;

import com.dianping.cascade.*;
import com.dianping.cascade.annotation.Cacheable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangjie on 1/23/16.
 */
public class CacheableFactory implements InvocationInterceptorFactory {

    private static class CacheableInterceptor implements InvocationInterceptor {
        private Cache<Object, Object> resultsCache;
        private MethodParametersResolver methodParametersResolver;

        CacheableInterceptor(Cacheable cacheable, MethodParametersResolver methodParametersResolver) {
            CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

            if (cacheable.expireMinutes() > 0) {
                builder.expireAfterWrite(cacheable.expireMinutes(), TimeUnit.MINUTES);
            }

            resultsCache = builder.build();

            this.methodParametersResolver = methodParametersResolver;
        }

        @Override
        public Object invoke(final InvocationHandler invocationHandler, final Field field, final ContextParams contextParams) {

            try {
                return resultsCache.get(methodParametersResolver.resolve(contextParams), new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        return invocationHandler.invoke(field, contextParams);
                    }
                });
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public InvocationInterceptor create(Method method, Object target, MethodParametersResolver methodParametersResolver) {
        if (method.getAnnotation(Cacheable.class) != null) {
            return new CacheableInterceptor(method.getAnnotation(Cacheable.class), methodParametersResolver);
        }

        return null;
    }
}
