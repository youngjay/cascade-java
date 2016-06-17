package com.dianping.cascade.resolver.factory;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.ParameterResolver;
import com.dianping.cascade.ParameterResolverFactory;
import com.dianping.cascade.annotation.Entity;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by yangjie on 1/24/16.
 */
public class EntityResolverFactory implements ParameterResolverFactory {
    private static ObjectMapper m = new ObjectMapper();

    {
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ParameterResolver create(Annotation[] annotations, Type type) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Entity) {
                final JavaType javaType = m.constructType(type);

                return new ParameterResolver() {
                    @Override
                    public Object resolve(ContextParams params) {
                        try {
                            return m.convertValue(params.getAll(), javaType);
                        } catch (Exception ex) {
                            throw new RuntimeException(String.format("@Entity param can not create instance for type [%s]", javaType.toCanonical()));
                        }
                    }
                };
            }
        }

        return null;
    }
}
