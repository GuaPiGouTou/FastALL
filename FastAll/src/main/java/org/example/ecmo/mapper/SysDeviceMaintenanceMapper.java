package org.example.ecmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.ecmo.entity.SysDeviceMaintenance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备维保记录Mapper接口
 */
@Mapper
public interface SysDeviceMaintenanceMapper extends BaseMapper<SysDeviceMaintenance> {
}
