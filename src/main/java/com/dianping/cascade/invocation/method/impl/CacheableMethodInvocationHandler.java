package com.dianping.cascade.invocation.method.impl;

import com.dianping.cascade.annotation.Cacheable;
import com.dianping.cascade.invocation.method.MethodInvocationHandler;
import com.dianping.cascade.invocation.method.MethodInvocationInterceptor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangjie on 12/28/15.
 */
public class CacheableMethodInvocationHandler implements MethodInvocationInterceptor {
    private Cache<Object, Object> resultsCache;

    public CacheableMethodInvocationHandler(Cacheable cacheable) {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

        if (cacheable.expireMinutes() > 0) {
            builder.expireAfterWrite(cacheable.expireMinutes(), TimeUnit.MINUTES);
        }

        resultsCache = builder.build();
    }

    @Override
    public Object invoke(final MethodInvocationHandler methodInvocationHandler, final Object target, final Method method, final Object[] args) throws ExecutionException {
        return resultsCache.get(Arrays.deepHashCode(args), new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return methodInvocationHandler.invoke(target, method, args);
            }
        });
    }
}
