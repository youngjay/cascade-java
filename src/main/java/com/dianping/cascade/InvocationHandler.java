package com.dianping.cascade;

/**
 * Created by yangjie on 1/11/16.
 */
public interface InvocationHandler {
    Object invoke(Field field, ContextParams contextParams);
}
