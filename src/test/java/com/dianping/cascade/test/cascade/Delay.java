package com.dianping.cascade.test.cascade;

import com.dianping.cascade.annotation.Param;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by yangjie on 1/3/16.
 */
public class Delay {
    public List query(@Param("count") int count) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List ret = Lists.newArrayListWithExpectedSize(count);
        for (int i = 0; i < count; i++) {
            ret.add(new Object());
        }
        return ret;
    }
}
