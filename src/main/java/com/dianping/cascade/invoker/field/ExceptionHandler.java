package com.dianping.cascade.invoker.field;

import com.dianping.cascade.BusinessException;
import com.dianping.cascade.ContextParams;
import com.dianping.cascade.Field;
import com.dianping.cascade.FieldInvoker;
import lombok.AllArgsConstructor;

/**
 * Created by yangjie on 1/3/16.
 */
@AllArgsConstructor
public class ExceptionHandler implements FieldInvoker {
    private FieldInvoker fieldInvoker;

    @Override
    public Object invoke(Field field, ContextParams contextParams) {
        try {
            return fieldInvoker.invoke(field, contextParams);
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
