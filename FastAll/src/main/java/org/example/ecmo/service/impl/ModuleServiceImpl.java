package org.example.ecmo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ecmo.entity.Module;
import org.example.ecmo.mapper.ModuleMapper;
import org.example.ecmo.service.ModuleService;
import org.springframework.stereotype.Service;

@Service
public class ModuleServiceImpl extends ServiceImpl<ModuleMapper, Module> implements ModuleService {
}
