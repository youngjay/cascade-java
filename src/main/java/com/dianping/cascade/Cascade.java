package com.dianping.cascade;

import com.dianping.cascade.invoker.PropsPicker;
import com.dianping.cascade.invoker.RegistryInvoker;
import com.dianping.cascade.reducer.SerialReducer;

import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 9/22/15.
 */
public class Cascade {
    private Registry registry;
    private Reducer reducer;

    public Cascade() {
        registry = new Registry();
        Invoker invoker = new PropsPicker(new RegistryInvoker(registry));
        reducer = new SerialReducer(invoker);
    }

    public Map reduce(List<Field> fields, Map contextParams) {
        return reducer.reduce(fields, ContextParams.create(contextParams));
    }

    public void register(Object bean) {
        registry.register(bean);
    }

}
