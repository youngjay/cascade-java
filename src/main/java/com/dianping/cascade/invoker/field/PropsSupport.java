package com.dianping.cascade.invoker.field;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.Field;
import com.dianping.cascade.FieldInvoker;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 12/12/15.
 */
@Deprecated
// 用这个invoker没办法做到，在传给children全部属性的同时，返回的数据里面只包含指定的属性
// 所以现在的做法是在reducer里面做
@AllArgsConstructor
public class PropsSupport implements FieldInvoker {
    private FieldInvoker fieldInvoker;

    @Override
    public Object invoke(Field field, ContextParams contextParams) {
        Object result = fieldInvoker.invoke(field, contextParams);

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
}
