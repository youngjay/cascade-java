package com.dianping.cascade.reducer;

import com.dianping.cascade.*;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 12/5/15.
 */
public class SerialReducer implements Reducer {
    private static final String CASCADE_ERROR = "[Cascade Error] ";

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
        final ContextParams contextParams = parentContextParams.extend(field.getParams());

        try {
            Object result = fieldInvoker.invoke(field, contextParams);

            if (CollectionUtils.isEmpty(field.getChildren()) || result == null) {
                return postProcessResult(field, result);
            }

            if (result instanceof List) {
                return reduceResults((List) result, field, contextParams);
            } else {
                return processFieldsWithResults(result, field, contextParams);
            }
        } catch (Exception ex) {
            return CASCADE_ERROR + ex.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private List reduceResults(List<Object> results, final Field field, final ContextParams contextParams) {
        return Lists.transform(results, new Function() {
            @Override
            public Object apply(Object input) {
                return processFieldsWithResults(input, field, contextParams);
            }
        });
    }

    private Object postProcessResult(Field field, Object result) {
        List<String> props = field.getProps();

        if (props.size() == 0) {
            return result;
        }

        Map<String, Object> ret = Maps.newHashMapWithExpectedSize(props.size());

        for (String prop : props) {
            try {
                ret.put(prop, PropertyUtils.getProperty(result, prop));
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("can not get prop \"%s\" from [%s]", prop, result.getClass().getSimpleName()));
            }
        }

        return ret;
    }


    @SuppressWarnings("unchecked")
    private Map processFieldsWithResults(Object parentResult, Field parentField, ContextParams parentContextParams) {
        Map resultMap = Util.toMap(parentResult);
        ContextParams contextParams = parentContextParams.extend(resultMap);
        Map subResultMap = reduceFields(parentField.getChildren(), contextParams);
        subResultMap.putAll(Util.toMap(postProcessResult(parentField, resultMap)));
        return subResultMap;
    }
}
