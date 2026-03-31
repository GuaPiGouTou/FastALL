package org.example.ecmo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "logExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); //核心线程数
        executor.setMaxPoolSize(20); //最大线程数
        executor.setQueueCapacity(200); //队列容量
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());// 设置淘汰策略为调用者策略
        executor.setThreadNamePrefix("Async-auth");
        executor.initialize();
        return executor;
    }
}
