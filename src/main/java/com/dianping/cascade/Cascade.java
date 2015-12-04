package com.dianping.cascade;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Map;

/**
 * Created by yangjie on 9/22/15.
 */
public class Cascade {
    private Registry registry = new Registry();
    private static final String CASCADE_ERROR = "[Cascade Error] ";

    public Map process(Collection<Field> fields) {
        return process(fields, null);
    }

    public Map process(Field field) {
        return process(field, null);
    }

    public Map process(Collection<Field> fields, Object contextParams) {
        return processFields(Maps.newHashMap(), fields, new ContextParams(toMap(contextParams), null));
    }

    public Map process(Field field, Object contextParams) {
        return process(Lists.newArrayList(field), contextParams);
    }

    @SuppressWarnings("unchecked")
    private Map processFields(Map results, Collection<Field> fields, ContextParams parentContextParams) {
        ContextParams contextParams = new ContextParams(results, parentContextParams);

        for (Field field : fields) {
            if (field.getType() == null) {
                processFields(results, field.getChildren(), new ContextParams(field.getParams(), contextParams));
            } else {
                Object result;
                try {
                    result = processField(field, contextParams);
                } catch (Exception ex) {
                    result = CASCADE_ERROR + ex.getMessage();
                }
                results.put(field.getComputedAs(), result);
            }
        }

        return results;
    }

    private Object processField(final Field field, ContextParams parentContextParams) {


        Invokable invokable = registry.get(field.getType(), field.getCategory());

        final ContextParams contextParams = new ContextParams(field.getParams(), parentContextParams);

        Object result = invokable.invoke(contextParams);

        if (CollectionUtils.isEmpty(field.getChildren()) || result == null) {
            return result;
        }

        if (result instanceof Collection) {
            return Collections2.transform((Collection) result, new Function() {
                @Override
                public Object apply(Object input) {
                    return processFields(toMap(input), field.getChildren(), contextParams);
                }
            });
        } else {
            return processFields(toMap(result), field.getChildren(), contextParams);
        }

    }

    private Map toMap(Object bean) {
        if (bean == null) {
            return null;
        }

        if (bean instanceof Map) {
            return (Map) bean;
        }

        Map resultMap;
        try {
            resultMap = PropertyUtils.describe(bean);
            resultMap.remove("class");
            return resultMap;
        } catch (Exception e) {
            throw new RuntimeException(String.format("[%s] can not convert to Map", bean.getClass().getName()));
        }
    }

    public void register(Object bean) {
        this.register(bean.getClass().getSimpleName(), bean);
    }

    public void register(String type, Object bean) {
        this.registry.register(type, bean);
    }
}
