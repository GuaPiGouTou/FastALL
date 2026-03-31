-- 试剂管理表
CREATE TABLE IF NOT EXISTS `sys_reagent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) NOT NULL COMMENT '试剂名称',
  `cas_number` varchar(50) DEFAULT NULL COMMENT 'CAS号',
  `purity` varchar(50) DEFAULT NULL COMMENT '纯度/规格',
  `unit` varchar(20) DEFAULT 'ml' COMMENT '单位',
  `stock_quantity` decimal(10,2) DEFAULT '0.00' COMMENT '即时库存',
  `safe_level` varchar(20) DEFAULT 'Normal' COMMENT '安全等级',
  `storage_condition` varchar(100) DEFAULT NULL COMMENT '存储条件',
  `expiration_date` datetime DEFAULT NULL COMMENT '有效期至',
  `batch_number` varchar(100) DEFAULT NULL COMMENT '批次号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试剂管理表';

-- 设备管理表
CREATE TABLE IF NOT EXISTS `sys_device` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_code` varchar(50) NOT NULL COMMENT '设备编号',
  `model` varchar(100) DEFAULT NULL COMMENT '型号',
  `serial_number` varchar(100) DEFAULT NULL COMMENT '序列号',
  `department` varchar(100) DEFAULT NULL COMMENT '负责科室',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0闲置, 1在用, 2维保, 3故障',
  `last_calibration_time` datetime DEFAULT NULL COMMENT '上次校准时间',
  `next_calibration_time` datetime DEFAULT NULL COMMENT '下次校准时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_device_code` (`device_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备管理表';

-- 实验任务管理表
CREATE TABLE IF NOT EXISTS `sys_experiment_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_name` varchar(255) NOT NULL COMMENT '任务名称',
  `priority` tinyint(4) DEFAULT '1' COMMENT '优先级: 0低, 1中, 2高',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态: 0草稿, 1待执行, 2执行中, 3复核, 4完成',
  `start_time` datetime DEFAULT NULL COMMENT '计划开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '计划结束时间',
  `charge_user_id` bigint(20) DEFAULT NULL COMMENT '负责人ID',
  `charge_user_name` varchar(100) DEFAULT NULL COMMENT '负责人姓名',
  `description` text COMMENT '任务描述',
  `result_summary` text COMMENT '实验结果摘要',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验任务管理表';

-- 4. 设备维保记录表
CREATE TABLE IF NOT EXISTS sys_device_maintenance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    maintenance_date DATETIME NOT NULL COMMENT '维保日期',
    maintenance_person VARCHAR(64) NOT NULL COMMENT '维保人',
    content TEXT COMMENT '维保内容记录',
    next_calibration_time DATETIME COMMENT '建议下次校准/维保时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '数据创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维保记录表';
