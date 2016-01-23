package com.dianping.cascade.reducer;

import com.dianping.cascade.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yangjie on 03/01/16.
 */
@CommonsLog
public class ParallelReducer implements Reducer {
    private InvocationHandler invocationHandler;
    private ExecutorService executorService;
    private BlockingQueue<Runnable> taskQueue;

    public ParallelReducer(InvocationHandler invocationHandler, ExecutorService executorService, BlockingQueue<Runnable> taskQueue) {
        this.invocationHandler = invocationHandler;
        this.executorService = executorService;
        this.taskQueue = taskQueue;
    }

    private interface CompleteNotifier {
        void emit(Object key, Object value);
    }

    private static abstract class CollectionCompleteNotifier<T> implements CompleteNotifier {
        protected T parentResults;
        private CompleteNotifier parent;
        private Object keyInParent;
        private AtomicInteger remainCount;

        protected CollectionCompleteNotifier(T parentResults, CompleteNotifier parent, Object keyInParent, int remainCount) {
            this.parentResults = parentResults;
            this.parent = parent;
            this.keyInParent = keyInParent;
            this.remainCount = new AtomicInteger(remainCount);
            if (remainCount == 0) {
                parent.emit(keyInParent, parentResults);
            }
        }

        @Override
        public void emit(Object key, Object value) {
            setData(key, value);
            if (remainCount.decrementAndGet() == 0) {
                parent.emit(keyInParent, parentResults);
            }
        }

        protected abstract void setData(Object key, Object value);
    }

    private static class MapCompleteNotifier extends CollectionCompleteNotifier<Map> {
        public MapCompleteNotifier(Map parentResults, CompleteNotifier parent, Object keyInParent, int remainCount) {
            super(parentResults, parent, keyInParent, remainCount);
        }

        protected void setData(Object key, Object value) {
            parentResults.put(key, value);
        }
    }

    private static class ListCompleteNotifier extends CollectionCompleteNotifier<List> {
        public ListCompleteNotifier(List parentResults, CompleteNotifier parent, Object keyInParent, int remainCount) {
            super(parentResults, parent, keyInParent, remainCount);
        }

        protected void setData(Object key, Object value) {
            parentResults.set((Integer) key, value);
        }
    }

    private static class RootCompleteNotifier implements CompleteNotifier {
        private Map results;
        private AtomicInteger remainCount;

        public RootCompleteNotifier(int remainCount) {
            this.remainCount = new AtomicInteger(remainCount);
            results = Maps.newHashMapWithExpectedSize(remainCount);
        }

        @Override
        public void emit(Object key, Object value) {
            results.put(key, value);
            remainCount.decrementAndGet();
        }

        public Map getResults() {
            return results;
        }
    }


    @AllArgsConstructor
    private class FieldRunner implements Runnable {
        private CompleteNotifier completeNotifier;
        private Field field;
        private ContextParams parentContextParams;

        @Override
        public void run() {
            ContextParams contextParams = parentContextParams.extend(field.getParams());

            Object result = invocationHandler.invoke(field, contextParams);

            if (field.getChildren().size() == 0 || Util.canNotHasChildren(result)) {
                completeNotifier.emit(field.getComputedAs(), result);
            } else {
                if (result instanceof Collection) {
                    List resultList = result instanceof List ? (List) result : Lists.newArrayList((Collection) result);
                    executorService.execute(new ListResultsRunner(resultList, new ListCompleteNotifier(resultList, completeNotifier, field.getComputedAs(), resultList.size()), field.getChildren(), contextParams));
                } else {
                    Map resultMap = Util.toMap(result);
                    executorService.execute(new FieldsRunner(new MapCompleteNotifier(createMapWithAdditionalSize(resultMap, field.getChildren().size()), completeNotifier, field.getComputedAs(), field.getChildren().size()), field.getChildren(), contextParams.extend(resultMap)));
                }
            }

        }
    }

    @AllArgsConstructor
    private class FieldsRunner implements Runnable {
        private CompleteNotifier completenotifier;
        private List<Field> fields;
        private ContextParams parentContextParams;

        @Override
        public void run() {
            for (Field field : fields) {
                executorService.execute(new FieldRunner(completenotifier, field, parentContextParams));
            }
        }
    }

    @AllArgsConstructor
    private class ListResultsRunner implements Runnable {
        private List parentResults;
        private CompleteNotifier completeNotifier;
        private List<Field> fields;
        private ContextParams parentContextParams;

        @Override
        public void run() {
            int index = 0;
            for (Object o : parentResults) {
                Map parentResultsMap = Util.toMap(o);
                executorService.execute(new FieldsRunner(new MapCompleteNotifier(createMapWithAdditionalSize(parentResultsMap, fields.size()), completeNotifier, index, fields.size()), fields, parentContextParams.extend(parentResultsMap)));
                ++index;
            }
        }
    }

    @Override
    public Map reduce(List<Field> fields, ContextParams contextParams) {
        RootCompleteNotifier root = new RootCompleteNotifier(fields.size());
        executorService.execute(new FieldsRunner(root, fields, contextParams));

        return waitForComplete(fields,root);
    }

    // 防止在插入child fields的时候map进行rehash，所以这里事先分配好大小
    @SuppressWarnings("unchecked")
    private Map createMapWithAdditionalSize(Map baseMap, int additionalCount) {
        Map ret = Maps.newHashMapWithExpectedSize(baseMap.size() + additionalCount);
        ret.putAll(baseMap);
        return ret;
    }


    // 顺便帮忙处理一些任务
    private Map waitForComplete(List<Field> fields, RootCompleteNotifier root) {
        // 最多重入多少次
        int maxEnterCount = 50;

        while (root.remainCount.get() != 0) {
            /* for debug */
            if (maxEnterCount > 0) {
                --maxEnterCount;
                if (maxEnterCount == 0) {
                    log.error("max run count arrived: " + fields);
                }
            }
            /* end for debug */

            Runnable runnable = null;

            try {
                runnable = taskQueue.poll(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // do nothing
            }

            if (runnable != null) {
                runnable.run();
            }
        }

        return root.getResults();
    }
}