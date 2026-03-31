package org.example.ecmo.validator.impl;

import cn.hutool.core.util.StrUtil;
import org.example.ecmo.context.LoginContext;
import org.example.ecmo.validator.LoginValidator;
import org.springframework.stereotype.Component;

@Component
public class UsernameValidator implements LoginValidator {

    @Override
    public boolean validate(LoginContext context) {
        if (StrUtil.isBlank(context.getUsername())) {
            context.setErrorMessage("用户名不能为空");
            return false;
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
