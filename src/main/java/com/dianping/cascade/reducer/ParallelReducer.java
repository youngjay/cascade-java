package com.dianping.cascade.reducer;

import com.dianping.cascade.*;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by yangjie on 12/5/15.
 *
 * WRONG IMPLEMENT
 */
public class ParallelReducer implements Reducer {
    private Invoker invoker;
    ExecutorService executorService;

    private static final String CASCADE_ERROR = "[Cascade Error] ";


    public ParallelReducer(Invoker invoker) {
        this.invoker = invoker;
        this.executorService = Executors.newFixedThreadPool(30);
    }

    @Override
    public Map reduce(List<Field> fields, final ContextParams contextParams) {

        Collection<Callable<Object>> callables = Collections2.transform(fields, new Function<Field, Callable<Object>>() {
            @Override
            public Callable<Object> apply(final Field field) {
                return new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        return reduceField(field, contextParams);
                    }
                };
            }
        });

        List<Future<Object>> futures;

        try {
            futures = executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<Object> results = Lists.transform(futures, new Function<Future<Object>, Object>() {
            @Override
            public Object apply(Future<Object> input) {
                try {
                    return input.get();
                } catch (Exception ex) {
                    return CASCADE_ERROR + ex.getMessage();
                }
            }
        });

        int i = 0;
        Map ret = Maps.newHashMap();
        for (Field field : fields) {
            ret.put(field.getComputedAs(), results.get(i));
            i++;
        }

        return ret;
    }




    private Object reduceField(final Field field, ContextParams parentContextParams) {

        final ContextParams contextParams = parentContextParams.extend(field.getParams());

        Object result;

        try {
            result = invoker.invoke(field, contextParams);
        } catch (Exception ex) {
            return CASCADE_ERROR + ex.getMessage();
        }

        if (CollectionUtils.isEmpty(field.getChildren()) || result == null) {
            return result;
        }

        if (result instanceof List) {
                List<Callable<Map>> callables = Lists.transform((List<Object>) result, new Function<Object, Callable<Map>>() {
                    @Override
                    public Callable<Map> apply(final Object input) {
                        return new Callable<Map>() {
                            @Override
                            public Map call() throws Exception {
                                return processFieldsWithResults(input, field.getChildren(), contextParams);
                            }
                        };
                    }
                });

                List<Future<Map>> futures;

                try {
                    futures = executorService.invokeAll(callables);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return Lists.transform(futures, new Function<Future<Map>, Object>() {
                    @Override
                    public Object apply(Future<Map> input) {
                        try {
                            return input.get();
                        } catch (Exception ex) {
                            return CASCADE_ERROR + ex.getMessage();
                        }
                    }
                });
        } else {
            return processFieldsWithResults(result, field.getChildren(), contextParams);
        }

    }

    @SuppressWarnings("unchecked")
    private Map processFieldsWithResults(Object result, List<Field> fields, ContextParams parentContextParams) {
        Map resultMap = Util.toMap(result);
        ContextParams contextParams = parentContextParams.extend(resultMap);
        Map subResultMap = reduce(fields, contextParams);
        resultMap.putAll(subResultMap);
        return resultMap;
    }
}
