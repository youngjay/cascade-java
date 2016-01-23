package com.dianping.cascade;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 1/23/16.
 */
public interface InvocationInterceptorFactory {
    InvocationInterceptor create(Method method, Object target);
}
