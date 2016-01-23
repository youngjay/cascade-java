package com.dianping.cascade;

import com.dianping.cascade.invocation.interceptor.ExceptionHandler;
import com.dianping.cascade.invocation.interceptor.PropsPicker;
import com.dianping.cascade.invocation.interceptor.factory.CacheableFactory;
import com.dianping.cascade.invocation.interceptor.factory.MethodInvokerFactory;
import com.dianping.cascade.invocation.interceptor.factory.ReflectInterceptorFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 12/5/15.
 */
public class Registry {
    private Map<String, InvocationHandler> fieldInvocationHandlerMap = Maps.newHashMap();

    private CascadeFactoryConfig config;

    public Registry(CascadeFactoryConfig config) {
        this.config = config;
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

        if (fieldInvocationHandlerMap.containsKey(mapKey)) {
            throw new RuntimeException(mapKey + " has already registered");
        }

        fieldInvocationHandlerMap.put(mapKey, buildFieldInvocationHandler(method, target));
    }

    private InvocationHandler buildFieldInvocationHandler(Method method, Object target) {
        return buildInvocationHandler(
                buildInvocationInterceptors(
                        getInvocationInterceptorFactories(), method, target
                )
        );
    }

    private List<InvocationInterceptor> buildInvocationInterceptors(
            List<InvocationInterceptorFactory> invocationInterceptorFactories,
            Method method,
            Object target
    ) {
        List<InvocationInterceptor> invocationInterceptors = Lists.newArrayList();
        for (InvocationInterceptorFactory invocationInterceptorFactory : invocationInterceptorFactories) {
            InvocationInterceptor invocationInterceptor = invocationInterceptorFactory.create(method, target);
            if (invocationInterceptor != null) {
                invocationInterceptors.add(invocationInterceptor);
            }
        }
        return invocationInterceptors;
    }

    private List<InvocationInterceptorFactory> getInvocationInterceptorFactories() {
        List<InvocationInterceptorFactory> fieldInvocationInterceptorFactories = Lists.newArrayList();

        fieldInvocationInterceptorFactories.add(new MethodInvokerFactory());
        fieldInvocationInterceptorFactories.add(new ReflectInterceptorFactory(PropsPicker.class));

        if (config.getFieldInvocationInterceptorFactories() != null) {
            fieldInvocationInterceptorFactories.addAll(config.getFieldInvocationInterceptorFactories());
        }

        fieldInvocationInterceptorFactories.add(new CacheableFactory());
        fieldInvocationInterceptorFactories.add(new ReflectInterceptorFactory(ExceptionHandler.class));

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

    public InvocationHandler getFieldInvocationHandler() {
        return new InvocationHandler() {
            @Override
            public Object invoke(Field field, ContextParams contextParams) {
                String mapKey = getKey(field.getType(), field.getCategory());
                InvocationHandler invocationHandler = fieldInvocationHandlerMap.get(mapKey);

                if (invocationHandler == null) {
                    throw new RuntimeException(mapKey + " has not registered");
                }

                return invocationHandler.invoke(field, contextParams);
            }
        };
    }
}
