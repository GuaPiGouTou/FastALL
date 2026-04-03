package org.example.ecmo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 统一对外验证码接口：
 * 前端统一访问 /api/captcha/**，内部转发到 AJ-Captcha 默认的 /captcha/** 接口。
 */
@RestController
@RequestMapping("/api/captcha")
public class CaptchaProxyController {

    @PostMapping("/get")
    public void get(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getRequestDispatcher("/captcha/get").forward(request, response);
    }

    @PostMapping("/check")
    public void check(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getRequestDispatcher("/captcha/check").forward(request, response);
    }
}

