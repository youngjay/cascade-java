package com.dianping.cascade;

import com.dianping.cascade.annotation.Cacheable;
import com.dianping.cascade.invokable.CacheableInvokable;
import com.dianping.cascade.invokable.DefaultInvokable;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 12/27/15.
 */
public class InvokableFactory {
    Invokable create(Object target, Method method) {
        ParameterResolvers parameterResolvers = new ParameterResolvers(method);

        if (method.getAnnotation(Cacheable.class) != null) {
            return new CacheableInvokable(target, method, parameterResolvers, method.getAnnotation(Cacheable.class));
        }

        return new DefaultInvokable(target, method, parameterResolvers);
    }
}
