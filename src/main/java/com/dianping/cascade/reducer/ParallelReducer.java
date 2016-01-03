package com.dianping.cascade.reducer;

import com.dianping.cascade.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yangjie on 03/01/16.
 */
public class ParallelReducer implements Reducer {
    private FieldInvoker fieldInvoker;
    private ExecutorService executorService;

    private Map<Object, CompleteNotifier> completeNotifierMap = Maps.newConcurrentMap();

    private final Object lock = new Object();
    private volatile boolean isComplete = false;

    private static final String CASCADE_ERROR = "[Cascade Error] ";

    public ParallelReducer(FieldInvoker fieldInvoker, ExecutorService executorService) {
        this.fieldInvoker = fieldInvoker;
        this.executorService = executorService;
    }

    private interface CompleteNotifier {
        void emit(Object key, Object value);
    }

    private abstract class CompleteNotifierBase<T> implements CompleteNotifier {
        protected T parentResults;
        private List parentPath;
        private Object keyInParent;
        private AtomicInteger remainCount;

        protected CompleteNotifierBase(T parentResults, List parentPath, Object keyInParent, int remainCount) {
            this.parentResults = parentResults;
            this.parentPath = parentPath;
            this.keyInParent = keyInParent;
            this.remainCount = new AtomicInteger(remainCount);
        }

        @Override
        public void emit(Object key, Object value) {
            setData(key, value);
            if (remainCount.decrementAndGet() == 0) {
                CompleteNotifier parent = completeNotifierMap.get(parentPath);
                parent.emit(keyInParent, parentResults);
            }
        }

        protected abstract void setData(Object key, Object value);
    }

    private class MapCompleteNotifier extends CompleteNotifierBase<Map> {
        public MapCompleteNotifier(Map parentResults, List parentPath, Object keyInParent, int remainCount) {
            super(parentResults, parentPath, keyInParent, remainCount);
        }

        protected void setData(Object key, Object value) {
            parentResults.put(key, value);
        }
    }

    private class ListCompleteNotifier extends CompleteNotifierBase<List> {
        public ListCompleteNotifier(List parentResults, List parentPath, Object keyInParent, int remainCount) {
            super(parentResults, parentPath, keyInParent, remainCount);
        }

        protected void setData(Object key, Object value) {
            parentResults.set((Integer) key, value);
        }
    }

    private class RootCompleteNotifier implements CompleteNotifier {
        private Map results;
        private AtomicInteger remainCount;

        public RootCompleteNotifier(int remainCount) {
            this.remainCount = new AtomicInteger(remainCount);
            results = Maps.newHashMapWithExpectedSize(remainCount);
        }

        @Override
        public void emit(Object key, Object value) {
            results.put(key, value);
            if (remainCount.decrementAndGet() == 0) {
                synchronized (lock) {
                    isComplete = true;
                    lock.notifyAll();
                }
            }
        }

        public Map getResults() {
            return results;
        }
    }


    @AllArgsConstructor
    private class FieldRunner implements Runnable {
        private List parentPath;
        private Field field;
        private ContextParams parentContextParams;

        @Override
        public void run() {
            Object result;

            ContextParams contextParams = parentContextParams.extend(field.getParams());

            try {
                result = fieldInvoker.invoke(field, contextParams);
            } catch (Exception ex) {
                result = CASCADE_ERROR + ex.getMessage();
            }

            if (CollectionUtils.isEmpty(field.getChildren()) || result == null) {
                completeNotifierMap.get(parentPath).emit(field.getComputedAs(), result);
            } else {
                List path = Lists.newArrayList(parentPath);
                path.add(field.getComputedAs());

                if (result instanceof List) {
                    List resultList = (List) result;
                    completeNotifierMap.put(path, new ListCompleteNotifier(resultList, parentPath, field.getComputedAs(), resultList.size()));
                    executorService.execute(new ListResultsRunner(resultList, path, field.getChildren(), contextParams));
                } else {
                    Map resultMap = Util.toMap(result);
                    completeNotifierMap.put(path, new MapCompleteNotifier(resultMap, parentPath, field.getComputedAs(), field.getChildren().size()));
                    executorService.execute(new FieldsRunner(path, field.getChildren(), contextParams.extend(resultMap)));
                }
            }

        }
    }

    @AllArgsConstructor
    private class FieldsRunner implements Runnable {
        private List parentPath;
        private List<Field> fields;
        private ContextParams parentContextParams;

        @Override
        public void run() {
            for (Field field : fields) {
                executorService.execute(new FieldRunner(parentPath, field, parentContextParams));
            }
        }
    }

    @AllArgsConstructor
    private class ListResultsRunner implements Runnable {
        List parentResults;
        private List parentPath;
        private List<Field> fields;
        private ContextParams parentContextParams;

        @Override
        public void run() {
            int index = 0;
            for (Object o : parentResults) {
                List path = Lists.newArrayList(parentPath);
                path.add(index);
                Map parentResultsMap = Util.toMap(o);
                completeNotifierMap.put(path, new MapCompleteNotifier(parentResultsMap, parentPath, index, fields.size()));
                executorService.execute(new FieldsRunner(path, fields, parentContextParams.extend(parentResultsMap)));
                ++index;
            }
        }
    }


    @Override
    public Map reduce(List<Field> fields, ContextParams contextParams) {
        List key = Lists.newArrayList();
        RootCompleteNotifier root = new RootCompleteNotifier(fields.size());

        completeNotifierMap.put(key, root);
        executorService.execute(new FieldsRunner(key, fields, contextParams));

        synchronized(lock){
            while (!isComplete){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return root.getResults();
    }
}