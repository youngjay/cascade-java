package com.dianping.cascade.cascadefactory;

import com.dianping.cascade.*;
import com.dianping.cascade.reducer.ParallelReducer;
import com.dianping.cascade.reducer.SerialReducer;
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
    private Reducer reducer;

    public RegistryCascadeFactory(Registry registry, CascadeFactoryConfig config) {
        reducer =  createReducer(registry.getInvocationHandler(), config.getThreadCount());
    }

    private Reducer createReducer(InvocationHandler invocationHandler, int threadCount) {
        if (threadCount> 1) {
            BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>(threadCount);

            return new ParallelReducer(invocationHandler, new ThreadPoolExecutor(
                    threadCount,
                    threadCount,
                    0L,
                    TimeUnit.MILLISECONDS,
                    taskQueue, // 额外接受1倍的task，拍脑袋定的，可以优化
                    new ThreadFactoryBuilder().setNameFormat("cascade-%d").build(),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            ), taskQueue);

        } else {
            return new SerialReducer(invocationHandler);
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
