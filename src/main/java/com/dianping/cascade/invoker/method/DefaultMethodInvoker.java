package com.dianping.cascade.invoker.method;

import com.dianping.cascade.MethodInvoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by yangjie on 12/28/15.
 */
public class DefaultMethodInvoker implements MethodInvoker {
    @Override
    public Object invoke(Object target, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }
}
