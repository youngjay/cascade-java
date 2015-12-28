package com.dianping.cascade.invoker.method;

import com.dianping.cascade.MethodInvoker;
import com.dianping.cascade.annotation.Cacheable;
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
public class CacheableMethodInvoker implements MethodInvoker {
    private Cache<Object, Object> resultsCache;
    private MethodInvoker methodInvoker;

    public CacheableMethodInvoker(MethodInvoker methodInvoker, Cacheable cacheable) {
        this.methodInvoker = methodInvoker;

        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

        if (cacheable.expireMinutes() > 0) {
            builder.expireAfterWrite(cacheable.expireMinutes(), TimeUnit.MINUTES);
        }

        resultsCache = builder.build();
    }

    @Override
    public Object invoke(final Object target, final Method method, final Object[] args) throws ExecutionException {
        return resultsCache.get(Arrays.deepHashCode(args), new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return CacheableMethodInvoker.this.methodInvoker.invoke(target, method, args);
            }
        });
    }
}
