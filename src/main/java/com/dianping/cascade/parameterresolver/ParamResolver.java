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
public class ParamResolver implements ParameterResolver {
    private static ObjectMapper m = new ObjectMapper();

    private static List<? extends  Class> NOT_ALLOW_NULL_CLASSES = Lists.newArrayList(int.class, boolean.class, double.class, float.class, long.class);


    private String paramKey;
    private Class type;
    private boolean allowNull;

    public ParamResolver(String paramKey, Class type) {
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
                throw new IllegalArgumentException(getLocation() + "not allow null");
            }
        }

        if (o.getClass().equals(type)) {
            return o;
        }

        try {
            return m.convertValue(o, type);
        } catch (Exception ex) {
            throw new RuntimeException(getLocation() + String.format("param type not match: expect [%s], actual [%s]", type.getSimpleName(), o.getClass().getSimpleName()));
        }

    }

    private String getLocation() {
        return String.format("@Param(\"%s\") ", paramKey);
    }
}
