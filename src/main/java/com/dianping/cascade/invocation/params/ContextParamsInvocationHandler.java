package com.dianping.cascade.invocation.params;

import com.dianping.cascade.ContextParams;

/**
 * Created by yangjie on 12/27/15.
 */
public interface ContextParamsInvocationHandler {
    Object invoke(ContextParams params) throws Exception;;
}
