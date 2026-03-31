package org.example.ecmo.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.SysUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface SysUserService {
    /**
     * 登录接口
     * */
    JsonResult<SaTokenInfo> login(Map<String, String> params);
    /**
    * 注册接口
    * */
    JsonResult<String> regit (SysUser user);
    /**
     * 检查用户名是否存在
     * */
    boolean checkUserName(String userName);
    /**
     * 检查邮箱是否存在
     * */
    boolean checkEmail(String email);
    /**
     * 查询所有的用户名|用于项目启动时初始化布隆过滤器
     * */
    List<String> findAllUsernames();


    JsonResult<String> logout();
}
