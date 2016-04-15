package com.dianping.cascade;

import com.dianping.cascade.invocation.interceptor.factory.CacheableFactory;
import com.dianping.cascade.invocation.interceptor.factory.ExceptionHandlerFactory;
import com.dianping.cascade.invocation.interceptor.factory.MethodInvokerFactory;
import com.dianping.cascade.invocation.interceptor.factory.PropsPickerFactory;
import com.dianping.cascade.resolver.factory.EntityResolverFactory;
import com.dianping.cascade.resolver.factory.ParamResolverFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 12/5/15.
 */
public class Registry {
    private Map<String, InvocationHandler> invocationHandlerMap = Maps.newHashMap();

    private CascadeFactoryConfig config;

    @Getter
    private InvocationHandler invocationHandler;

    public Registry(CascadeFactoryConfig config) {
        this.config = config;

        invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Field field, ContextParams contextParams) {
                String mapKey = getKey(field.getType(), field.getCategory());
                InvocationHandler invocationHandler = invocationHandlerMap.get(mapKey);

                if (invocationHandler == null) {
                    return ExceptionHandlerFactory.CASCADE_ERROR + "[" + mapKey + "] has not registered";
                }

                return invocationHandler.invoke(field, contextParams);
            }
        };
    }

    public void register(Object bean) {
        register(bean.getClass().getSimpleName(), bean);
    }

    public void register(String type, Object bean) {
       for (Method method : bean.getClass().getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                registerMethod(type, bean, method);
            }
       }
    }

    private String getKey(String type, String methodName) {
        return type + "." + methodName;
    }

    private void registerMethod(String type, Object target, Method method) {
        String methodName = method.getName();

        String mapKey = getKey(type, methodName);

        if (invocationHandlerMap.containsKey(mapKey)) {
            throw new RuntimeException(mapKey + " has already registered");
        }

        invocationHandlerMap.put(mapKey, buildFieldInvocationHandler(method, target));
    }

    private InvocationHandler buildFieldInvocationHandler(Method method, Object target) {
        return buildInvocationHandler(
                buildInvocationInterceptors(
                        getInvocationInterceptorFactories(), method, target, getParameterResolvers(method)
                )
        );
    }

    private MethodParametersResolver getParameterResolvers(Method method) {
        List<ParameterResolverFactory> parameterResolverFactories = Lists.newArrayList();

        if (config.getParameterResolverFactories() != null) {
            parameterResolverFactories.addAll(config.getParameterResolverFactories());
        }

        parameterResolverFactories.add(new ParamResolverFactory());
        parameterResolverFactories.add(new EntityResolverFactory());

        return new MethodParametersResolver(method, parameterResolverFactories);
    }

    private List<InvocationInterceptor> buildInvocationInterceptors(
            List<InvocationInterceptorFactory> invocationInterceptorFactories,
            Method method,
            Object target,
            MethodParametersResolver methodParametersResolver
    ) {
        List<InvocationInterceptor> invocationInterceptors = Lists.newArrayList();
        for (InvocationInterceptorFactory invocationInterceptorFactory : invocationInterceptorFactories) {
            InvocationInterceptor invocationInterceptor = invocationInterceptorFactory.create(method, target, methodParametersResolver);
            if (invocationInterceptor != null) {
                invocationInterceptors.add(invocationInterceptor);
            }
        }
        return invocationInterceptors;
    }

    private List<InvocationInterceptorFactory> getInvocationInterceptorFactories() {
        List<InvocationInterceptorFactory> fieldInvocationInterceptorFactories = Lists.newArrayList();

        fieldInvocationInterceptorFactories.add(new MethodInvokerFactory());
        fieldInvocationInterceptorFactories.add(new CacheableFactory());
        fieldInvocationInterceptorFactories.add(new PropsPickerFactory());

        if (config.getInvocationInterceptorFactories() != null) {
            fieldInvocationInterceptorFactories.addAll(config.getInvocationInterceptorFactories());
        }

        fieldInvocationInterceptorFactories.add(new ExceptionHandlerFactory());

        return fieldInvocationInterceptorFactories;
    }

    private InvocationHandler buildInvocationHandler(List<InvocationInterceptor> interceptors) {
        InvocationHandler last = null;
        for (final InvocationInterceptor interceptor : interceptors) {
            final InvocationHandler prev = last;
            last = new InvocationHandler() {
                @Override
                public Object invoke(Field field, ContextParams contextParams) {
                    return interceptor.invoke(prev, field, contextParams);
                }
            };
        }

        return last;
    }

}
