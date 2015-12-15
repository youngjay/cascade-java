package com.dianping.cascade.cascadefactory;

import com.dianping.cascade.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by yangjie on 9/24/15.
 */
public class SpringContextCascadeFactory implements ApplicationContextAware, CascadeFactory {
    private BeansCascadeFactory beansCascadeFactory;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        beansCascadeFactory = new BeansCascadeFactory(applicationContext.getBeansOfType(CascadeAware.class).values());
    }

    @Override
    public Cascade create() {
        return beansCascadeFactory.create();
    }
}
