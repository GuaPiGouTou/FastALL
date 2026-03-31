package org.example.ecmo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.example.ecmo.handler.EncryptTypeHandler;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
public class SysUser {

    private static final long serialVersionUID = 1L;
    private Long id;
    @TableField(value = "username")
    private String username;
    private String password;
    private String realName;
    @TableField(value = "id_card", typeHandler = EncryptTypeHandler.class)
    private String idCard;
    @TableField(value = "phone",typeHandler = EncryptTypeHandler.class)
    private String phone;
    @TableField(value = "email",typeHandler = EncryptTypeHandler.class)
    private String email;
    private Integer userType;
    private Long relationId;
    private String avatar;
    private Integer status;
    private String lastLoginIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
