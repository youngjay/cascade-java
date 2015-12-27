package com.dianping.cascade.invokable;

import com.dianping.cascade.*;
import com.dianping.cascade.annotation.Entity;
import com.dianping.cascade.annotation.Param;
import com.dianping.cascade.parameterresolver.EntityResolver;
import com.dianping.cascade.parameterresolver.ParamResolver;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by yangjie on 12/5/15.
 */
@AllArgsConstructor
public class DefaultInvokable implements Invokable {
    private Object target;
    private Method method;
    private ParameterResolvers parameterResolvers;

    protected Object invoke(List args) {
        try {
            return method.invoke(target, args.toArray());
        } catch (Exception ex) {
            Throwable cause = ex.getCause();

            if (cause instanceof BusinessException) {
                throw (BusinessException) cause;
            }

            String msg;

            if (cause == null) {
                msg = ex.getMessage();
            } else {
                msg = cause.getMessage();

                if (msg == null) {
                    msg = cause.getClass().getSimpleName();
                }
            }

            throw new RuntimeException(getLocation(method.getName()) + msg);
        }
    }

    @Override
    public Object invoke(final ContextParams params) {
        return invoke(parameterResolvers.resolve(params));
    }

    private String getLocation(String methodName) {
        return String.format("[%s.%s] ", target.getClass().getSimpleName(), methodName);
    }
}
