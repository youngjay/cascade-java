package com.dianping.cascade;

import com.dianping.cascade.invoker.DefaultInvoker;
import com.dianping.cascade.reducer.DefaultReducer;

import java.util.Collection;
import java.util.Map;

/**
 * Created by yangjie on 9/22/15.
 */
public class Cascade {
    private Registry registry;
    private Reducer reducer;
    private Invoker invoker;

    public Cascade() {
        registry = new Registry();
        reducer  = new DefaultReducer();
        invoker = new DefaultInvoker(registry);
    }

    public Map process(Collection<Field> fields, Map contextParams) {
        return reducer.reduce(fields, ContextParams.create(contextParams), invoker);
    }

    public void register(Object bean) {
        registry.register(bean.getClass().getSimpleName(), bean);
    }
}
