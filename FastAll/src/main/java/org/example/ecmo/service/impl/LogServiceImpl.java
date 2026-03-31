package org.example.ecmo.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.ecmo.entity.SysLoginLog;
import org.example.ecmo.entity.SysOperLog;
import org.example.ecmo.mapper.SysLoginLogMapper;
import org.example.ecmo.mapper.SysOperLogMapper;
import org.example.ecmo.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class LogServiceImpl implements LogService {
    @Autowired
    private SysLoginLogMapper sysLoginLogMapper;
    @Autowired
    private SysOperLogMapper sysOperLogMapper;
    @Async
    @Override
    public void recordLoginLog(SysLoginLog loginLog) {
        loginLog.setLoginTime(LocalDateTime.now());
        sysLoginLogMapper.insert(loginLog);
    }


    @Override
    public Page<SysLoginLog> findLoginLogPage(int current, int size, String username, String status) {
        // 这里的泛型必须是 <SysLoginLog>
        Page<SysLoginLog> page = new Page<>(current, size);
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        // 筛选条件：匹配用户名 (username)
        wrapper.like(StringUtils.hasText(username), SysLoginLog::getUsername, username)
                .eq(StringUtils.hasText(status), SysLoginLog::getStatus, status)
                .orderByDesc(SysLoginLog::getLoginTime); // 注意是登录时间
        return sysLoginLogMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<SysOperLog> findOperLogPage(int current, int size, String title, String status) {
        Page<SysOperLog> page = new Page<>(current, size);
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();

        // 只有当有值时才拼接条件
        wrapper.like(StringUtils.hasText(title), SysOperLog::getTitle, title)
                .eq(StringUtils.hasText(status), SysOperLog::getStatus, status)
                .orderByDesc(SysOperLog::getOperTime); // 倒序看最新

        return sysOperLogMapper.selectPage(page, wrapper);
    }


}
