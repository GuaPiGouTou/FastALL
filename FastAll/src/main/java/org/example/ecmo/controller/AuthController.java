package org.example.ecmo.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.SysUser;
import org.example.ecmo.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private SysUserService sysUserService;
    @AuditLog(title = "登录", businessType = 0)
    @PostMapping("/login")
    public JsonResult<SaTokenInfo> login(@RequestBody Map<String,String>  params) {
        return sysUserService.login(params);
    }
    @AuditLog(title = "注册", businessType = 0)
    @PostMapping("/regit")
    public JsonResult<String> regit(@RequestBody SysUser user){
        return sysUserService.regit(user);
    }
    /**
     * 退出登录
     */
    @AuditLog(title = "退出登录", businessType = 0)
    @PostMapping("/logout")
    public JsonResult<String> logout() {
        return sysUserService.logout();
    }

}