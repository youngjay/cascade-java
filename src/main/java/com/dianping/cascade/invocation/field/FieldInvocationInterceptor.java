package com.dianping.cascade.invocation.field;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.Field;

/**
 * Created by yangjie on 12/5/15.
 */
public interface FieldInvocationInterceptor {
    Object invoke(FieldInvocationHandler fieldInvocationHandler, Field field, ContextParams contextParams);
}
