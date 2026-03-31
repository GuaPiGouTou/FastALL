package org.example.ecmo.validator.impl;

import org.example.ecmo.context.LoginContext;
import org.example.ecmo.validator.LoginValidator;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RateLimitValidator implements LoginValidator {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean validate(LoginContext context) {
        String username = context.getUsername();
        
        RRateLimiter minLimiter = redissonClient.getRateLimiter("sys:min:" + username);
        minLimiter.trySetRate(RateType.OVERALL, 5, 5, RateIntervalUnit.MINUTES);
        if (!minLimiter.tryAcquire(1)) {
            context.setErrorMessage("登录失败，一分钟内不允许进行用户登录");
            return false;
        }
        
        RRateLimiter dayLimiter = redissonClient.getRateLimiter("sys:day:" + username);
        dayLimiter.trySetRate(RateType.OVERALL, 10, 1, RateIntervalUnit.DAYS);
        if (!dayLimiter.tryAcquire(1)) {
            context.setErrorMessage("登录失败，一天内不允许进行用户登录");
            return false;
        }
        
        return true;
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
