package org.example.ecmo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 系统访问记录/登录日志实体类
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 访问ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户账号 */
    private String username;

    /** 登录IP地址 */
    private String ipAddress;

    /** 登录地点 */
    private String loginLocation;

    /** 浏览器类型 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 设备类型 (PC/Mobile/Tablet) */
    private String deviceType;

    /** 原始 User-Agent 字符串 */
    private String userAgent;

    /** 登录状态 (0成功 1失败) */
    private Integer status;

    /** 提示消息 */
    private String msg;

    /** 访问时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginTime;
}
