package org.example.ecmo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.ecmo.annotation.DistributeLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class DistributeLockAspect {

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(distributeLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributeLock distributeLock) throws Throwable {
        // 解析出真实的 Redis Key  #user.phone 转成具体的手机号
        MethodSignature signature  = (MethodSignature)joinPoint.getSignature();
        // 获取参数名称和参数值
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        // 创建 SpEL 上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 把参数值设置到 SpEL 上下文中
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        ExpressionParser parser = new SpelExpressionParser();
        String phone = parser.parseExpression(distributeLock.key()).getValue(context, String.class);
        String lockKey = "lock:" + phone;

        RLock lock = redissonClient.getLock(lockKey);

        boolean res = false;
        try {
            // 加锁
            res = lock.tryLock(distributeLock.waitTime(), distributeLock.leaseTime(), distributeLock.unit());

            if (res) {
                log.info("成功获取分布式锁: {}", lockKey);
                return joinPoint.proceed(); // 执行原业务方法
            } else {
                log.warn("获取分布式锁失败: {}", lockKey);
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
        } finally {
            // 释放锁 有锁并且是当前线程才释放
            if (res && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("成功释放分布式锁: {}", lockKey);
            }
        }
    }
}
