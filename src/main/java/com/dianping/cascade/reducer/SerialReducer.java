package com.dianping.cascade.reducer;

import com.dianping.cascade.*;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 12/5/15.
 */
public class SerialReducer implements Reducer {
    private FieldInvoker fieldInvoker;

    public SerialReducer(FieldInvoker fieldInvoker) {
        this.fieldInvoker = fieldInvoker;
    }

    @Override
    public Map reduce(List<Field> fields, ContextParams contextParams) {
        return reduceFields(fields, contextParams);
    }

    @SuppressWarnings("unchecked")
    private Map reduceFields(List<Field> fields, ContextParams contextParams) {
        Map results = Maps.newHashMap();

        for (Field field : fields) {
            results.put(field.getComputedAs(), reduceField(field, contextParams));
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    private Object reduceField(final Field field, ContextParams parentContextParams) {

        ContextParams contextParams = parentContextParams.extend(field.getParams());

        Object result = fieldInvoker.invoke(field, contextParams);

        if (field.getChildren().size() == 0 || Util.canNotHasChildren(result)) {
            return result;
        }

        if (result instanceof List) {
            return reduceResults((List) result, field.getChildren(), contextParams);
        } else {
            return processFieldsWithResults(result, field.getChildren(), contextParams);
        }
    }

    @SuppressWarnings("unchecked")
    private List reduceResults(List<Object> results, final List<Field> fields, final ContextParams contextParams) {
        return Lists.transform(results, new Function() {
            @Override
            public Object apply(Object input) {
                return processFieldsWithResults(input, fields, contextParams);
            }
        });
    }


    @SuppressWarnings("unchecked")
    private Map processFieldsWithResults(Object result, List<Field> fields, ContextParams parentContextParams) {
        Map resultMap = Util.toMap(result);
        ContextParams contextParams = parentContextParams.extend(resultMap);
        Map subResultMap = reduceFields(fields, contextParams);
        resultMap.putAll(subResultMap);
        return resultMap;
    }
}
