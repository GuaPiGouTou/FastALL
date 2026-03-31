package org.example.ecmo.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.AsyncConfig;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.SysOperLog;
import org.example.ecmo.mapper.SysOperLogMapper;

import org.example.ecmo.utils.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;

@Aspect
@Component
public class AuditLogAspect {
    @Autowired
    SysOperLogMapper sysOperLogMapper;
    @Qualifier("logExecutor")
    @Autowired
    Executor logExecutor;
    @Pointcut("@annotation(org.example.ecmo.annotation.AuditLog)")
    public void logPointCut(){
    }

    @AfterReturning(pointcut = "logPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        //获取当前请求属性
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        //创建日志实体并填充基本信息
        SysOperLog operLog = new SysOperLog();
        operLog.setOperUrl(request.getRequestURI());       // 请求路径
        operLog.setRequestMethod(request.getMethod());     // 请求方式 (GET/POST)
        operLog.setOperIp(IpUtils.getIpAddr( request));       // 操作 IP
        operLog.setOperTime(LocalDateTime.now());         // 操作时间
        operLog.setStatus(0);                             // 0 代表成功
        operLog.setJsonResult(JSON.toJSONString(jsonResult));     // 返回结果
        //从切点中获取方法签名
        MethodSignature signature  = (MethodSignature) joinPoint.getSignature();
        //获取方法
        Method method = signature.getMethod();
        //获取注解信息
        AuditLog auditLog = method.getAnnotation(AuditLog.class);
        //设置日志信息
        if(auditLog!=null)
        {
            operLog.setTitle(auditLog.title());
            operLog.setBusinessType(auditLog.businessType());
        }
        // 1. 在主线程捕获当前登录人 ID
        Long loginId = null;
        try {
            loginId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 忽略未登录导致的异常
        }
        
        final Long finalLoginId = loginId;
        // 2. 传入异步任务
        logExecutor.execute(() -> {
            operLog.setOperId(finalLoginId);
            System.out.println(">>> [异步日志] 成功记录操作人 ID: " + finalLoginId);
            sysOperLogMapper.insert(operLog);
        });
    }
    @AfterThrowing(pointcut = "logPointCut()",throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint,Exception e)
    {
        //获取当前请求属性
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        //设置日志
        SysOperLog operLog = new SysOperLog();
        operLog.setOperUrl(request.getRequestURI());       // 请求路径
        operLog.setRequestMethod(request.getMethod());     // 请求方式 (GET/POST)
        operLog.setOperIp(IpUtils.getIpAddr( request));       // 操作 IP
        operLog.setOperTime(LocalDateTime.now());         // 操作时间
        operLog.setStatus(1);
        operLog.setErrorMsg(e.getMessage());
        operLog.setJsonResult(e.getMessage());     // 必须修改：Java 17 不支持直接序列化 Exception 对象
        //获取 方法签名
        MethodSignature signature  = (MethodSignature) joinPoint.getSignature();
        //获取方法
        Method method = signature.getMethod();
        //获取注解
        AuditLog annotation = method.getAnnotation(AuditLog.class);
        if(annotation!=null)
        {
            operLog.setTitle(annotation.title());
            operLog.setBusinessType(annotation.businessType());
        }
        // 1. 在主线程捕获当前登录人 ID
        Long loginId = null;
        try {
            loginId = StpUtil.getLoginIdAsLong();
        } catch (Exception e1) {
            // 忽略未登录导致的异常
        }
        
        final Long finalLoginId = loginId;
        // 2. 传入异步任务
        logExecutor.execute(() -> {
            operLog.setOperId(finalLoginId);
            System.out.println(">>> [异步日志] 成功记录操作人 ID: " + finalLoginId);
            sysOperLogMapper.insert(operLog);
        });

    }
}
