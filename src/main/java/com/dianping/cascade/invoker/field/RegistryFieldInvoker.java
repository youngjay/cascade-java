package com.dianping.cascade.invoker.field;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.Field;
import com.dianping.cascade.FieldInvoker;
import com.dianping.cascade.Registry;
import lombok.AllArgsConstructor;

/**
 * Created by yangjie on 12/5/15.
 */
@AllArgsConstructor
public class RegistryFieldInvoker implements FieldInvoker {
    private Registry registry;

    @Override
    public Object invoke(Field field, ContextParams contextParams) {
        try {
            return registry.get(field.getType(), field.getCategory()).invoke(contextParams);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
