package com.dianping.cascade.invokable;

import com.dianping.cascade.ParameterResolvers;
import com.dianping.cascade.annotation.Cacheable;
import com.google.common.collect.Maps;
import org.apache.commons.collections.map.LRUMap;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 12/27/15.
 */
public class CacheableInvokable extends DefaultInvokable {
    private Map resultsMap;

    public CacheableInvokable(Object target, Method method, ParameterResolvers parameterResolvers, Cacheable cacheable) {
        super(target, method, parameterResolvers);
        resultsMap = new LRUMap(cacheable.size());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(List args) {
        Object result = resultsMap.get(args);
        if (result == null) {
            result = super.invoke(args);
            resultsMap.put(args, result);
        }
        return result;
    }
}
