package org.example.ecmo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.SysDevice;
import org.example.ecmo.service.SysDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 设备管理控制层
 */
@RestController
@RequestMapping("/api/device")
public class SysDeviceController {

    @Autowired
    private SysDeviceService sysDeviceService;

    @AuditLog(title = "查询设备列表", businessType = 1)
    @GetMapping("/list")
    public JsonResult<IPage<SysDevice>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String deviceCode) {
        Page<SysDevice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(deviceCode), SysDevice::getDeviceCode, deviceCode);
        wrapper.orderByDesc(SysDevice::getCreateTime);
        return new JsonResult<>(200, sysDeviceService.page(page, wrapper), "查询成功");
    }

    @AuditLog(title = "新增设备", businessType = 2)
    @PostMapping("/add")
    public JsonResult<Boolean> add(@RequestBody SysDevice device) {
        return new JsonResult<>(200, sysDeviceService.save(device), "新增成功");
    }

    @AuditLog(title = "修改设备", businessType = 2)
    @PutMapping("/update")
    public JsonResult<Boolean> update(@RequestBody SysDevice device) {
        return new JsonResult<>(200, sysDeviceService.updateById(device), "修改成功");
    }

    @AuditLog(title = "删除设备", businessType = 3)
    @DeleteMapping("/delete/{id}")
    public JsonResult<Boolean> delete(@PathVariable Long id) {
        return new JsonResult<>(200, sysDeviceService.removeById(id), "删除成功");
    }
}
