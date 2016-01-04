package com.dianping.cascade.test;

import com.alibaba.fastjson.JSON;
import com.dianping.cascade.Cascade;
import com.dianping.cascade.CascadeFactoryConfig;
import com.dianping.cascade.Field;
import com.dianping.cascade.cascadefactory.BeansCascadeFactory;
import com.dianping.cascade.test.cascade.Delay;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangjie on 1/3/16.
 */
public class ParallelPermanceTest {
    private Cascade multipleThreadCascade;
    private Cascade singleThreadCascade;


    @BeforeClass
    public void init() {
        CascadeFactoryConfig config1 = new CascadeFactoryConfig();
        config1.setThreadCount(100);
        BeansCascadeFactory factory1 = new BeansCascadeFactory(Lists.newArrayList(new Delay()),
                config1);
        multipleThreadCascade = factory1.create();


        CascadeFactoryConfig config2 = new CascadeFactoryConfig();
        config2.setThreadCount(1);
        BeansCascadeFactory factory2 = new BeansCascadeFactory(Lists.newArrayList(new Delay()),
                config2);
        singleThreadCascade = factory2.create();
    }

    @Test
    public void testMultipleThreadMultipleFields() {
        long elapsed = testMultipleFields(multipleThreadCascade);
        Assert.assertTrue(elapsed < 10 * 100);
    }

    @Test
    public void testSingleThreadMultipleFields() {
        long elapsed = testMultipleFields(singleThreadCascade);
        Assert.assertTrue(elapsed > 10 * 100);
    }


    @Test
    public void testMultipleThreadMultipleChildFields() {
        long elapsed = testMultipleChildFields(multipleThreadCascade);
        Assert.assertTrue(elapsed < 11 * 100);
    }

    @Test
    public void testSingleThreadMultipleChildFields() {
        long elapsed = testMultipleChildFields(singleThreadCascade);
        Assert.assertTrue(elapsed > 11 * 100);
    }
    
    @Test
    public void testMultipleThreadMultipleChildren() {
        long elapsed = testMultipleChildren(multipleThreadCascade);
        Assert.assertTrue(elapsed < 11 * 100);
    }
    
    @Test
    public void testSingleThreadMultipleChildren() {
        long elapsed = testMultipleChildren(singleThreadCascade);
        Assert.assertTrue(elapsed > 11 * 100);
    }
    

    private long testMultipleFields(Cascade cascade) {
        List<Field> fields = Lists.newArrayList();
        int fieldCount = 10;
        for (int i = 0; i < fieldCount; i++) {
            Field field = new Field();
            field.setType("Delay");
            field.setAs(Integer.valueOf(i).toString());
            field.setParams(new HashMap(){{
                put("count", 1);
            }});
            fields.add(field);
        }

        final Stopwatch stopwatch = Stopwatch.createStarted();
        Map ret = cascade.process(fields, null);
        System.out.println(JSON.toJSONString(ret));
        long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        System.out.println("time elapsed:" + elapsed + " ms");
        return elapsed;
    }

    private long testMultipleChildFields(Cascade cascade) {
        List<Field> fields = Lists.newArrayList();
        int fieldCount = 10;
        for (int i = 0; i < fieldCount; i++) {
            Field field = new Field();
            field.setType("Delay");
            field.setAs(Integer.valueOf(i).toString());
            field.setParams(new HashMap(){{
                put("count", 1);
            }});
            fields.add(field);
        }

        Field rootField = new Field();
        rootField.setType("Delay");
        rootField.setParams(new HashMap(){{
            put("count", 1);
        }});
        rootField.setChildren(fields);

        final Stopwatch stopwatch = Stopwatch.createStarted();
        Map ret = cascade.process(Lists.newArrayList(rootField), null);
        System.out.println(JSON.toJSONString(ret));
        long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        System.out.println("time elapsed:" + elapsed + " ms");

        return elapsed;
    }

    private long testMultipleChildren(Cascade cascade) {

        Field rootField = new Field();
        rootField.setType("Delay");
        rootField.setParams(new HashMap(){{
            put("count", 10);
        }});

        Field childField = new Field();
        childField.setType("Delay");
        childField.setParams(new HashMap(){{
            put("count", 1);
        }});

        rootField.setChildren(Lists.newArrayList(childField));

        final Stopwatch stopwatch = Stopwatch.createStarted();
        Map ret = cascade.process(Lists.newArrayList(rootField), null);
        System.out.println(JSON.toJSONString(ret));
        long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        System.out.println("time elapsed:" + elapsed + " ms");

        return elapsed;
    }
}
