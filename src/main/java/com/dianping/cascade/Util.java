package com.dianping.cascade;

import com.google.common.collect.Lists;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.List;
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

    private static List CAN_NOT_HAS_CHILDREN_CLASSES = Lists.newArrayList(
            int.class, boolean.class, double.class, float.class, long.class,String.class,
            Integer.class, Boolean.class, Double.class, Float.class, Long.class
    );

    public static boolean canNotHasChildren(Object o) {
        if (o == null) {
            return true;
        }

        return CAN_NOT_HAS_CHILDREN_CLASSES.indexOf(o.getClass()) != -1;
    }
}
