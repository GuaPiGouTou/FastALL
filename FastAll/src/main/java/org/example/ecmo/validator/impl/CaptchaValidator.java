package org.example.ecmo.validator.impl;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.example.ecmo.context.LoginContext;
import org.example.ecmo.validator.LoginValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CaptchaValidator implements LoginValidator {

    @Autowired
    private CaptchaService captchaService;

    @Override
    public boolean validate(LoginContext context) {
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(context.getCaptchaVerification());
        ResponseModel response = captchaService.verification(captchaVO);
        log.info("验证码校验结果：{}", context.getCaptchaVerification());
        
        if (!response.isSuccess()) {
            context.setErrorMessage("登录失败，" + response.getRepMsg());
            return false;
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
