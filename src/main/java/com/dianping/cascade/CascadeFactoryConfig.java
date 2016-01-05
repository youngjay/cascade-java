package com.dianping.cascade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

/**
 * Created by yangjie on 1/3/16.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CascadeFactoryConfig {
    private int threadCount = 1;

    public final static CascadeFactoryConfig DEFAULT = new CascadeFactoryConfig();
}
