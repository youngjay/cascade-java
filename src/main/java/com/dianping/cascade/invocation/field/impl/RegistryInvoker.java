package com.dianping.cascade.invocation.field.impl;

import com.dianping.cascade.*;
import com.dianping.cascade.invocation.field.FieldInvocationHandler;
import com.dianping.cascade.invocation.field.FieldInvocationInterceptor;
import lombok.AllArgsConstructor;

/**
 * Created by yangjie on 12/5/15.
 */
@AllArgsConstructor
public class RegistryInvoker implements FieldInvocationInterceptor {
    private Registry registry;

    @Override
    public Object invoke(FieldInvocationHandler invocationHandler, Field field, ContextParams contextParams) {
        try {
            return registry.get(field.getType(), field.getCategory()).invoke(contextParams);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
