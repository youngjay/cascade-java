package com.dianping.cascade;

import com.dianping.cascade.annotation.Cacheable;
import com.dianping.cascade.invoker.method.CacheableMethodInvoker;
import com.dianping.cascade.invoker.method.DefaultMethodInvoker;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 12/27/15.
 */
public class ContextParamsInvokerFactory {
    ContextParamsInvoker create(final Object target, final Method method) {
        final ParameterResolvers parameterResolvers = new ParameterResolvers(method);

        MethodInvoker methodInvoker = new DefaultMethodInvoker();

        if (method.getAnnotation(Cacheable.class) != null) {
            methodInvoker = new CacheableMethodInvoker(methodInvoker, method.getAnnotation(Cacheable.class));
        }


        final MethodInvoker finalMethodInvoker = methodInvoker;

        return new ContextParamsInvoker() {
            @Override
            public Object invoke(ContextParams params) {
                try {
                    return finalMethodInvoker.invoke(target, method, parameterResolvers.resolve(params).toArray());
                } catch (Exception ex) {
                    Throwable cause = getCause(ex);

                    if (cause instanceof BusinessException) {
                        throw (BusinessException) cause;
                    }

                    String msg = cause.getMessage();

                    if (msg == null) {
                        msg = cause.getClass().getSimpleName();
                    }

                    throw new RuntimeException(String.format("[%s.%s] %s", target.getClass().getSimpleName(), method.getName(), msg));
                }
            }
        };


    }

    private Throwable getCause(Throwable outer) {
        Throwable inner = outer.getCause();
        return inner == null ? outer : getCause(inner);
    }
}
