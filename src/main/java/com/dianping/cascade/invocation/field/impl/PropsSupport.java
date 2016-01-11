package com.dianping.cascade.invocation.field.impl;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.Field;
import com.dianping.cascade.invocation.field.FieldInvocationHandler;
import com.dianping.cascade.invocation.field.FieldInvocationInterceptor;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 12/12/15.
 */
@AllArgsConstructor
public class PropsSupport implements FieldInvocationInterceptor {
    @Override
    public Object invoke(FieldInvocationHandler fieldInvocationHandler, Field field, ContextParams contextParams) {
        Object result = fieldInvocationHandler.invoke(field, contextParams);

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
