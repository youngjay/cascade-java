package com.dianping.cascade;

import com.dianping.cascade.annotation.Cacheable;
import com.dianping.cascade.invoker.method.CacheableMethodInvoker;
import com.dianping.cascade.invoker.method.DefaultMethodInvoker;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 12/27/15.
 */
public class ContextParamsInvokerFactory {
    public ContextParamsInvoker create(final Object target, final Method method) {
        final ParameterResolvers parameterResolvers = new ParameterResolvers(method);

        MethodInvoker methodInvoker = new DefaultMethodInvoker();

        if (method.getAnnotation(Cacheable.class) != null) {
            methodInvoker = new CacheableMethodInvoker(methodInvoker, method.getAnnotation(Cacheable.class));
        }

        final MethodInvoker finalMethodInvoker = methodInvoker;

        return new ContextParamsInvoker() {
            @Override
            public Object invoke(ContextParams params) throws Exception {
                return finalMethodInvoker.invoke(target, method, parameterResolvers.resolve(params).toArray());
            }
        };
    }

}
