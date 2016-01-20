package com.dianping.cascade.invocation;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.Field;
import com.dianping.cascade.FieldInvocationInterceptorFactory;
import com.dianping.cascade.annotation.Cacheable;
import com.dianping.cascade.invocation.field.FieldInvocationHandler;
import com.dianping.cascade.invocation.field.FieldInvocationInterceptor;
import com.dianping.cascade.invocation.method.MethodInvocationHandler;
import com.dianping.cascade.invocation.method.MethodInvocationInterceptor;
import com.dianping.cascade.invocation.method.impl.CacheableMethodInvocationHandler;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangjie on 1/20/16.
 */
public class CacheableFactory implements FieldInvocationInterceptorFactory {

    private static class CacheableMethodInvocationHandler implements FieldInvocationInterceptor {
        private Cache<Object, Object> resultsCache;

        public CacheableMethodInvocationHandler(Cacheable cacheable) {
            CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

            if (cacheable.expireMinutes() > 0) {
                builder.expireAfterWrite(cacheable.expireMinutes(), TimeUnit.MINUTES);
            }

            resultsCache = builder.build();
        }

        @Override
        public Object invoke(FieldInvocationHandler fieldInvocationHandler, Field field, ContextParams contextParams) {
            return resultsCache.get(Arrays.deepHashCode(args), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return methodInvocationHandler.invoke(target, method, args);
                }
            });
        }
//        @Override
//        public Object invoke(final MethodInvocationHandler methodInvocationHandler, final Object target, final Method method, final Object[] args) throws ExecutionException {
//            return resultsCache.get(Arrays.deepHashCode(args), new Callable<Object>() {
//                @Override
//                public Object call() throws Exception {
//                    return methodInvocationHandler.invoke(target, method, args);
//                }
//            });
//        }
    }

    @Override
    public FieldInvocationInterceptor create(Method method, Object target) {
        if (method.getAnnotation(Cacheable.class) != null) {
            return new CacheableMethodInvocationHandler(method.getAnnotation(Cacheable.class));
        }

        return null;
    }
}
