package com.dianping.cascade;

import com.dianping.cascade.invocation.field.FieldInvocationHandler;
import com.dianping.cascade.invocation.field.FieldInvocationInterceptor;
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
    private final Map<String, InvocationTarget> invocationTargetMap = Maps.newHashMap();
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

        fieldInvocationHandlerMap.put(mapKey, createFieldInvocationHandler(method, target));
    }

    private List<FieldInvocationInterceptor> createFieldInvocationInterceptors(Method method, Object target) {
        List<FieldInvocationInterceptor> interceptors = Lists.newArrayList();
        for (FieldInvocationInterceptorFactory factory : config.getFieldInvocationInterceptorFactories()) {
            FieldInvocationInterceptor interceptor = factory.create(method, target);
            if (interceptor != null) {
                interceptors.add(interceptor);
            }
        }
        return interceptors;
    }

    public FieldInvocationHandler createFieldInvocationHandler(Method method, Object target) {
        final List<FieldInvocationInterceptor> interceptors = Lists.newArrayList();

        final InvocationTarget invocationTarget = new InvocationTarget(method, target);

        interceptors.add(new FieldInvocationInterceptor() {
            @Override
            public Object invoke(FieldInvocationHandler nothing, Field field, ContextParams contextParams) {
                try {
                    return invocationTarget.invoke(contextParams);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


        interceptors.addAll(createFieldInvocationInterceptors(method, target));

        FieldInvocationHandler fieldInvocationHandler = createFieldInvocationHandler(interceptors);
        return fieldInvocationHandler;
    }

    private FieldInvocationHandler createFieldInvocationHandler(List<FieldInvocationInterceptor> interceptors) {
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


    private static class InvocationTarget {
        private Method method;
        private Object target;
        private ParameterResolvers parameterResolvers;

        public InvocationTarget(Method method, Object target) {
            this.method = method;
            this.target = target;
            this.parameterResolvers = new ParameterResolvers(method);
        }

        public Object invoke(ContextParams params) throws Exception {
            return method.invoke(target, parameterResolvers.resolve(params).toArray());
        }
    }

}
