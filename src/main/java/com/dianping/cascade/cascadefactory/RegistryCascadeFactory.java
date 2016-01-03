package com.dianping.cascade.cascadefactory;

import com.dianping.cascade.*;
import com.dianping.cascade.invoker.field.PropsSupport;
import com.dianping.cascade.invoker.field.RegistryFieldInvoker;
import com.dianping.cascade.reducer.ParallelReducer;
import com.dianping.cascade.reducer.SerialReducer;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
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

        if (config.getThreadCount() > 1) {
            // all cascades share one thread pool
            final ExecutorService executorService = Executors.newFixedThreadPool(config.getThreadCount());

            return new Cascade() {
                @Override
                public Map process(List<Field> fields, Map contextParams) {
                    return (new ParallelReducer(fieldInvoker, executorService)).reduce(fields, ContextParams.create(contextParams));
                }
            };
        } else {
            return new Cascade() {
                @Override
                public Map process(List<Field> fields, Map contextParams) {
                    return (new SerialReducer(fieldInvoker)).reduce(fields, ContextParams.create(contextParams));
                }
            };
        }
    }
}
