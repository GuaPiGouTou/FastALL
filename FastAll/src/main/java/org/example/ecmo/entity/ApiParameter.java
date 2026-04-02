package org.example.ecmo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("api_parameter")
public class ApiParameter {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long apiId;

    private Long apiVersionId;

    private String paramCategory;

    private String paramPosition;

    private String paramName;

    private String paramLabel;

    private String paramType;

    private String paramFormat;

    private Integer required;

    private String defaultValue;

    private Integer minLength;

    private Integer maxLength;

    private BigDecimal minValue;

    private BigDecimal maxValue;

    private String regexPattern;

    private String enumValues;

    private Long parentId;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object objectSchema;

    private String sourceField;

    private String targetField;

    private String fieldTransform;

    @TableField("is_sensitive")
    private Integer sensitive;

    private String mockRule;

    private String mockValue;

    private String description;

    private String exampleValue;

    private Integer sortOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
