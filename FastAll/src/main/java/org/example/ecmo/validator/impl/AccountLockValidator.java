package org.example.ecmo.validator.impl;

import org.example.ecmo.context.LoginContext;
import org.example.ecmo.validator.LoginValidator;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class AccountLockValidator implements LoginValidator {

    private static final long WINDOW_MINUTES = 30;
    private static final int MAX_FAIL_COUNT = 5;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean validate(LoginContext context) {
        String failKey = "login:fail_window:" + context.getUsername();
        RScoredSortedSet<String> failWindows = redissonClient.getScoredSortedSet(failKey);
        long now = System.currentTimeMillis();
        long failWindowsStart = now - Duration.ofMinutes(WINDOW_MINUTES).toMillis();
        failWindows.removeRangeByScore(0, true, failWindowsStart, false);
        
        if (failWindows.size() >= MAX_FAIL_COUNT) {
            Double oldScore = failWindows.firstScore();
            long remainMs = (long) (oldScore + Duration.ofMinutes(WINDOW_MINUTES).toMillis()) - now;
            context.setErrorMessage(String.format("账号已被锁定，请 %d 秒后再试", Math.max(0, remainMs / 1000)));
            return false;
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
