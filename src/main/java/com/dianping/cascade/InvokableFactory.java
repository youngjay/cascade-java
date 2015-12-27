package com.dianping.cascade;

import com.dianping.cascade.invokable.DefaultInvokable;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 12/27/15.
 */
public class InvokableFactory {
    Invokable create(Object target, Method method) {
        return new DefaultInvokable(target, method, new ParameterResolvers(method));
    }


}
