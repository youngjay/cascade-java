package com.dianping.cascade;

import java.lang.reflect.Method;

/**
 * Created by yangjie on 12/28/15.
 */
public interface MethodInvoker {
    Object invoke(Object target, Method method, Object[] args) throws Exception;
}
