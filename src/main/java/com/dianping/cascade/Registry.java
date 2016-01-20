package com.dianping.cascade;

import com.dianping.cascade.invocation.field.FieldInvocationHandler;
import com.dianping.cascade.invocation.field.FieldInvocationInterceptor;
import com.dianping.cascade.invocation.field.impl.ExceptionHandler;
import com.dianping.cascade.invocation.field.impl.PropsSupport;
import com.dianping.cascade.invocation.method.MethodInvocationHandler;
import com.dianping.cascade.invocation.method.MethodInvocationInterceptor;
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


    public FieldInvocationHandler buildFieldInvocationHandler(final Method method, final Object target) {
        final ParameterResolvers parameterResolvers = new ParameterResolvers(method);


        List<MethodInvocationInterceptor> methodInvocationInterceptors = Lists.newArrayList();

        methodInvocationInterceptors.add(new MethodInvocationInterceptor() {
            @Override
            public Object invoke(MethodInvocationHandler handler, Object[] args) throws Exception {
                return method.invoke(target, args);
            }
        });

        List<MethodInvocationInterceptorFactory> methodInvocationInterceptorFactories = config.getMethodInvocationInterceptorFactories();

        if (methodInvocationInterceptorFactories != null) {
            for (MethodInvocationInterceptorFactory methodInvocationInterceptorFactory : methodInvocationInterceptorFactories) {
                MethodInvocationInterceptor interceptor = methodInvocationInterceptorFactory.create(method, target);
                if (interceptor != null) {
                    methodInvocationInterceptors.add(interceptor);
                }
            }
        }

        final MethodInvocationHandler methodInvocationHandler = createMethodInvocationHandler(methodInvocationInterceptors);


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


        List<FieldInvocationInterceptorFactory> fieldInvocationInterceptorFactories = config.getFieldInvocationInterceptorFactories();

        if (fieldInvocationInterceptorFactories != null) {
            for (FieldInvocationInterceptorFactory fieldInvocationInterceptorFactory : fieldInvocationInterceptorFactories) {
                FieldInvocationInterceptor interceptor = fieldInvocationInterceptorFactory.create();
                if (interceptor != null) {
                    fieldInvocationInterceptors.add(interceptor);
                }
            }
        }

        fieldInvocationInterceptors.add(new PropsSupport());
        fieldInvocationInterceptors.add(new ExceptionHandler());

        return createFieldInvocationHandler(fieldInvocationInterceptors);

















//        final List<FieldInvocationInterceptor> interceptors = Lists.newArrayList();
//
//        final InvocationTarget invocationTarget = new InvocationTarget(method, target, config);
//
//        interceptors.add(new FieldInvocationInterceptor() {
//            @Override
//            public Object invoke(FieldInvocationHandler nothing, Field field, ContextParams contextParams) {
//                try {
//                    return invocationTarget.invoke(contextParams);
//                } catch (Exception ex) {
//                    throw new RuntimeException(ex);
//                }
//            }
//        });
//
//
//        interceptors.addAll(createFieldInvocationInterceptors(method, target));
//
//        FieldInvocationHandler fieldInvocationHandler = createFieldInvocationHandler(interceptors);
//        return fieldInvocationHandler;
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

    private MethodInvocationHandler createMethodInvocationHandler(List<MethodInvocationInterceptor> interceptors) {
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
