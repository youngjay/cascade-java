package com.dianping.cascade;

import com.dianping.cascade.annotation.Param;
import com.dianping.cascade.parameterresolver.ParamValueResolver;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 10/23/15.
 */
public class Invokable {

    private Object target;

    private Map<String, MethodAndResolvers> methodParams = Maps.newHashMap();

    public Invokable(Object obj) {
        target = obj;

        for (Method method : target.getClass().getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                registerMethod(method);
            }
        }
    }

    private void registerMethod(Method method) {
        String methodName = method.getName();

        MethodAndResolvers methodAndResolvers = methodParams.get(methodName);

        if (methodAndResolvers != null) {
            throw new RuntimeException(getLocation(methodName) + "has already registered");
        }

        List<ParameterResolver> parameterResolvers = Lists.newArrayList();

        int parameterIndex = 0;

        Class<?>[] types = method.getParameterTypes();

        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Param) {
                    parameterResolvers.add(new ParamValueResolver(((Param) annotation).value(), types[parameterIndex]));
                    break;
                }
            }

            parameterIndex += 1;

            if (parameterResolvers.size() != parameterIndex) {
                throw new IllegalArgumentException(getLocation(methodName) + "every arguments must have @Param annotation");
            }
        }

        methodParams.put(methodName, new MethodAndResolvers(method, parameterResolvers));
    }


    @Data
    @AllArgsConstructor
    private class MethodAndResolvers {
        private Method method;
        private List<ParameterResolver> parameterResolvers;

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
    }

    public Object invoke(String methodName, ContextParams params) {

        MethodAndResolvers methodAndResolvers = methodParams.get(methodName);

        if (methodAndResolvers == null) {
            throw new RuntimeException(getLocation(methodName) + "not registered");
        }

        return methodAndResolvers.invoke(params);
    }

    private String getLocation(String methodName) {
        return String.format("[%s.%s] ", target.getClass().getSimpleName(), methodName);
    }

}
