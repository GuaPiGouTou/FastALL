package org.example.ecmo.aspect;

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

@Aspect
@Component
public class AuditLogAspect {
    @Autowired
    SysOperLogMapper sysOperLogMapper;
    @Qualifier("logExecutor")
    @Autowired
    ThreadPoolTaskExecutor logExecutor;
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
        //异步入库日志
        logExecutor.execute(()->{
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
        operLog.setJsonResult(JSON.toJSONString(e));     // 返回结果
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
        //异步入库日志
        logExecutor.execute(()->{
            sysOperLogMapper.insert(operLog);
        });

    }
}
