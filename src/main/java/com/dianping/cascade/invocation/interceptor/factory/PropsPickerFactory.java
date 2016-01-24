package com.dianping.cascade.invocation.interceptor.factory;

import com.dianping.cascade.*;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 12/12/15.
 */
public class PropsPickerFactory implements InvocationInterceptorFactory {
    @Override
    public InvocationInterceptor create(Method method, Object target, MethodParametersResolver methodParametersResolver) {
        return new InvocationInterceptor() {
            @Override
            public Object invoke(InvocationHandler invocationHandler, Field field, ContextParams contextParams) {
                Object result = invocationHandler.invoke(field, contextParams);

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
        };
    }
}
