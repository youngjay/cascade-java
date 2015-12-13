package com.dianping.cascade;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by yangjie on 10/30/15.
 */
public class ContextParams {
    private ContextParams parent;
    private Map current;

    public ContextParams(Map current, ContextParams parent) {
        this.parent = parent;
        this.current = current;
    }

    public Object get(Object key) {
        Object result = null;

        if (current != null) {
            result = current.get(key);

            if (result != null) {
                return result;
            }
        }

        if (parent != null) {
            result = parent.get(key);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public Map getAll() {
        Map ret = Maps.newHashMap();
        if (parent != null) {
            ret.putAll(parent.getAll());
        }
        if (current != null) {
            ret.putAll(current);
        }
        return ret;
    }

    public ContextParams extend(Map params) {
        return new ContextParams(params, this);
    }

    public static ContextParams create(Map params) {
        return new ContextParams(params, null);
    }

    public static ContextParams create() {
        return create(null);
    }

}
