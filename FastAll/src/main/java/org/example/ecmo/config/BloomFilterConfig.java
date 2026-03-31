package org.example.ecmo.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfig {
    @Bean
    public RBloomFilter<String> userBloomFilter(RedissonClient redissonClient) {
        // 创建布隆过滤器，指定名称
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("user:bloom:filter");

        // 初始化布隆过滤器
        // 参数1：预期插入数量（根据你的用户总数预估，建议预留20%冗余）
        // 参数2：误判率（0.01 = 1%，越小占用内存越大）
        bloomFilter.tryInit(10000L, 0.01);

        return bloomFilter;
    }
    @Bean
    public RBloomFilter<String> emailVBloomFilter(RedissonClient redissonClient) {
        // 创建布隆过滤器，指定名称
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("email:bloom:filter");

        // 初始化布隆过滤器
        // 参数1：预期插入数量（根据你的用户总数预估，建议预留20%冗余）
        // 参数2：误判率（0.01 = 1%，越小占用内存越大）
        bloomFilter.tryInit(10000L, 0.01);
        return bloomFilter;
    }
}
