package com.dianping.cascade;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 12/27/15.
 */
public class MethodParametersResolver {

    private List<ContextParamResolver> parameterResolvers = Lists.newArrayList();
    private String methodName;

    public MethodParametersResolver(Method method, Map<Class<? extends Annotation>, ParameterResolverFactory> resolverFactoryMap) {
        methodName = method.getName();
        int parameterIndex = 0;

        Type[] types = method.getGenericParameterTypes();

        for (Annotation[] annotations : method.getParameterAnnotations()) {
            Type type = types[parameterIndex];

            ContextParamResolver contextParamResolver = getParameterResolver(annotations, type, resolverFactoryMap);

            if (contextParamResolver == null) {
                throw new IllegalArgumentException(String.format("%s: can not resolve the [%s] parameter", methodName, parameterIndex + 1));
            }

            parameterResolvers.add(contextParamResolver);

            parameterIndex += 1;
        }
    }

    private ContextParamResolver getParameterResolver(Annotation[] annotations, Type type, Map<Class<? extends Annotation>, ParameterResolverFactory> resolverFactoryMap) {
        for (Annotation annotation : annotations) {
            if (resolverFactoryMap.containsKey(annotation.annotationType())) {
                return resolverFactoryMap.get(annotation.annotationType()).create(annotation, new ParamConverter(type));
            }
        }
        return null;
    }

    public List<Object> resolve(final ContextParams params) {
        try {
            return Lists.transform(parameterResolvers, new Function<ContextParamResolver, Object>() {
                @Override
                public Object apply(ContextParamResolver input) {
                    return input.resolve(params);
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(methodName + ": illegal argument: " + ex.getMessage());
        }
    }
}
