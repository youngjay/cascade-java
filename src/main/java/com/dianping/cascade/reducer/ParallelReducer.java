package com.dianping.cascade.reducer;

import com.dianping.cascade.*;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yangjie on 03/01/16.
 */
public class ParallelReducer implements Reducer {
    private FieldInvoker fieldInvoker;
    private ExecutorService executorService;

    public ParallelReducer(FieldInvoker fieldInvoker, ExecutorService executorService) {
        this.fieldInvoker = fieldInvoker;
        this.executorService = executorService;
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
        private Object lock;
        private AtomicInteger remainCount;

        public RootCompleteNotifier(int remainCount, Object lock) {
            this.remainCount = new AtomicInteger(remainCount);
            this.lock = lock;
            results = Maps.newHashMapWithExpectedSize(remainCount);
        }

        @Override
        public void emit(Object key, Object value) {
            results.put(key, value);
            if (remainCount.decrementAndGet() == 0) {
                synchronized (lock) {
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
        private CompleteNotifier completeNotifier;
        private Field field;
        private ContextParams parentContextParams;

        @Override
        public void run() {
            ContextParams contextParams = parentContextParams.extend(field.getParams());

            Object result = fieldInvoker.invoke(field, contextParams);

            if (field.getChildren().size() == 0 || Util.canNotHasChildren(result)) {
                completeNotifier.emit(field.getComputedAs(), result);
            } else {
                if (result instanceof List) {
                    List resultList = (List) result;
                    executorService.execute(new ListResultsRunner(resultList, new ListCompleteNotifier(resultList, completeNotifier, field.getComputedAs(), resultList.size()), field.getChildren(), contextParams));
                } else {
                    Map resultMap = Util.toMap(result);
                    executorService.execute(new FieldsRunner(new MapCompleteNotifier(resultMap, completeNotifier, field.getComputedAs(), field.getChildren().size()), field.getChildren(), contextParams.extend(resultMap)));
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
                executorService.execute(new FieldsRunner(new MapCompleteNotifier(parentResultsMap, completeNotifier, index, fields.size()), fields, parentContextParams.extend(parentResultsMap)));
                ++index;
            }
        }
    }


    @Override
    public Map reduce(List<Field> fields, ContextParams contextParams) {

        Object lock = new Object();

        RootCompleteNotifier root = new RootCompleteNotifier(fields.size(), lock);
        executorService.execute(new FieldsRunner(root, fields, contextParams));

        synchronized(lock){
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return root.getResults();
    }
}