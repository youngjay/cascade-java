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

/**
 * Created by yangjie on 12/5/15.
 */
public class SerialReducer implements Reducer {
    private static final String CASCADE_ERROR = "[Cascade Error] ";

    private Invoker invoker;

    public SerialReducer(Invoker invoker) {
        this.invoker = invoker;
    }

    @Override@SuppressWarnings("unchecked")
    public Map reduce(List<Field> fields, ContextParams contextParams) {
        Map results = Maps.newHashMap();

        for (Field field : fields) {
            results.put(field.getComputedAs(), reduceField(field, contextParams));
        }

        return results;
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
            return Lists.transform((List) result, new Function() {
                @Override
                public Object apply(Object input) {
                    return processFieldsWithResults(input, field.getChildren(), contextParams);
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
