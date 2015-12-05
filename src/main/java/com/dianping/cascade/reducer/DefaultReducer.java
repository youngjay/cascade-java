package com.dianping.cascade.reducer;

import com.dianping.cascade.*;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Map;

/**
 * Created by yangjie on 12/5/15.
 */
public class DefaultReducer implements Reducer {
    private static final String CASCADE_ERROR = "[Cascade Error] ";

    @Override@SuppressWarnings("unchecked")
    public Map reduce(Collection<Field> fields, ContextParams contextParams, Invoker invoker) {
        Map results = Maps.newHashMap();

        for (Field field : fields) {
            Object result;
            try {
                result = reduceField(field, contextParams, invoker);
            } catch (Exception ex) {
                result = CASCADE_ERROR + ex.getMessage();
            }
            results.put(field.getComputedAs(), result);
        }

        return results;
    }

    private Object reduceField(final Field field, ContextParams parentContextParams, final Invoker invoker) {

        final ContextParams contextParams = parentContextParams.extend(field.getParams());

        Object result = invoker.invoke(field, contextParams);

        if (CollectionUtils.isEmpty(field.getChildren()) || result == null) {
            return result;
        }

        if (result instanceof Collection) {
            return Collections2.transform((Collection) result, new Function() {
                @Override
                public Object apply(Object input) {
                    return processFieldsWithResults(input, field.getChildren(), contextParams, invoker);
                }
            });
        } else {
            return processFieldsWithResults(result, field.getChildren(), contextParams, invoker);
        }

    }

    @SuppressWarnings("unchecked")
    private Map processFieldsWithResults(Object result, Collection<Field> fields, ContextParams parentContextParams, Invoker invoker) {
        Map resultMap = Util.toMap(result);
        ContextParams contextParams = parentContextParams.extend(resultMap);
        Map subResultMap = reduce(fields, contextParams, invoker);
        resultMap.putAll(subResultMap);
        return resultMap;
    }


}
