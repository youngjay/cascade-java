package com.dianping.cascade.resolver.factory;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.ParamConverter;
import com.dianping.cascade.ContextParamResolver;
import com.dianping.cascade.ParameterResolverFactory;
import com.dianping.cascade.annotation.Param;

import java.lang.annotation.Annotation;

/**
 * Created by yangjie on 1/24/16.
 */
public class ParamResolverFactory implements ParameterResolverFactory {
    private static final String SPLITTER = ",";

    @Override
    public ContextParamResolver create(Annotation annotation, final ParamConverter converter) {
        Param param = (Param) annotation;
        final String[] paramKeys = param.value().split(SPLITTER);

        return new ContextParamResolver() {
            @Override
            public Object resolve(ContextParams params) {
                return converter.convert(findByKeys(params));
            }

            private Object findByKeys(ContextParams params) {
                Object value = null;
                for (String paramKey : paramKeys) {
                    value = params.get(paramKey);
                    if (value != null) {
                        break;
                    }
                }
                return value;
            }
        };
    }
}
