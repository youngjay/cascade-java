package com.dianping.cascade.test;

import com.alibaba.fastjson.JSON;
import com.dianping.cascade.*;
import com.dianping.cascade.cascadefactory.BeansCascadeFactory;
import com.dianping.cascade.test.cascade.Cooperation;
import com.dianping.cascade.test.cascade.Delay;
import com.dianping.cascade.test.cascade.Shop;
import com.dianping.cascade.test.cascade.User;
import com.dianping.cascade.test.model.UserDTO;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.apache.commons.beanutils.PropertyUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangjie on 9/23/15.
 */
public class CascadeTest {

    private Cascade c;


    @BeforeClass
    public void init() {
        BeansCascadeFactory factory = new BeansCascadeFactory(Lists.newArrayList(new Cooperation(), new User(), new Shop(), new Delay()), CascadeFactoryConfig.DEFAULT);
        c = factory.create();
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
        Assert.assertEquals(PropertyUtils.getProperty(ret, "user_load.name"), "Someone");
    }

    @Test
    public void testInitParams() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field field = new Field();
        field.setType("User");
        field.setCategory("context");
        final int context = 1;
        Map ret = c.process(Lists.newArrayList(field), new HashMap() {{
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

    @Test
    public void testProps() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field userField = new Field();
        final Integer id = 99;
        userField.setType("User");
        userField.setCategory("load");
        userField.setParams(new HashMap(){{
            put("userId", id);
        }});
        userField.setProps(Lists.newArrayList("id"));


        Map ret = c.process(Lists.newArrayList(userField), null);

         Assert.assertEquals(PropertyUtils.getProperty(ret, "user_load.id"), id);
         Assert.assertNull(PropertyUtils.getProperty(ret, "user_load.name"));

    }

    @Test
    public void testPropsException()  throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field userField = new Field();
        final Integer id = 99;
        userField.setType("User");
        userField.setCategory("load");
        userField.setParams(new HashMap(){{
            put("userId", id);
        }});
        userField.setProps(Lists.newArrayList("foo"));


        Map ret = c.process(Lists.newArrayList(userField), null);


        Assert.assertTrue(((String) PropertyUtils.getProperty(ret, "user_load")).startsWith("[Cascade Error]"));
    }

    @Test
    public void testEntity() throws Exception{
        Field userField = new Field();
        userField.setType("User");
        userField.setCategory("add");
        userField.setParams(new HashMap() {{
            put("id", 1);
            put("name", "someone");
        }});

        Map ret = c.process(Lists.newArrayList(userField), null);

        Assert.assertEquals(((UserDTO) ret.get("user_add")).getId(), 2);
        Assert.assertEquals(((UserDTO) ret.get("user_add")).getName(), "someone1");
    }

    @Test
    public void testEntityWithNotCompleteOrMoreProperties() throws Exception{
        Field userField = new Field();
        userField.setType("User");
        userField.setCategory("add");
        userField.setParams(new HashMap() {{
            put("id", 1);
            put("ppppp", "someone");
        }});

        Map ret = c.process(Lists.newArrayList(userField), null);

        Assert.assertEquals(((UserDTO) ret.get("user_add")).getId(), 2);
        Assert.assertEquals(((UserDTO) ret.get("user_add")).getName(), "1");
    }


    private Field getUserFieldForCacheTest(final String name) {

        Field field = new Field();
        field.setType("User");
        field.setCategory("cachedLoad");
        field.setParams(new HashMap() {{
            put("userId", 1);
            put("object", new UserDTO(1, name));
        }});

        return field;

    }

    private Field getFixedUserFieldForCacheTest() {
        return getUserFieldForCacheTest("ddd");
    }

    @Test
    // 这个测试要看命令行
    // cachedload called:1 应该出现1次
    public void testCache() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        Field field = getFixedUserFieldForCacheTest();

        Map ret = c.process(Lists.newArrayList(field), null);

        Field field1 = getFixedUserFieldForCacheTest();

        Map ret1 = c.process(Lists.newArrayList(field1), null);

    }


}
