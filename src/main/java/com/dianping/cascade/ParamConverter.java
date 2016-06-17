package com.dianping.cascade;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;

/**
 * Created by yangjie on 6/17/16.
 */
public class ParamConverter {
    private static ObjectMapper m = new ObjectMapper();

    {
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JavaType javaType;

    public ParamConverter(Type type) {
        javaType = m.constructType(type);
    }

    public Object convert(Object input) {
        if (input == null) {
            return null;
        }

        try {
            return m.convertValue(input, javaType);
        } catch (Exception ex) {
            throw new RuntimeException(String.format("param type not match: expect [%s], actual [%s]", javaType.toCanonical(), input.getClass().getCanonicalName()));

        }
    }

}
