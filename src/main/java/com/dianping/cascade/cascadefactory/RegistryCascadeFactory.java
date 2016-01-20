package com.dianping.cascade.cascadefactory;

import com.dianping.cascade.*;
import com.dianping.cascade.invocation.field.FieldInvocationHandler;
import com.dianping.cascade.invocation.field.FieldInvocationInterceptor;
import com.dianping.cascade.invocation.field.impl.*;
import com.dianping.cascade.reducer.impl.ParallelReducer;
import com.dianping.cascade.reducer.Reducer;
import com.dianping.cascade.reducer.impl.SerialReducer;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangjie on 12/15/15.
 */
public class RegistryCascadeFactory implements CascadeFactory {
    private Registry registry;
    private CascadeFactoryConfig config;
    private Reducer reducer;

    public RegistryCascadeFactory(Registry registry, CascadeFactoryConfig config) {
        this.registry = registry;
        this.config = config;

        reducer =  createReducer(registry.getFieldInvocationHandler(), config.getThreadCount());
    }


//    private FieldInvocationHandler createFieldInvocationHandler() {
//
//        List<FieldInvocationInterceptor> fieldInvocationInterceptors = Lists.newArrayList();
//
//        fieldInvocationInterceptors.add(new RegistryInvoker(registry));
//        fieldInvocationInterceptors.add(new PropsSupport());
//
//        if (config.getFieldInvocationInterceptors() != null) {
//            fieldInvocationInterceptors.addAll(config.getFieldInvocationInterceptors());
//        }
//
//        fieldInvocationInterceptors.add(new ExceptionHandler());
//
//        FieldInvocationHandler last = null;
//        for (final FieldInvocationInterceptor interceptor : fieldInvocationInterceptors) {
//            final FieldInvocationHandler prev = last;
//            last = new FieldInvocationHandler() {
//                @Override
//                public Object invoke(Field field, ContextParams contextParams) {
//                    return interceptor.invoke(prev, field, contextParams);
//                }
//            };
//        }
//
//        return last;
//    }

    private Reducer createReducer(FieldInvocationHandler fieldInvocationHandler, int threadCount) {
        if (threadCount> 1) {
            BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>(threadCount);

            return new ParallelReducer(fieldInvocationHandler, new ThreadPoolExecutor(
                    threadCount,
                    threadCount,
                    0L,
                    TimeUnit.MILLISECONDS,
                    taskQueue, // 额外接受1倍的task，拍脑袋定的，可以优化
                    new ThreadFactoryBuilder().setNameFormat("cascade-%d").build(),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            ), taskQueue);

        } else {
            return new SerialReducer(fieldInvocationHandler);
        }
    }

    @Override
    public Cascade create() {
        return new Cascade() {
            @Override
            public Map process(List<Field> fields, Map contextParams) {
                return reducer.reduce(fields, ContextParams.create(contextParams));
            }
        };
    }
}
