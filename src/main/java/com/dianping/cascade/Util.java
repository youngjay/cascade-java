package com.dianping.cascade;

import org.apache.commons.beanutils.PropertyUtils;

import java.util.Map;

/**
 * Created by yangjie on 12/5/15.
 */
public class Util {
    public static Map toMap(Object bean) {
        if (bean == null) {
            return null;
        }

        if (bean instanceof Map) {
            return (Map) bean;
        }

        Map resultMap;
        try {
            resultMap = PropertyUtils.describe(bean);
            resultMap.remove("class");
            return resultMap;
        } catch (Exception e) {
            throw new RuntimeException(String.format("[%s] can not convert to Map", bean.getClass().getName()));
        }
    }
}
