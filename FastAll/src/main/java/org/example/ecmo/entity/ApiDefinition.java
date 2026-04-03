package org.example.ecmo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
@TableName("api_definition")
public class ApiDefinition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String apiName;

    private String apiCode;

    private String apiPath;

    private String apiMethod;

    private String apiVersion;

    private Long groupId;

    private String groupName;

    private String tags;

    private String description;

    private Long datasourceId;

    private String datasourceType;

    private Long dataCenterGroupId;

    private String tableName;

    private String execMode;

    private String sqlTemplate;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object flowConfig;

    private Integer responseWrapper;

    private String responseTemplate;

    private String authType;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object authConfig;

    private Integer rateLimit;

    private String ipWhitelist;

    private String ipBlacklist;

    private Integer corsEnabled;

    private Integer mockEnabled;

    private String mockData;

    private String environment;

    private String status;

    // 兼容：部分历史库可能尚未补齐 operation_type 等扩展字段
    // exist=false 会让 MyBatis-Plus 在 insert/select 时跳过该字段，避免 Unknown column 导致创建失败
    @TableField(exist = false)
    private String operationType;
    
    @TableField(exist = false)
    private String tenantAppId;
    
    @TableField(exist = false, typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object returnFields;
    
    @TableField(exist = false, typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object requestFields;
    
    @TableField(exist = false, typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object conditionFields;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime publishTime;

    private String publishUser;

    private Long callCount;

    private Integer avgExecuteTime;

    private Long errorCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private String createUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    private String updateUser;

    private Integer deleted;
}
