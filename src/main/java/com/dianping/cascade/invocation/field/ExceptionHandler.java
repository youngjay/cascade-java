package com.dianping.cascade.invocation.field;

import com.dianping.cascade.*;
import com.dianping.cascade.FieldInvocationHandler;
import com.dianping.cascade.FieldInvocationInterceptor;
import lombok.AllArgsConstructor;

/**
 * Created by yangjie on 1/3/16.
 */
@AllArgsConstructor
public class ExceptionHandler implements FieldInvocationInterceptor {
    @Override
    public Object invoke(FieldInvocationHandler fieldInvocationHandler, Field field, ContextParams contextParams) {
        try {
            return fieldInvocationHandler.invoke(field, contextParams);
        } catch (Throwable ex) {
            Throwable cause = getCause(ex);

            if (cause instanceof BusinessException) {
                return String.format("[Cascade Error] %s", cause.getMessage());
            }

            String msg = cause.getMessage();

            if (msg == null) {
                msg = cause.getClass().getSimpleName();
            }

            return String.format("[Cascade Error] [%s.%s] %s", field.getType(), field.getCategory(), msg);
        }
    }

    private Throwable getCause(Throwable outer) {
        Throwable inner = outer.getCause();
        return inner == null ? outer : getCause(inner);
    }
}
