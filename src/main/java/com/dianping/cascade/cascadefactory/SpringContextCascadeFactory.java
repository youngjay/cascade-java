package com.dianping.cascade.cascadefactory;

import com.dianping.cascade.Cascade;
import com.dianping.cascade.CascadeAware;
import com.dianping.cascade.CascadeFactory;
import com.dianping.cascade.CascadeFactoryConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by yangjie on 9/24/15.
 */
public class SpringContextCascadeFactory implements ApplicationContextAware, CascadeFactory {
    private BeansCascadeFactory beansCascadeFactory;
    private CascadeFactoryConfig config;

    public SpringContextCascadeFactory(CascadeFactoryConfig config) {
        this.config = config;
    }

    public SpringContextCascadeFactory() {
        this(CascadeFactoryConfig.DEFAULT);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        beansCascadeFactory = new BeansCascadeFactory(applicationContext.getBeansOfType(CascadeAware.class).values(), config);
    }

    @Override
    public Cascade create() {
        return beansCascadeFactory.create();
    }
}
