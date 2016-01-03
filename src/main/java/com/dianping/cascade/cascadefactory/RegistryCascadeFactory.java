package com.dianping.cascade.cascadefactory;

import com.dianping.cascade.*;
import com.dianping.cascade.invoker.field.PropsSupport;
import com.dianping.cascade.invoker.field.RegistryFieldInvoker;
import com.dianping.cascade.reducer.ParallelReducer;
import com.dianping.cascade.reducer.SerialReducer;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by yangjie on 12/15/15.
 */
@AllArgsConstructor
public class RegistryCascadeFactory implements CascadeFactory {
    private Registry registry;
    private CascadeFactoryConfig config;

    @Override
    public Cascade create() {
        final FieldInvoker fieldInvoker = new PropsSupport(new RegistryFieldInvoker(registry));

        final Reducer reducer = config.getThreadCount() > 1 ?
                new ParallelReducer(fieldInvoker, Executors.newFixedThreadPool(config.getThreadCount())) :
                new SerialReducer(fieldInvoker);

        return new Cascade() {
            @Override
            public Map process(List<Field> fields, Map contextParams) {
                return reducer.reduce(fields, ContextParams.create(contextParams));
            }
        };
    }
}
