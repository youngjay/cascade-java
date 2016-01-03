package com.dianping.cascade;

import lombok.Data;
import lombok.experimental.Builder;

/**
 * Created by yangjie on 1/3/16.
 */
@Data
@Builder
public class CascadeFactoryConfig {
    private int threadCount;

    public final static CascadeFactoryConfig DEFAULT = CascadeFactoryConfig.builder().threadCount(1).build();
}
