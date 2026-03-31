package org.example.ecmo.service.impl;

import com.anji.captcha.service.CaptchaCacheService;
import org.example.ecmo.utils.SpringContextUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 用于 Anji 验证码的 Redis 缓存实现类
 * 通过 SPI 机制被框架加载
 */
public class CaptchaCacheServiceRedisImpl implements CaptchaCacheService {
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 手动获取 StringRedisTemplate 的实例，避免空指针
     */
    private StringRedisTemplate getStringRedisTemplate() {
        if (stringRedisTemplate == null) {
            stringRedisTemplate = SpringContextUtils.getBean(StringRedisTemplate.class);
        }
        return stringRedisTemplate;
    }

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        getStringRedisTemplate().opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exists(String key) {
        return getStringRedisTemplate().hasKey(key);
    }

    @Override
    public void delete(String key) {
        getStringRedisTemplate().delete(key);
    }

    @Override
    public String get(String key) {
        return getStringRedisTemplate().opsForValue().get(key);
    }

    @Override
    public String type() {
        return "redis";
    }

    @Override
    public Long increment(String key, long val) {
        return getStringRedisTemplate().opsForValue().increment(key, val);
    }
}
