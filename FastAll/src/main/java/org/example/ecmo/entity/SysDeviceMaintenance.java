package org.example.ecmo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 设备维保记录实体
 */
@Data
@TableName("sys_device_maintenance")
public class SysDeviceMaintenance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 维保日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date maintenanceDate;

    /**
     * 维保人
     */
    private String maintenancePerson;

    /**
     * 维保内容记录
     */
    private String content;

    /**
     * 建议下次校准/维保时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date nextCalibrationTime;

    /**
     * 数据创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
