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
 * Created by yangjie on 12/27/15.
 */
public class ParameterResolvers {

    private List<ParameterResolver> parameterResolvers = Lists.newArrayList();
    private String methodName;

    public ParameterResolvers(Method method) {
        methodName = method.getName();
        int parameterIndex = 0;

        Class<?>[] types = method.getParameterTypes();

        for (Annotation[] annotations : method.getParameterAnnotations()) {
            Class type = types[parameterIndex];

            for (Annotation annotation : annotations) {
                if (annotation instanceof Param) {
                    parameterResolvers.add(new ParamResolver(((Param) annotation).value(), type));
                    break;
                }
                if (annotation instanceof Entity) {
                    parameterResolvers.add(new EntityResolver(type));
                    break;
                }
            }

            parameterIndex += 1;

            if (parameterResolvers.size() != parameterIndex) {
                throw new IllegalArgumentException(methodName + ": every argument must have @Param or @Entity annotation");
            }
        }
    }

    public List<Object> resolve(final ContextParams params) {
        try {
            return Lists.transform(parameterResolvers, new Function<ParameterResolver, Object>() {
                @Override
                public Object apply(ParameterResolver input) {
                    return input.resolve(params);
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(methodName + ": illegal argument: " + ex.getMessage());
        }
    }
}
