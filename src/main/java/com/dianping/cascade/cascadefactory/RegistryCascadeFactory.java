package com.dianping.cascade.cascadefactory;

import com.dianping.cascade.*;
import com.dianping.cascade.invoker.PropsSupport;
import com.dianping.cascade.invoker.RegistryInvoker;
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
        Invoker invoker = new PropsSupport(new RegistryInvoker(registry));
        final Reducer reducer = new SerialReducer(invoker);

        return new Cascade() {
            @Override
            public Map process(List<Field> fields, Map contextParams) {
                return reducer.reduce(fields, ContextParams.create(contextParams));
            }
        };
    }
}
