package com.dianping.cascade;

import com.dianping.cascade.invoker.DefaultInvoker;
import com.dianping.cascade.reducer.ParallelReducer;
import com.dianping.cascade.reducer.SerialReducer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 9/22/15.
 */
public class Cascade {
    private Registry registry;
    public Cascade() {
        registry = new Registry();
    }

    public Map process(List<Field> fields, Map contextParams) {
        Invoker invoker = new DefaultInvoker(registry);
        Reducer reducer  = new ParallelReducer(invoker);

        return reducer.reduce(fields, ContextParams.create(contextParams));
    }

    public void register(Object bean) {
        registry.register(bean.getClass().getSimpleName(), bean);
    }
}
