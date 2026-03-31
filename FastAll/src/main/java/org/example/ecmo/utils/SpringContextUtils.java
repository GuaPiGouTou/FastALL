package org.example.ecmo.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> requiredType) {
        if (applicationContext == null) {
            throw new IllegalStateException("SpringContextUtils未初始化，请检查是否被Spring扫描到");
        }
        return applicationContext.getBean(requiredType);
    }
}
