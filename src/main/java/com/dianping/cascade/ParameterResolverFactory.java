package com.dianping.cascade;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by yangjie on 1/23/16.
 */
public interface ParameterResolverFactory {
    ParameterResolver create(Annotation[] annotations, Type type);
}
