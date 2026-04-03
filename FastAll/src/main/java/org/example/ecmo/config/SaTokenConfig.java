package org.example.ecmo.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            SaRouter.match("/api/**")
                // 验证码接口允许匿名访问（避免 NotLoginException）
                .notMatch("/api/captcha/get")
                .notMatch("/api/captcha/check")
                .notMatch("/api/auth/**")
                .notMatch("/api/file/upload")
                .notMatch("/api/file/upload/**")
                .notMatch("/api/file/chunk/**")
                .notMatch("/api/file/biz-files/**")
                .notMatch("/doc.html", "/swagger-resources/**", "/webjars/**", "/v2/api-docs/**")
                .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
