package com.dianping.cascade.cascadefactory;

import com.dianping.cascade.Cascade;
import com.dianping.cascade.CascadeFactory;
import com.dianping.cascade.Registry;

import java.util.Collection;

/**
 * Created by yangjie on 12/15/15.
 */
public class BeansCascadeFactory implements CascadeFactory {
    private Registry registry = new Registry();
    private RegistryCascadeFactory registryCascadeFactory = new RegistryCascadeFactory(registry);

    public BeansCascadeFactory(Collection<? extends Object> beans) {
        for (Object bean : beans) {
            registry.register(bean);
        }
    }

    @Override
    public Cascade create() {
        return registryCascadeFactory.create();
    }
}
