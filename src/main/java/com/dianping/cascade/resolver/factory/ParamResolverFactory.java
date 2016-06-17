package com.dianping.cascade.resolver.factory;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.ParameterResolver;
import com.dianping.cascade.ParameterResolverFactory;
import com.dianping.cascade.annotation.Param;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by yangjie on 1/24/16.
 */
public class ParamResolverFactory implements ParameterResolverFactory {
    private static final ObjectMapper m = new ObjectMapper();

    {
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static final String SPLITTER = ",";

    @Override
    public ParameterResolver create(Annotation[] annotations, Type type) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Param) {
                Param param = (Param) annotation;
                final String[] paramKeys = param.value().split(SPLITTER);
                final JavaType javaType = m.constructType(type);

                return new ParameterResolver() {
                    @Override
                    public Object resolve(ContextParams params) {
                        return convert(findByKeys(params));
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

                    private Object convert(Object o) {
                        if (o == null) {
                            return null;
                        }

                        try {
                            return m.convertValue(o, javaType);
                        } catch (Exception ex) {
                            throw new RuntimeException(getLocation() + String.format("param type not match: expect [%s], actual [%s]", javaType.toCanonical(), o.getClass().getSimpleName()));
                        }
                    }

                    private String getLocation() {
                        return String.format("@Param(\"%s\") ", paramKeys);
                    }
                };
            }
        }
        return null;
    }
}
