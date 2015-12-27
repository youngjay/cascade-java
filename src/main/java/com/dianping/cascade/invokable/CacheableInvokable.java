package com.dianping.cascade.invokable;

import com.dianping.cascade.ParameterResolvers;
import com.dianping.cascade.annotation.Cacheable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangjie on 12/27/15.
 */
public class CacheableInvokable extends DefaultInvokable {
    private Cache<List, Object> resultsCache;

    public CacheableInvokable(Object target, Method method, ParameterResolvers parameterResolvers, Cacheable cacheable) {
        super(target, method, parameterResolvers);

        resultsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheable.expireHours(), TimeUnit.HOURS)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invokeByArgs(final List args) {
        try {
            return resultsCache.get(args, new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return CacheableInvokable.super.invokeByArgs(args);
                }
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
