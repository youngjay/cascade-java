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
        return registry.get(field.getType(), field.getCategory()).invoke(contextParams);
    }
}
