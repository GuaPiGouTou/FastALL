package org.example.ecmo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysPermission {

    private Long id;
    private String permissionName;
    private String permissionCode;
    private String resourceType;
    private String resourceUrl;
    private Long parentId;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}