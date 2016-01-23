package com.dianping.cascade.resolver.factory;

import com.dianping.cascade.ContextParams;
import com.dianping.cascade.ParameterResolver;
import com.dianping.cascade.ParameterResolverFactory;
import com.dianping.cascade.annotation.Param;
import com.google.common.collect.Lists;
import org.codehaus.jackson.map.ObjectMapper;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Created by yangjie on 1/24/16.
 */
public class ParamResolverFactory implements ParameterResolverFactory {
    public static class ParamResolver implements ParameterResolver {
        private static final ObjectMapper m = new ObjectMapper();

        private static final String SPLITTER = ",";
        private static final List<? extends  Class> NOT_ALLOW_NULL_CLASSES = Lists.newArrayList(int.class, boolean.class, double.class, float.class, long.class);
        private static boolean isAllowNullFor(Class type) {
            return !NOT_ALLOW_NULL_CLASSES.contains(type);
        }

        private String[] paramKeys;
        private Class type;
        private boolean allowNull;

        public ParamResolver(Param param, Class type) {
            String paramKeysSplitByDot = param.value();
            this.paramKeys = paramKeysSplitByDot.split(SPLITTER);
            this.type = type;
            this.allowNull = isAllowNullFor(type);
        }

        @Override
        public Object resolve(ContextParams params) {
            Object value = null;
            for (String paramKey : paramKeys) {
                value = params.get(paramKey);
                if (value != null) {
                    break;
                }
            }
            return convert(value);
        }

        private Object convert(Object o) {
            if (o == null) {
                if (allowNull) {
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
            return String.format("@Param(\"%s\") ", paramKeys);
        }
    }

    @Override
    public ParameterResolver create(Annotation[] annotations, Class type) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Param) {
                return new ParamResolver((Param) annotation, type);
            }
        }
        return null;
    }
}
