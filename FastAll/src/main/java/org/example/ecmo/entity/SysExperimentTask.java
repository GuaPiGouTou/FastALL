package org.example.ecmo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 实验任务管理实体
 */
@Data
@TableName("sys_experiment_task")
public class SysExperimentTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 优先级: 0低, 1中, 2高
     */
    private Integer priority;

    /**
     * 状态: 0草稿, 1待执行, 2执行中, 3复核, 4完成
     */
    private Integer status;

    /**
     * 计划开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 计划结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 负责人ID
     */
    private Long chargeUserId;

    /**
     * 负责人姓名
     */
    private String chargeUserName;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 实验结果摘要
     */
    private String resultSummary;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
