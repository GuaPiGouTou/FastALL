package org.example.ecmo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ecmo.entity.SysDevice;
import org.example.ecmo.mapper.SysDeviceMapper;
import org.example.ecmo.service.SysDeviceService;
import org.springframework.stereotype.Service;

@Service
public class SysDeviceServiceImpl extends ServiceImpl<SysDeviceMapper, SysDevice> implements SysDeviceService {
}
