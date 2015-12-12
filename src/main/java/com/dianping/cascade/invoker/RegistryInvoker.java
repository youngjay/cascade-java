package com.dianping.cascade.invoker;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.Field;
import com.dianping.cascade.Invoker;
import com.dianping.cascade.Registry;
import lombok.AllArgsConstructor;

/**
 * Created by yangjie on 12/5/15.
 */
@AllArgsConstructor
public class RegistryInvoker implements Invoker {
    private Registry registry;

    @Override
    public Object invoke(Field field, ContextParams contextParams) {
        return registry.get(field.getType(), field.getCategory()).invoke(contextParams);
    }
}
