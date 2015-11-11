package com.dianping.cascade.spring;

import com.dianping.cascade.Cascade;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by yangjie on 9/24/15.
 */
public class CascadeFactory implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    // factory method
    public Cascade create() {
        Cascade cascade = new Cascade();
        cascade.register(applicationContext.getBeansOfType(CascadeAware.class).values());
        return cascade;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
