package com.dianping.cascade.reducer;

import com.dianping.cascade.*;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yangjie on 03/01/16.
 */
public class ParallelReducer implements Reducer {
    private FieldInvoker fieldInvoker;
    private ExecutorService executorService;

    private static final String CASCADE_ERROR = "[Cascade Error] ";

    public ParallelReducer(FieldInvoker fieldInvoker, ExecutorService executorService) {
        this.fieldInvoker = fieldInvoker;
        this.executorService = executorService;
    }

    private interface CompleteNotifier {
        void emit(Object key, Object value);
    }

    private static abstract class CompleteNotifierBase<T> implements CompleteNotifier {
        protected T parentResults;
        private CompleteNotifier parent;
        private Object keyInParent;
        private AtomicInteger remainCount;

        protected CompleteNotifierBase(T parentResults, CompleteNotifier parent, Object keyInParent, int remainCount) {
            this.parentResults = parentResults;
            this.parent = parent;
            this.keyInParent = keyInParent;
            this.remainCount = new AtomicInteger(remainCount);
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

    private static class MapCompleteNotifier extends CompleteNotifierBase<Map> {
        public MapCompleteNotifier(Map parentResults, CompleteNotifier parent, Object keyInParent, int remainCount) {
            super(parentResults, parent, keyInParent, remainCount);
        }

        protected void setData(Object key, Object value) {
            parentResults.put(key, value);
        }
    }

    private static class ListCompleteNotifier extends CompleteNotifierBase<List> {
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
        private CompleteNotifier parent;
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
                parent.emit(field.getComputedAs(), result);
            } else {
                if (result instanceof List) {
                    List resultList = (List) result;
                    CompleteNotifier list =  new ListCompleteNotifier(resultList, parent, field.getComputedAs(), resultList.size());
                    executorService.execute(new ListResultsRunner(resultList, list, field.getChildren(), contextParams));
                } else {
                    Map resultMap = Util.toMap(result);
                    CompleteNotifier map = new MapCompleteNotifier(resultMap, parent, field.getComputedAs(), field.getChildren().size());
                    executorService.execute(new FieldsRunner(map, field.getChildren(), contextParams.extend(resultMap)));
                }
            }

        }
    }

    @AllArgsConstructor
    private class FieldsRunner implements Runnable {
        private CompleteNotifier parent;
        private List<Field> fields;
        private ContextParams parentContextParams;

        @Override
        public void run() {
            for (Field field : fields) {
                executorService.execute(new FieldRunner(parent, field, parentContextParams));
            }
        }
    }

    @AllArgsConstructor
    private class ListResultsRunner implements Runnable {
        private List parentResults;
        private CompleteNotifier parent;
        private List<Field> fields;
        private ContextParams parentContextParams;

        @Override
        public void run() {
            int index = 0;
            for (Object o : parentResults) {
                Map parentResultsMap = Util.toMap(o);
                CompleteNotifier map = new  MapCompleteNotifier(parentResultsMap, parent, index, fields.size());
                executorService.execute(new FieldsRunner(map, fields, parentContextParams.extend(parentResultsMap)));
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