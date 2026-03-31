package org.example.ecmo.validator.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.ecmo.context.LoginContext;
import org.example.ecmo.entity.SysUser;
import org.example.ecmo.mapper.SysUserMapper;
import org.example.ecmo.utils.PasswordUtil;
import org.example.ecmo.validator.LoginValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CredentialValidator implements LoginValidator {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public boolean validate(LoginContext context) {
        SysUser user = sysUserMapper.findByUsername(context.getUsername());
        if (user == null) {
            return fail(context);
        }
        
        if (verifyPassword(context.getPassword(), user.getPassword())) {
            context.setUser(user);
            
            if (isMd5Password(user.getPassword())) {
                upgradePassword(user, context.getPassword());
            }
            return true;
        }
        
        return fail(context);
    }
    
    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (isMd5Password(encodedPassword)) {
            return SaSecureUtil.md5(rawPassword).equals(encodedPassword);
        }
        return PasswordUtil.matches(rawPassword, encodedPassword);
    }
    
    private boolean isMd5Password(String password) {
        return password != null && password.length() == 32 && password.matches("[a-fA-F0-9]+");
    }
    
    private void upgradePassword(SysUser user, String rawPassword) {
        try {
            String newEncodedPassword = PasswordUtil.encode(rawPassword);
            user.setPassword(newEncodedPassword);
            sysUserMapper.updateById(user);
            log.info("用户 {} 密码已从MD5升级为BCrypt", user.getUsername());
        } catch (Exception e) {
            log.error("密码升级失败: {}", e.getMessage());
        }
    }
    
    private boolean fail(LoginContext context) {
        context.setShouldRecordFailedAttempt(true);
        context.setErrorMessage("用户名或密码错误");
        return false;
    }

    @Override
    public int getOrder() {
        return 6;
    }
}
