package com.dianping.cascade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by yangjie on 1/3/16.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CascadeFactoryConfig {
    private int threadCount = 1;

    private List<InvocationInterceptorFactory> invocationInterceptorFactories;

    public final static CascadeFactoryConfig DEFAULT = new CascadeFactoryConfig();
}
