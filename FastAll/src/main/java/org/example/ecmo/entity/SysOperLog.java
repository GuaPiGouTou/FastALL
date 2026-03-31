package org.example.ecmo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private Integer businessType;
    
    private String method;
    
    private String requestMethod;
    
    private String operUrl;
    
    private String operIp;
    
    private String operParam;
    
    private String jsonResult;
    
    private Integer status;
    
    private String errorMsg;

    /** 操作人 ID */
    private Long operId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime operTime;
}
