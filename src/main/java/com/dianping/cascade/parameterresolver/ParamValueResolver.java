package com.dianping.cascade.parameterresolver;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.ParameterResolver;
import com.google.common.collect.Lists;
import lombok.Data;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.List;

/**
 * Created by yangjie on 10/24/15.
 */
@Data
public class ParamValueResolver implements ParameterResolver {
    private static ObjectMapper m;

    {
        m = new ObjectMapper();
//        m.configure(SerializationConfig.Feature., false);
    }

    private static List<? extends  Class> NOT_ALLOW_NULL_CLASSES = Lists.newArrayList(int.class, boolean.class, double.class, float.class, long.class);


    private String paramKey;
    private Class type;
    private boolean allowNull;

    public ParamValueResolver(String paramKey, Class type) {
        this.paramKey = paramKey;
        this.type = type;
        this.allowNull = isAllowNullFor(type);
    }

    private boolean isAllowNullFor(Class type) {
        return !NOT_ALLOW_NULL_CLASSES.contains(type);
    }

    @Override
    public Object resolve(ContextParams params) {
        return convert(params.get(paramKey));
    }

    private Object convert(Object o) {
        if (o == null) {
            if (isAllowNull()) {
                return null;
            } else {
                throw new IllegalArgumentException(String.format("not allow null for [%s]", paramKey));
            }
        }

        if (o.getClass().equals(type)) {
            return o;
        }

        try {
            return m.convertValue(o, type);
        } catch (Exception ex) {
            throw new RuntimeException(String.format("actual param [%s] can not convert to required param [%s]", o.getClass().getSimpleName(), type.getSimpleName()));
        }

    }
}
