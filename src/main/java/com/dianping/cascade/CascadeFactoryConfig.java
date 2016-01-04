package com.dianping.cascade;

import lombok.Data;
import lombok.experimental.Builder;

/**
 * Created by yangjie on 1/3/16.
 */
@Data
public class CascadeFactoryConfig {
    private int threadCount;

    public final static CascadeFactoryConfig DEFAULT = new CascadeFactoryConfig();
}
