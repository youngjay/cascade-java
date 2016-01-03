package com.dianping.cascade.cascadefactory;

import com.dianping.cascade.*;
import com.dianping.cascade.invoker.field.PropsSupport;
import com.dianping.cascade.invoker.field.RegistryFieldInvoker;
import com.dianping.cascade.reducer.ParallelReducer;
import com.dianping.cascade.reducer.SerialReducer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yangjie on 12/15/15.
 */
public class RegistryCascadeFactory implements CascadeFactory {
    private Registry registry;
    private FieldInvoker fieldInvoker;
    private ExecutorService executorService = Executors.newFixedThreadPool(100);

    public RegistryCascadeFactory(Registry registry) {
        this.registry = registry;
        fieldInvoker = new PropsSupport(new RegistryFieldInvoker(registry));
    }

    @Override
    public Cascade create() {
        return new Cascade() {
            @Override
            public Map process(List<Field> fields, Map contextParams) {
                final Reducer reducer = new ParallelReducer(fieldInvoker, executorService);
//                final Reducer reducer = new SerialReducer(fieldInvoker);
                return reducer.reduce(fields, ContextParams.create(contextParams));
            }
        };
    }
}
