package com.dianping.cascade;

import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Created by yangjie on 12/5/15.
 */
public class Registry {
    private Map<String, Invokable> invokableMap = Maps.newHashMap();

    public void register(String type, Object bean) {
       for (Method method : bean.getClass().getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                registerMethod(type, bean, method);
            }
       }
    }

    public Invokable get(String type, String methodName) {
        String mapKey = generateKey(type, methodName);
        Invokable invokable = invokableMap.get(mapKey);
        if (invokable == null) {
            throw new RuntimeException(mapKey + " has not registered");
        }
        return invokable;
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

        invokableMap.put(mapKey, new Invokable(target, method));
    }

}
