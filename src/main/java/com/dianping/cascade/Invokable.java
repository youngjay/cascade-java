package com.dianping.cascade;

import com.dianping.cascade.annotation.Entity;
import com.dianping.cascade.annotation.Param;
import com.dianping.cascade.parameterresolver.EntityResolver;
import com.dianping.cascade.parameterresolver.ParamResolver;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by yangjie on 12/5/15.
 */
public class Invokable {
    private Object target;
    private Method method;
    private List<ParameterResolver> parameterResolvers = Lists.newArrayList();

    public Invokable(Object target, Method method) {
        this.target = target;
        this.method = method;
        this.buildParameterResolvers();
    }

    public void buildParameterResolvers() {
        int parameterIndex = 0;

        Class<?>[] types = method.getParameterTypes();

        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Param) {
                    parameterResolvers.add(new ParamResolver(((Param) annotation).value(), types[parameterIndex]));
                    break;
                }
                if (annotation instanceof Entity) {
                    parameterResolvers.add(new EntityResolver(types[parameterIndex]));
                    break;
                }
            }

            parameterIndex += 1;

            if (parameterResolvers.size() != parameterIndex) {
                throw new IllegalArgumentException(getLocation(method.getName()) + "every arguments must have @Param annotation");
            }
        }
    }

    public Object invoke(final ContextParams params) {
        List args;

        try {
            args = Lists.transform(parameterResolvers, new Function<ParameterResolver, Object>() {
                @Override
                public Object apply(ParameterResolver input) {
                    return input.resolve(params);
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(getLocation(method.getName()) + "illegal argument: " + ex.getMessage());
        }

        try {
            return method.invoke(target, args.toArray());
        } catch (Exception ex) {
            Throwable cause = ex.getCause();

            if (cause instanceof BusinessException) {
                throw (BusinessException) cause;
            }

            String msg;

            if (cause == null) {
                msg = ex.getMessage();
            } else {
                msg = cause.getMessage();

                if (msg == null) {
                    msg = cause.getClass().getSimpleName();
                }
            }

            throw new RuntimeException(getLocation(method.getName()) + msg);
        }
    }


    private String getLocation(String methodName) {
        return String.format("[%s.%s] ", target.getClass().getSimpleName(), methodName);
    }
}
