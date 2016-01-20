package com.dianping.cascade.invocation.method;

/**
 * Created by yangjie on 12/28/15.
 */
public interface MethodInvocationHandler {
    Object invoke(Object[] args) throws Exception;
}
