package org.example.ecmo.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributeLock {
    /**
     * 锁的Key，支持SpEL表达式（例如：#user.username）
     */
    String key()  default "#user.phone";

    /**
     * 等待锁的超时时间（默认 5 秒）
     */
    long waitTime() default 5;

    /**
     * 锁自动释放时间（默认 10 秒）
     */
    long leaseTime() default 20;

    /**
     * 时间单位（默认秒）
     */
    TimeUnit unit() default TimeUnit.SECONDS;
}
