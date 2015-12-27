package com.dianping.cascade.invokable;

import com.dianping.cascade.ParameterResolvers;
import com.dianping.cascade.annotation.Cacheable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.lang.reflect.Method;
import java.util.List;
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
    public Object invoke(List args) {
        Object result = resultsCache.getIfPresent(args);
        if (result == null) {
            result = super.invoke(args);
            resultsCache.put(args, result);
        }
        return result;
    }
}
