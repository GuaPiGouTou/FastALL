-- FastCRUD 核心数据表结构

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(200) COMMENT '描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
  `resource_type` VARCHAR(20) NOT NULL COMMENT '资源类型',
  `resource_url` VARCHAR(200) COMMENT '资源路径',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID',
  `icon` VARCHAR(50) COMMENT '图标',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 字段配置表
CREATE TABLE IF NOT EXISTS `sys_field_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `table_name` VARCHAR(100) NOT NULL COMMENT '表名',
  `prop` VARCHAR(100) NOT NULL COMMENT '字段名',
  `label` VARCHAR(100) NOT NULL COMMENT '字段标签',
  `ui_type` VARCHAR(50) NOT NULL COMMENT 'UI类型',
  `options` JSON COMMENT '选项配置',
  `is_show_in_list` TINYINT NOT NULL DEFAULT 1 COMMENT '是否在列表中显示',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_table_prop` (`table_name`, `prop`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段配置表';

-- 模块信息表
CREATE TABLE IF NOT EXISTS `module` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `module_name` VARCHAR(100) NOT NULL COMMENT '模块名称',
  `table_name` VARCHAR(100) NOT NULL COMMENT '表名',
  `description` VARCHAR(200) COMMENT '描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_module_name` (`module_name`),
  UNIQUE KEY `uk_table_name` (`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模块信息表';

-- 初始化管理员角色和权限
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES
('管理员', 'admin', '系统管理员'),
('普通用户', 'user', '普通用户');

-- 初始化基础权限
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `resource_type`, `resource_url`, `parent_id`, `icon`, `sort_order`) VALUES
('系统管理', 'system:manage', 'menu', '/system', 0, 'System', 1),
('用户管理', 'user:manage', 'menu', '/system/users', 1, 'User', 2),
('角色管理', 'role:manage', 'menu', '/system/roles', 1, 'Role', 3),
('权限管理', 'permission:manage', 'menu', '/system/permissions', 1, 'Permission', 4),
('模块管理', 'module:manage', 'menu', '/system/modules', 1, 'Module', 5),
('CRUD管理', 'crud:manage', 'menu', '/crud', 0, 'Crud', 6),
('用户创建', 'user:create', 'button', '/api/users', 2, '', 1),
('用户查看', 'user:read', 'button', '/api/users', 2, '', 2),
('用户更新', 'user:update', 'button', '/api/users', 2, '', 3),
('用户删除', 'user:delete', 'button', '/api/users', 2, '', 4);

-- API定义表
CREATE TABLE IF NOT EXISTS `api_definition` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `api_name` VARCHAR(100) NOT NULL COMMENT 'API名称',
  `api_path` VARCHAR(200) NOT NULL COMMENT 'API路径',
  `api_method` VARCHAR(10) NOT NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
  `module_name` VARCHAR(100) COMMENT '所属模块',
  `table_name` VARCHAR(100) COMMENT '关联表名',
  `description` VARCHAR(500) COMMENT 'API描述',
  `request_params` JSON COMMENT '请求参数配置',
  `response_params` JSON COMMENT '响应参数配置',
  `sql_template` TEXT COMMENT 'SQL模板',
  `is_custom_sql` TINYINT NOT NULL DEFAULT 0 COMMENT '是否自定义SQL(0:否,1:是)',
  `auth_required` TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要认证(0:否,1:是)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_path_method` (`api_path`, `api_method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API定义表';

-- API调用日志表
CREATE TABLE IF NOT EXISTS `api_call_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `api_id` BIGINT NOT NULL COMMENT 'API ID',
  `api_path` VARCHAR(200) NOT NULL COMMENT 'API路径',
  `api_method` VARCHAR(10) NOT NULL COMMENT '请求方法',
  `user_id` BIGINT COMMENT '用户ID',
  `user_name` VARCHAR(50) COMMENT '用户名',
  `request_params` TEXT COMMENT '请求参数',
  `response_data` TEXT COMMENT '响应数据',
  `status` VARCHAR(20) NOT NULL COMMENT '执行状态(success/error)',
  `error_msg` TEXT COMMENT '错误信息',
  `ip_address` VARCHAR(50) COMMENT 'IP地址',
  `execute_time` INT COMMENT '执行时间(毫秒)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_api_id` (`api_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API调用日志表';

-- 给管理员角色分配所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) SELECT 1, id FROM `sys_permission`;