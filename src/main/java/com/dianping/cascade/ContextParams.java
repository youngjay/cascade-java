package com.dianping.cascade;

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
}
