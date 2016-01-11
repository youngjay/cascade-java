package com.dianping.cascade.invocation.field;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.Field;

/**
 * Created by yangjie on 1/11/16.
 */
public interface FieldInvocationHandler {
    Object invoke(Field field, ContextParams contextParams);
}
