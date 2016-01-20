package com.dianping.cascade.invocation.method;

import com.dianping.cascade.MethodInvocationInterceptorFactory;
import com.dianping.cascade.annotation.Cacheable;
import com.dianping.cascade.MethodInvocationHandler;
import com.dianping.cascade.MethodInvocationInterceptor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangjie on 1/20/16.
 */
public class CacheableMethodInvocationInterceptorFactory implements MethodInvocationInterceptorFactory {

    private static class CacheableMethodInvocationHandler implements MethodInvocationInterceptor {
        private Cache<Object, Object> resultsCache;

        public CacheableMethodInvocationHandler(Cacheable cacheable) {
            CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

            if (cacheable.expireMinutes() > 0) {
                builder.expireAfterWrite(cacheable.expireMinutes(), TimeUnit.MINUTES);
            }

            resultsCache = builder.build();
        }

        @Override
        public Object invoke(final MethodInvocationHandler handler, final Object[] args) throws Exception {
            return resultsCache.get(Arrays.deepHashCode(args), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return handler.invoke(args);
                }
            });
        }
    }

    @Override
    public MethodInvocationInterceptor create(Method method, Object target) {

        if (method.getAnnotation(Cacheable.class) != null) {
            return new CacheableMethodInvocationHandler(method.getAnnotation(Cacheable.class));
        }

        return null;
    }
}