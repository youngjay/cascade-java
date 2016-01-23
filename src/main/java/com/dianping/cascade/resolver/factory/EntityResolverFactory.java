package com.dianping.cascade.resolver.factory;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.ParameterResolver;
import com.dianping.cascade.ParameterResolverFactory;
import com.dianping.cascade.annotation.Entity;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Created by yangjie on 1/24/16.
 */
public class EntityResolverFactory implements ParameterResolverFactory {

    public static class EntityResolver implements ParameterResolver {
        private static ObjectMapper m = new ObjectMapper();

        {
            m.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        private Class type;

        public EntityResolver(Class type) {
            this.type = type;
        }

        @Override
        public Object resolve(ContextParams params) {
            return convert(params.getAll());
        }

        private Object convert(Map params) {
            try {
                return m.convertValue(params, type);
            } catch (Exception ex) {
                throw new RuntimeException(String.format("@Entity param can not create instance for type [%s]", type.getSimpleName()));
            }
        }

    }

    @Override
    public ParameterResolver create(Annotation[] annotations, Class type) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Entity) {
                return new EntityResolver(type);
            }
        }
        return null;
    }
}
