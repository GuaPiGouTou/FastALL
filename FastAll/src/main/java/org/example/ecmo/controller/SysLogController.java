package org.example.ecmo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.SysLoginLog;
import org.example.ecmo.entity.SysOperLog;
import org.example.ecmo.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/syslog")
public class SysLogController {
    @Autowired
    private LogService logService;
    /**
     * 查询登录日志列表
     */
    @AuditLog(title="登录日志查询")
    @GetMapping("/login/pagelist")
    public JsonResult<Page<SysLoginLog>> loginList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            String username,
            String status) {
        return new JsonResult<>(200, logService.findLoginLogPage(current, size, username, status), "查询成功");
    }
    /**
     * 查询操作日志列表
     */
    @AuditLog(title = "操作日志查询", businessType = 0)
    @GetMapping("/oper/pagelist")
    public JsonResult<Page<SysOperLog>> operList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            String title,
            String status) {
        return new JsonResult<>(200, logService.findOperLogPage(current, size, title, status), "查询成功");
    }
}
