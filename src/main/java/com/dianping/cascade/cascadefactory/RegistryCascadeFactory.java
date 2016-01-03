package com.dianping.cascade.cascadefactory;

import com.dianping.cascade.*;
import com.dianping.cascade.invoker.field.PropsSupport;
import com.dianping.cascade.invoker.field.RegistryFieldInvoker;
import com.dianping.cascade.reducer.ParallelReducer;
import com.dianping.cascade.reducer.SerialReducer;

import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 12/15/15.
 */
public class RegistryCascadeFactory implements CascadeFactory {
    private Registry registry;

    public RegistryCascadeFactory(Registry registry) {
        this.registry = registry;
    }

    @Override
    public Cascade create() {
        return new Cascade() {
            @Override
            public Map process(List<Field> fields, Map contextParams) {

                FieldInvoker fieldInvoker = new RegistryFieldInvoker(registry);
//                final Reducer reducer = new ParallelReducer(fieldInvoker);
                final Reducer reducer = new SerialReducer(fieldInvoker);
                return reducer.reduce(fields, ContextParams.create(contextParams));
            }
        };
    }
}
