package org.example.ecmo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.SysDeviceMaintenance;
import org.example.ecmo.service.SysDeviceMaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备维保记录控制层
 */
@RestController
@RequestMapping("/api/device-maintenance")
public class SysDeviceMaintenanceController {

    @Autowired
    private SysDeviceMaintenanceService sysDeviceMaintenanceService;

    /**
     * 获取指定设备的维保记录列表
     */
    @AuditLog(title = "查询维保记录", businessType = 1)
    @GetMapping("/list/{deviceId}")
    public JsonResult<List<SysDeviceMaintenance>> list(@PathVariable Long deviceId) {
        LambdaQueryWrapper<SysDeviceMaintenance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDeviceMaintenance::getDeviceId, deviceId);
        wrapper.orderByDesc(SysDeviceMaintenance::getMaintenanceDate);
        return new JsonResult<>(200, sysDeviceMaintenanceService.list(wrapper), "查询成功");
    }

    /**
     * 新增维保记录
     */
    @AuditLog(title = "新增维保记录", businessType = 2)
    @PostMapping("/add")
    public JsonResult<Boolean> add(@RequestBody SysDeviceMaintenance maintenance) {
        return new JsonResult<>(200, sysDeviceMaintenanceService.saveMaintenance(maintenance), "维保记录已保存");
    }
}
