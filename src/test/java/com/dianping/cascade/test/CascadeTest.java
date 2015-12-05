package com.dianping.cascade.test;

import com.dianping.cascade.Cascade;
import com.dianping.cascade.Field;
import com.dianping.cascade.test.cascade.Cooperation;
import com.dianping.cascade.test.cascade.Shop;
import com.dianping.cascade.test.cascade.User;
import com.google.common.collect.Lists;
import org.apache.commons.beanutils.PropertyUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangjie on 9/23/15.
 */
public class CascadeTest {
    private Cascade c = new Cascade();

    @BeforeClass
    public void init() {


        c.register(new Cooperation());
        c.register(new User());
        c.register(new Shop());
    }

    @Test
    public void testNoParams() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field field = new Field();
        field.setType("User");
        Map ret = c.process(Lists.newArrayList(field), null);
        Assert.assertEquals(((Collection) PropertyUtils.getProperty(ret, "user")).size(), 2);
    }

    @Test
    public void testParams() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field field = new Field();
        field.setType("User");
        field.setCategory("load");
        field.setParams(new HashMap() {{
            put("userId", 1);
        }});

        Map ret = c.process(Lists.newArrayList(field), null);

        Assert.assertEquals(PropertyUtils.getProperty(ret, "user_load.id"), 1);
    }

    @Test
    public void testInitParams() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field field = new Field();
        field.setType("User");
        field.setCategory("context");
        final int context = 1;
        Map ret = c.process(Lists.newArrayList(field), new HashMap(){{
            put("context", context);
        }});

        Assert.assertEquals(PropertyUtils.getProperty(ret, "user_context"), context);
    }

    @Test
    public void testBusinessException() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field field = new Field();
        field.setType("User");
        field.setCategory("businessException");


        Map ret = c.process(Lists.newArrayList(field), null);

        Assert.assertEquals(PropertyUtils.getProperty(ret, "user_businessException"), "[Cascade Error] error");
    }

    @Test
    public void testRuntimeException() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field field = new Field();
        field.setType("User");
        field.setCategory("runtimeException");


        Map ret = c.process(Lists.newArrayList(field), null);

        Assert.assertEquals(PropertyUtils.getProperty(ret, "user_runtimeException"), "[Cascade Error] [User.runtimeException] error");
    }

    @Test
    public void childCanReceiveParentParams() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        final int userId = 2;
        Field field = new Field();
        field.setType("User");
        field.setCategory("load");
        field.setParams(new HashMap() {{
            put("userId", userId);
        }});

        Field shopField = new Field();
        shopField.setType("Shop");
        shopField.setCategory("byUser");

        field.setChildren(Lists.newArrayList(shopField));


        Map ret = c.process(Lists.newArrayList(field), null);

        Assert.assertEquals(PropertyUtils.getProperty(ret, "user_load.shop_byUser.ownerId"), userId);
    }

    @Test
    public void childCanReceiveParentResults() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        final int userId = 2;

        Field field = new Field();
        field.setType("User");
        field.setCategory("load");
        field.setParams(new HashMap() {{
            put("userId", userId);
        }});

        Field shopField = new Field();
        shopField.setType("Shop");
        shopField.setCategory("byUserName");

        field.setChildren(Lists.newArrayList(shopField));


        Map ret = c.process(Lists.newArrayList(field), null);

        Assert.assertEquals(PropertyUtils.getProperty(ret, "user_load.shop_byUserName.name"), "user:Someone");
    }

    @Test
    public void testParamNotAllowNull() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field field = new Field();
        field.setType("User");
        field.setCategory("load");
        Map ret = c.process(Lists.newArrayList(field), null);

        Assert.assertEquals(PropertyUtils.getProperty(ret, "user_load"), "[Cascade Error] [User.load] @Param(\"userId\") not allow null");

    }

    @Test
    public void testParamCannotConvert() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field field = new Field();
        field.setType("User");
        field.setCategory("load");
        field.setParams(new HashMap(){{
            put("userId", Lists.newArrayList());
        }});
        Map ret = c.process(Lists.newArrayList(field), null);

        Assert.assertEquals(PropertyUtils.getProperty(ret, "user_load"), "[Cascade Error] [User.load] @Param(\"userId\") param type not match: expect [int], actual [ArrayList]");
    }
}
