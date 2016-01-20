package com.dianping.cascade.cascadefactory;

import com.dianping.cascade.Cascade;
import com.dianping.cascade.CascadeFactory;
import com.dianping.cascade.CascadeFactoryConfig;
import com.dianping.cascade.Registry;

import java.util.Collection;

/**
 * Created by yangjie on 12/15/15.
 */
public class BeansCascadeFactory implements CascadeFactory {
    private RegistryCascadeFactory registryCascadeFactory;

    public BeansCascadeFactory(Collection<? extends Object> beans, CascadeFactoryConfig config) {
        Registry registry = new Registry(config);
        for (Object bean : beans) {
            registry.register(bean);
        }
        registryCascadeFactory = new RegistryCascadeFactory(registry, config);
    }

    @Override
    public Cascade create() {
        return registryCascadeFactory.create();
    }
}
