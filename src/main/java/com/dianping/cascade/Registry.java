package com.dianping.cascade;

import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Created by yangjie on 12/5/15.
 */
public class Registry {
    private Map<String, ContextParamsInvoker> invokableMap = Maps.newHashMap();

    private ContextParamsInvokerFactory contextParamsInvokerFactory = new ContextParamsInvokerFactory();

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

    public ContextParamsInvoker get(String type, String methodName) {
        String mapKey = generateKey(type, methodName);
        ContextParamsInvoker contextParamsInvoker = invokableMap.get(mapKey);
        if (contextParamsInvoker == null) {
            throw new RuntimeException(mapKey + " has not registered");
        }
        return contextParamsInvoker;
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

        invokableMap.put(mapKey, contextParamsInvokerFactory.create(target, method));
    }

}
