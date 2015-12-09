package com.dianping.cascade.spring;

import com.dianping.cascade.Cascade;
import com.dianping.cascade.Invoker;
import com.dianping.cascade.Reducer;
import com.dianping.cascade.Registry;
import com.dianping.cascade.invoker.DefaultInvoker;
import com.dianping.cascade.reducer.SerialReducer;
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
        Registry registry = new Registry();
        Invoker invoker = new DefaultInvoker(registry);
        Reducer reducer = new SerialReducer(invoker);
        Cascade cascade = new Cascade(reducer);
        registry.register(applicationContext.getBeansOfType(CascadeAware.class).values());
        return cascade;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
