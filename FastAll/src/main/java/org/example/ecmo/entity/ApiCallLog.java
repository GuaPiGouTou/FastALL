package org.example.ecmo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
@TableName("api_call_log")
public class ApiCallLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long apiId;

    private String apiPath;

    private String apiMethod;

    private Long userId;

    private String userName;

    private String requestParams;

    private String responseData;

    private String status;

    private String errorMsg;

    private String ipAddress;

    private Integer executeTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
