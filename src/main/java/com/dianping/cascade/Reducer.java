package com.dianping.cascade;

import java.util.Collection;
import java.util.Map;

/**
 * Created by yangjie on 12/5/15.
 */
public interface Reducer {
    Map reduce(Collection<Field> fields, ContextParams contextParams, Invoker invoker);
}
