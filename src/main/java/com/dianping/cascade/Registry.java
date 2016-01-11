package com.dianping.cascade;

import com.dianping.cascade.annotation.Cacheable;
import com.dianping.cascade.invocation.method.impl.CacheableMethodInvocationHandler;
import com.dianping.cascade.invocation.method.impl.DefaultMethodInvocationHandler;
import com.dianping.cascade.invocation.method.MethodInvocationHandler;
import com.dianping.cascade.invocation.params.ContextParamsInvocationHandler;
import com.dianping.cascade.invocation.params.impl.DefaultContextParamsInvocationHandler;
import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Created by yangjie on 12/5/15.
 */
public class Registry {
    private Map<String, ContextParamsInvocationHandler> invokableMap = Maps.newHashMap();

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

    public ContextParamsInvocationHandler get(String type, String methodName) {
        String mapKey = generateKey(type, methodName);
        ContextParamsInvocationHandler contextParamsInvocationHandler = invokableMap.get(mapKey);
        if (contextParamsInvocationHandler == null) {
            throw new RuntimeException(mapKey + " has not registered");
        }
        return contextParamsInvocationHandler;
    }

    private String generateKey(String type, String methodName) {
        return type + "." + methodName;
    }

    private void registerMethod(String type, Object target, Method method) {
        String methodName = method.getName();

        String mapKey = generateKey(type, methodName);

        if (invokableMap.containsKey(mapKey)) {
            throw new RuntimeException(mapKey + " has already registered");
        }

        invokableMap.put(mapKey, new DefaultContextParamsInvocationHandler(target, method));
    }
}
