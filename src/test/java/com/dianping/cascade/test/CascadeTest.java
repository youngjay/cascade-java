package com.dianping.cascade.test;

import com.alibaba.fastjson.JSON;
import com.dianping.cascade.Cascade;
import com.dianping.cascade.Field;
import com.dianping.cascade.test.cascade.Cooperation;
import com.dianping.cascade.test.cascade.User;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangjie on 9/23/15.
 */
public class CascadeTest {

    // 故意用map，让resolver做类型转换
    private static Map mockContext = new HashMap(){{
        put("context", new HashMap(){{
            put("id", 101);
            put("name", "Tom");
        }});
    }};

    private static Cascade c = new Cascade();

    public static void main(String[] args)  {
        c.register(Cooperation.class.getSimpleName(), new Cooperation());
        c.register(User.class.getSimpleName(), new User());

        testUser();
        testCooperation();
        testParamDTO();
        testNoType();
    }

    private static Field getUserField() {
        Field field = new Field();
        field.setType("User");
        field.setParams(new HashMap() {
            {
                put("name", "jay");
            }
        });
        return field;
    }

    private static Field getCooperationField() {
        Field field = new Field();
        field.setType("Cooperation");

        return field;
    }

    private static void print(Field field) {

        Object o = c.process(Lists.newArrayList(field), mockContext);

        System.out.println(JSON.toJSON(o));
    }

    private static void testUser() {
        Field userField = getUserField();
        print(userField);
    }

    private static void testCooperation() {
        Field userField = getUserField();
        userField.setChildren(Lists.newArrayList(getCooperationField()));
        print(userField);
    }

    private static void testParamDTO() {
        Field cooperationField = getCooperationField();
        cooperationField.setCategory("queryByShop");
        cooperationField.setParams(new HashMap() {{
            put("shop", new HashMap() {{
                put("id", 10001);
            }});
        }});

        print(cooperationField);
    }

    private static void testNoType() {
        Field field = new Field();
        field.setParams(new HashMap() {{
            put("id", 10002);
        }});

        field.setChildren(Lists.newArrayList(getCooperationField()));

        print(field);
    }
}
