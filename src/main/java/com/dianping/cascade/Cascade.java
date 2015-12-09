package com.dianping.cascade;

import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 9/22/15.
 */
public class Cascade {
    private Reducer reducer;
    public Cascade(Reducer reducer) {
        this.reducer = reducer;
    }

    public Map reduce(List<Field> fields, Map contextParams) {
        return reducer.reduce(fields, ContextParams.create(contextParams));
    }

}
