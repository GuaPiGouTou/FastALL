package org.example.ecmo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ecmo.entity.SysDevice;
import org.example.ecmo.entity.SysDeviceMaintenance;
import org.example.ecmo.mapper.SysDeviceMaintenanceMapper;
import org.example.ecmo.service.SysDeviceMaintenanceService;
import org.example.ecmo.service.SysDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 设备维保记录Service实现类
 */
@Service
public class SysDeviceMaintenanceServiceImpl extends ServiceImpl<SysDeviceMaintenanceMapper, SysDeviceMaintenance> implements SysDeviceMaintenanceService {

    @Autowired
    private SysDeviceService sysDeviceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMaintenance(SysDeviceMaintenance maintenance) {
        // 1. 保存维保记录
        boolean saved = this.save(maintenance);
        if (!saved) return false;

        // 2. 同步更新设备状态与校准周期
        SysDevice device = sysDeviceService.getById(maintenance.getDeviceId());
        if (device != null) {
            // 设置为“闲置”或恢复为“在用”（此处暂定为恢复为在用，或者维持原样，通常维保后状态会恢复）
            if (device.getStatus() == 2 || device.getStatus() == 3) {
                device.setStatus(1); // 恢复为在用
            }
            device.setLastCalibrationTime(maintenance.getMaintenanceDate());
            device.setNextCalibrationTime(maintenance.getNextCalibrationTime());
            return sysDeviceService.updateById(device);
        }
        
        return true;
    }
}
