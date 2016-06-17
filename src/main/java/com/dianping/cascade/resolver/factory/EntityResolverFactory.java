package com.dianping.cascade.resolver.factory;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.ParamConverter;
import com.dianping.cascade.ContextParamResolver;
import com.dianping.cascade.ParameterResolverFactory;

import java.lang.annotation.Annotation;

/**
 * Created by yangjie on 1/24/16.
 */
public class EntityResolverFactory implements ParameterResolverFactory {

    @Override
    public ContextParamResolver create(Annotation annotation, final ParamConverter converter) {
        return new ContextParamResolver() {
            @Override
            public Object resolve(ContextParams params) {
                return converter.convert(params.getAll());
            }
        };
    }
}
