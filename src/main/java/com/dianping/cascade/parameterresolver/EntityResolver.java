package com.dianping.cascade.parameterresolver;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.ParameterResolver;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.util.Map;

/**
 * Created by yangjie on 12/13/15.
 */
public class EntityResolver implements ParameterResolver {
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
