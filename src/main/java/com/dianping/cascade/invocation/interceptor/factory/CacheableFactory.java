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
    @Override
    public InvocationInterceptor create(Method method, Object target, final MethodParametersResolver methodParametersResolver) {
        if (method.getAnnotation(Cacheable.class) != null) {
            Cacheable cacheable = method.getAnnotation(Cacheable.class);

            CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

            if (cacheable.expireMinutes() > 0) {
                builder.expireAfterWrite(cacheable.expireMinutes(), TimeUnit.MINUTES);
            }

            final Cache<Object, Object> resultsCache = builder.build();

            return new InvocationInterceptor() {
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
            };

        }

        return null;
    }
}
