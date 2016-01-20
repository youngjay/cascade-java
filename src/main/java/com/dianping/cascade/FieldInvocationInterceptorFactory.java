package com.dianping.cascade;

import com.dianping.cascade.invocation.field.FieldInvocationInterceptor;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 1/20/16.
 */
public interface FieldInvocationInterceptorFactory {
    FieldInvocationInterceptor create();
}
