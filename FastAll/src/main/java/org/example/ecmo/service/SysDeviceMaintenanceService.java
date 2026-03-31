package org.example.ecmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ecmo.entity.SysDeviceMaintenance;

/**
 * 设备维保记录Service接口
 */
public interface SysDeviceMaintenanceService extends IService<SysDeviceMaintenance> {
    
    /**
     * 保存维保记录并同步更新设备状态与校准周期
     */
    boolean saveMaintenance(SysDeviceMaintenance maintenance);
}
