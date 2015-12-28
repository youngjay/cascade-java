package com.dianping.cascade;

/**
 * Created by yangjie on 12/5/15.
 */
public interface FieldInvoker {
    Object invoke(Field field, ContextParams contextParams);
}
