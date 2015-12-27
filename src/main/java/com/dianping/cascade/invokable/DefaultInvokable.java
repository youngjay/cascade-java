package com.dianping.cascade.invokable;

import com.dianping.cascade.*;
import lombok.AllArgsConstructor;

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

    protected Object invokeByArgs(List args) {
        try {
            return method.invoke(target, args.toArray());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object invoke(ContextParams params) {
        try {
            return invokeByArgs(parameterResolvers.resolve(params));
        } catch (Exception ex) {
            Throwable cause = getCause(ex);

            if (cause instanceof BusinessException) {
                throw (BusinessException) cause;
            }

            String msg = cause.getMessage();

            if (msg == null) {
                msg = cause.getClass().getSimpleName();
            }

            throw new RuntimeException(getLocation(method.getName()) + msg);
        }
    }

    private Throwable getCause(Throwable outer) {
        Throwable inner = outer.getCause();
        return inner == null ? outer : getCause(inner);
    }

    private String getLocation(String methodName) {
        return String.format("[%s.%s] ", target.getClass().getSimpleName(), methodName);
    }
}
