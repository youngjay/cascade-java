package com.dianping.cascade;

import com.dianping.cascade.invocation.field.ExceptionHandler;
import com.dianping.cascade.invocation.field.PropsSupport;
import com.dianping.cascade.invocation.method.CacheableMethodInvocationInterceptorFactory;
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
    private Map<String, FieldInvocationHandler> fieldInvocationHandlerMap = Maps.newHashMap();

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

    private String generateKey(String type, String methodName) {
        return type + "." + methodName;
    }

    private void registerMethod(String type, Object target, Method method) {
        String methodName = method.getName();

        String mapKey = generateKey(type, methodName);

        if (fieldInvocationHandlerMap.containsKey(mapKey)) {
            throw new RuntimeException(mapKey + " has already registered");
        }

        fieldInvocationHandlerMap.put(mapKey, buildFieldInvocationHandler(method, target));
    }

    private MethodInvocationHandler createMethodInvocationHandler(final Method method, final Object target) {
        List<MethodInvocationInterceptor> methodInvocationInterceptors = Lists.newArrayList();

        methodInvocationInterceptors.add(new MethodInvocationInterceptor() {
            @Override
            public Object invoke(MethodInvocationHandler handler, Object[] args) throws Exception {
                return method.invoke(target, args);
            }
        });

        List<MethodInvocationInterceptorFactory> methodInvocationInterceptorFactories = config.getMethodInvocationInterceptorFactories();

        if (methodInvocationInterceptorFactories == null) {
            methodInvocationInterceptorFactories = Lists.newArrayList();
        }

        methodInvocationInterceptorFactories.add(new CacheableMethodInvocationInterceptorFactory());

        for (MethodInvocationInterceptorFactory methodInvocationInterceptorFactory : methodInvocationInterceptorFactories) {
            MethodInvocationInterceptor interceptor = methodInvocationInterceptorFactory.create(method, target);
            if (interceptor != null) {
                methodInvocationInterceptors.add(interceptor);
            }
        }

        return createMethodInvocationHandlerFromInterceptors(methodInvocationInterceptors);
    }

    private FieldInvocationHandler createFieldInvocationHandler(final MethodInvocationHandler methodInvocationHandler, final ParameterResolvers parameterResolvers) {

        List<FieldInvocationInterceptor> fieldInvocationInterceptors = Lists.newArrayList();

        fieldInvocationInterceptors.add(new FieldInvocationInterceptor() {
            @Override
            public Object invoke(FieldInvocationHandler fieldInvocationHandler, Field field, ContextParams contextParams) {
                try {
                    return methodInvocationHandler.invoke(parameterResolvers.resolve(contextParams).toArray());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        fieldInvocationInterceptors.add(new PropsSupport());

        if (config.getFieldInvocationInterceptors() != null) {
            fieldInvocationInterceptors.addAll(config.getFieldInvocationInterceptors());
        }

        fieldInvocationInterceptors.add(new ExceptionHandler());

        return createFieldInvocationHandlerFromInterceptors(fieldInvocationInterceptors);
    }

    public FieldInvocationHandler buildFieldInvocationHandler(Method method, Object target) {
        return createFieldInvocationHandler(createMethodInvocationHandler(method, target), new ParameterResolvers(method));
    }

    private FieldInvocationHandler createFieldInvocationHandlerFromInterceptors(List<FieldInvocationInterceptor> interceptors) {
        FieldInvocationHandler last = null;
        for (final FieldInvocationInterceptor interceptor : interceptors) {
            final FieldInvocationHandler prev = last;
            last = new FieldInvocationHandler() {
                @Override
                public Object invoke(Field field, ContextParams contextParams) {
                    return interceptor.invoke(prev, field, contextParams);
                }
            };
        }

        return last;
    }

    private MethodInvocationHandler createMethodInvocationHandlerFromInterceptors(List<MethodInvocationInterceptor> interceptors) {
        MethodInvocationHandler last = null;
        for (final MethodInvocationInterceptor interceptor : interceptors) {
            final MethodInvocationHandler prev = last;
            last = new MethodInvocationHandler() {
                @Override
                public Object invoke(Object[] args) throws Exception {
                    return interceptor.invoke(prev, args);
                }
            };
        }

        return last;
    }

    public FieldInvocationHandler getFieldInvocationHandler() {
        return new FieldInvocationHandler() {
            @Override
            public Object invoke(Field field, ContextParams contextParams) {
                String mapKey = generateKey(field.getType(), field.getCategory());
                FieldInvocationHandler fieldInvocationHandler = fieldInvocationHandlerMap.get(mapKey);

                if (fieldInvocationHandler == null) {
                    throw new RuntimeException(mapKey + " has not registered");
                }

                return fieldInvocationHandler.invoke(field, contextParams);
            }
        };
    }
}
