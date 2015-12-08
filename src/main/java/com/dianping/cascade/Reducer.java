package com.dianping.cascade;

import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 12/5/15.
 */
public interface Reducer {
    Map reduce(List<Field> fields, ContextParams contextParams);
}
