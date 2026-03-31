package org.example.ecmo.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.ecmo.annotation.DistributeLock;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.context.LoginContext;
import org.example.ecmo.entity.SysUser;
import org.example.ecmo.mapper.SysUserMapper;
import org.example.ecmo.service.LogService;
import org.example.ecmo.service.SysUserService;
import org.example.ecmo.utils.LogUtils;
import org.example.ecmo.utils.PasswordUtil;
import org.example.ecmo.utils.ServletUtils;
import org.example.ecmo.validator.LoginValidator;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.example.ecmo.utils.IpUtils.getIpAddr;

@Slf4j
@Service
public class SysUserServiceImpl implements SysUserService {

    private static final long WINDOW_MINUTES = 30;
    private static final int MAX_FAIL_COUNT = 5;

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private LogService logService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RBloomFilter<String> userBloomFilter;
    @Autowired
    private List<LoginValidator> validators;

    @Override
    public JsonResult<SaTokenInfo> login(Map<String, String> params) {
        String ipAddress = getIpAddr(ServletUtils.getRequest());
        LoginContext context = new LoginContext(params, ServletUtils.getRequest(), ipAddress);
        
        LogUtils.setRests(ServletUtils.getRequest(), context.getLoginLog());

        List<LoginValidator> sortedValidators = validators.stream()
                .sorted(Comparator.comparingInt(LoginValidator::getOrder))
                .collect(Collectors.toList());

        for (LoginValidator validator : sortedValidators) {
            if (!validator.validate(context)) {
                recordLoginFailure(context);
                return new JsonResult<>(401, null, context.getErrorMessage());
            }
        }

        return performLogin(context);
    }

    private void recordLoginFailure(LoginContext context) {
        context.getLoginLog().setMsg(context.getErrorMessage());
        logService.recordLoginLog(context.getLoginLog());
        
        if (context.isShouldRecordFailedAttempt()) {
            recordFailedAttempt(context.getUsername());
        }
    }

    private void recordFailedAttempt(String username) {
        String failKey = "login:fail_window:" + username;
        RScoredSortedSet<String> failWindows = redissonClient.getScoredSortedSet(failKey);
        failWindows.add(System.currentTimeMillis(), UUID.randomUUID().toString());
        failWindows.expire(Duration.ofMinutes(WINDOW_MINUTES));
    }

    private JsonResult<SaTokenInfo> performLogin(LoginContext context) {
        SysUser user = context.getUser();
        
        String failKey = "login:fail_window:" + context.getUsername();
        RScoredSortedSet<String> failWindows = redissonClient.getScoredSortedSet(failKey);
        failWindows.delete();

        context.getLoginLog().setStatus(0);
        context.getLoginLog().setMsg("登录成功");
        context.getLoginLog().setUserId(user.getId());
        logService.recordLoginLog(context.getLoginLog());

        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return new JsonResult<>(200, tokenInfo, "登录成功");
    }

    @DistributeLock(key = "#user.username")
    @Override
    public JsonResult<String> regit(SysUser user) {
        if (user.getUsername() != null && sysUserMapper.findByUsername(user.getUsername()) != null) {
            return new JsonResult<>(401, null, "用户名已存在");
        }
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            return new JsonResult<>(401, null, "用户名已存在");
        }
        
        String rawPassword = user.getPassword();
        try {
            AES aes = new AES("ecmo_aes_key_123".getBytes());
            rawPassword = aes.decryptStr(user.getPassword());
        } catch (Exception e) {
        }
        
        user.setPassword(PasswordUtil.encode(rawPassword));
        user.setStatus(1);
        try {
            sysUserMapper.insert(user);
            userBloomFilter.add(user.getUsername());
        } catch (DuplicateKeyException ignored) {
            return new JsonResult<>(401, null, "用户名已存在");
        }
        return new JsonResult<>(200, null, "注册成功");
    }

    @Override
    public boolean checkUserName(String userName) {
        return sysUserMapper.findByUsername(userName) != null;
    }

    @Override
    public boolean checkEmail(String email) {
        return sysUserMapper.findByEmail(email) != null;
    }

    @Override
    public List<String> findAllUsernames() {
        return sysUserMapper.findAllUsernames();
    }

    @Override
    public JsonResult<String> logout() {
        try {
            StpUtil.logout();
            String token = StpUtil.getTokenValue();
            if (token != null) {
                redisTemplate.delete("user:token:" + token);
            }
            return new JsonResult<>(200, null, "退出成功");
        } catch (Exception e) {
            log.error("退出登录失败", e);
            return new JsonResult<>(500, null, "退出失败");
        }
    }
}
