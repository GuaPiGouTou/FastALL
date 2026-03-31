package org.example.ecmo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ecmo.entity.SysExperimentTask;
import org.example.ecmo.mapper.SysExperimentTaskMapper;
import org.example.ecmo.service.SysExperimentTaskService;
import org.springframework.stereotype.Service;

@Service
public class SysExperimentTaskServiceImpl extends ServiceImpl<SysExperimentTaskMapper, SysExperimentTask> implements SysExperimentTaskService {
}
