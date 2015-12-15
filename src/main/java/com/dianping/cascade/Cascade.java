package com.dianping.cascade;

import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 9/22/15.
 */
public interface Cascade {
    Map process(List<Field> fields, Map contextParams);
}
