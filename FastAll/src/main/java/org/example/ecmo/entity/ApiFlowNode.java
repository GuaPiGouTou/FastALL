package org.example.ecmo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
@TableName("api_flow_node")
public class ApiFlowNode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long apiId;

    private String flowId;

    private String nodeId;

    private String nodeName;

    private String nodeType;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object nodeConfig;

    private String prevNodes;

    private String nextNodes;

    private Integer execOrder;

    private String conditionExpr;

    private Integer timeout;

    private Integer retryCount;

    private Integer positionX;

    private Integer positionY;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
