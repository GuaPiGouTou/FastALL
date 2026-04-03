-- =====================================================
-- API Generator Enhanced Tables
-- API生成器增强版数据库表结构
-- =====================================================

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS `api_flow_node`;
DROP TABLE IF EXISTS `api_parameter`;
DROP TABLE IF EXISTS `api_version`;
DROP TABLE IF EXISTS `api_definition`;

-- 如果表已存在，添加缺失的列
ALTER TABLE `api_definition` 
ADD COLUMN IF NOT EXISTS `operation_type` VARCHAR(20) COMMENT '操作类型: list/detail/add/update/delete',
ADD COLUMN IF NOT EXISTS `tenant_app_id` VARCHAR(100) COMMENT '租户应用ID',
ADD COLUMN IF NOT EXISTS `return_fields` JSON COMMENT '返回字段配置JSON',
ADD COLUMN IF NOT EXISTS `request_fields` JSON COMMENT '请求字段配置JSON',
ADD COLUMN IF NOT EXISTS `condition_fields` JSON COMMENT '查询条件字段配置JSON';

-- =====================================================
-- 1. API定义表 (增强版)
-- =====================================================
CREATE TABLE `api_definition` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `api_name` VARCHAR(100) NOT NULL COMMENT 'API名称',
  `api_code` VARCHAR(100) NOT NULL COMMENT 'API唯一标识编码',
  `api_path` VARCHAR(200) NOT NULL COMMENT 'API路径，支持路径变量如 /users/{id}',
  `api_method` VARCHAR(10) NOT NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
  `api_version` VARCHAR(20) NOT NULL DEFAULT 'v1' COMMENT 'API版本号',
  `group_id` BIGINT COMMENT '所属分组ID',
  `group_name` VARCHAR(100) COMMENT '所属分组名称',
  `tags` VARCHAR(500) COMMENT '标签，逗号分隔',
  `description` VARCHAR(1000) COMMENT 'API描述',
  
  -- 数据源配置
  `datasource_id` BIGINT COMMENT '数据源ID',
  `datasource_type` VARCHAR(20) DEFAULT 'mysql' COMMENT '数据源类型(mysql/postgresql/mongodb/api)',
  `data_center_group_id` BIGINT COMMENT '数据中心分组ID',
  `table_name` VARCHAR(100) COMMENT '关联表名',
  
  -- 执行模式配置
  `exec_mode` VARCHAR(20) NOT NULL DEFAULT 'AUTO' COMMENT '执行模式: AUTO-自动CRUD, SQL-SQL模式, FLOW-流程编排',
  `sql_template` TEXT COMMENT 'SQL模板',
  `flow_config` JSON COMMENT '流程编排配置JSON',
  
  -- 响应配置
  `response_wrapper` TINYINT NOT NULL DEFAULT 1 COMMENT '是否包装响应(0:否,1:是)',
  `response_template` TEXT COMMENT '响应模板JSON',
  
  -- 安全配置
  `auth_type` VARCHAR(20) NOT NULL DEFAULT 'TOKEN' COMMENT '鉴权类型: PUBLIC-公开, TOKEN-JWT, API_KEY',
  `auth_config` JSON COMMENT '鉴权配置JSON',
  `rate_limit` INT DEFAULT 0 COMMENT '限流配置(每分钟最大请求数, 0表示不限)',
  `ip_whitelist` TEXT COMMENT 'IP白名单，逗号分隔',
  `ip_blacklist` TEXT COMMENT 'IP黑名单，逗号分隔',
  `cors_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许跨域(0:否,1:是)',
  
  -- Mock配置
  `mock_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用Mock(0:否,1:是)',
  `mock_data` TEXT COMMENT 'Mock数据JSON',
  
  -- 环境配置
    `environment` VARCHAR(20) NOT NULL DEFAULT 'dev' COMMENT '环境: dev/test/prod',
    
    -- 标准CRUD配置
    `operation_type` VARCHAR(20) COMMENT '操作类型: list/detail/add/update/delete',
    `tenant_app_id` VARCHAR(100) COMMENT '租户应用ID',
    `return_fields` JSON COMMENT '返回字段配置JSON',
    `request_fields` JSON COMMENT '请求字段配置JSON',
    `condition_fields` JSON COMMENT '查询条件字段配置JSON',
    
    -- 状态管理
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态: draft-草稿, published-已发布, offline-已下线',
  `publish_time` DATETIME COMMENT '发布时间',
  `publish_user` VARCHAR(50) COMMENT '发布人',
  
  -- 统计信息
  `call_count` BIGINT DEFAULT 0 COMMENT '调用次数',
  `avg_execute_time` INT DEFAULT 0 COMMENT '平均执行时间(ms)',
  `error_count` BIGINT DEFAULT 0 COMMENT '错误次数',
  
  -- 审计字段
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` VARCHAR(50) COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0:未删除,1:已删除)',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_code_version` (`api_code`, `api_version`),
  KEY `idx_api_path_method` (`api_path`, `api_method`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_status` (`status`),
  KEY `idx_environment` (`environment`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API定义表';

-- =====================================================
-- 2. API参数配置表
-- =====================================================
CREATE TABLE `api_parameter` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `api_id` BIGINT NOT NULL COMMENT 'API定义ID',
  `api_version_id` BIGINT COMMENT 'API版本ID',
  
  -- 参数基本信息
  `param_category` VARCHAR(20) NOT NULL COMMENT '参数类别: REQUEST-请求参数, RESPONSE-响应参数',
  `param_position` VARCHAR(20) COMMENT '参数位置: QUERY, PATH, BODY, HEADER',
  `param_name` VARCHAR(100) NOT NULL COMMENT '参数名称',
  `param_label` VARCHAR(100) COMMENT '参数标签/显示名称',
  `param_type` VARCHAR(50) NOT NULL COMMENT '参数类型: String, Integer, Float, Boolean, Array, Object',
  `param_format` VARCHAR(50) COMMENT '格式: date, datetime, email, phone, uuid等',
  
  -- 参数约束
  `required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填(0:否,1:是)',
  `default_value` VARCHAR(500) COMMENT '默认值',
  `min_length` INT COMMENT '最小长度',
  `max_length` INT COMMENT '最大长度',
  `min_value` DECIMAL(20,6) COMMENT '最小值',
  `max_value` DECIMAL(20,6) COMMENT '最大值',
  `regex_pattern` VARCHAR(500) COMMENT '正则校验表达式',
  `enum_values` TEXT COMMENT '枚举值JSON数组',
  
  -- 嵌套结构
  `parent_id` BIGINT DEFAULT 0 COMMENT '父参数ID，用于嵌套结构',
  `object_schema` JSON COMMENT 'Object类型的Schema定义',
  
  -- 字段映射
  `source_field` VARCHAR(100) COMMENT '源字段名(数据库字段)',
  `target_field` VARCHAR(100) COMMENT '目标字段名(返回字段)',
  `field_transform` VARCHAR(100) COMMENT '字段转换表达式',
  `sensitive` TINYINT NOT NULL DEFAULT 0 COMMENT '是否敏感字段(0:否,1:是-脱敏处理)',
  
  -- Mock配置
  `mock_rule` VARCHAR(100) COMMENT 'Mock规则: random, range, faker等',
  `mock_value` VARCHAR(500) COMMENT 'Mock固定值',
  
  -- 其他
  `description` VARCHAR(500) COMMENT '参数描述',
  `example_value` VARCHAR(500) COMMENT '示例值',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_api_id` (`api_id`),
  KEY `idx_api_version_id` (`api_version_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API参数配置表';

-- =====================================================
-- 3. API版本管理表
-- =====================================================
CREATE TABLE `api_version` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `api_id` BIGINT NOT NULL COMMENT 'API定义ID',
  `version_no` VARCHAR(20) NOT NULL COMMENT '版本号',
  `version_name` VARCHAR(100) COMMENT '版本名称',
  `version_desc` VARCHAR(500) COMMENT '版本描述',
  
  -- 版本快照
  `snapshot` JSON COMMENT '版本快照(完整的API配置JSON)',
  
  -- 变更信息
  `change_type` VARCHAR(20) NOT NULL COMMENT '变更类型: CREATE-创建, UPDATE-更新, PUBLISH-发布, OFFLINE-下线',
  `change_log` TEXT COMMENT '变更日志',
  
  -- 状态
  `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active-活跃, deprecated-废弃, archived-归档',
  `is_current` TINYINT NOT NULL DEFAULT 0 COMMENT '是否当前版本(0:否,1:是)',
  
  -- 审计
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` VARCHAR(50) COMMENT '创建人',
  
  PRIMARY KEY (`id`),
  KEY `idx_api_id` (`api_id`),
  KEY `idx_version_no` (`version_no`),
  UNIQUE KEY `uk_api_version` (`api_id`, `version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API版本管理表';

-- =====================================================
-- 4. API流程节点表 (可视化流程编排)
-- =====================================================
CREATE TABLE `api_flow_node` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `api_id` BIGINT NOT NULL COMMENT 'API定义ID',
  `flow_id` VARCHAR(50) NOT NULL COMMENT '流程ID',
  
  -- 节点信息
  `node_id` VARCHAR(50) NOT NULL COMMENT '节点ID',
  `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
  `node_type` VARCHAR(50) NOT NULL COMMENT '节点类型: DATASOURCE-数据源, LOGIC-逻辑, PROCESSOR-处理, SCRIPT-脚本, OUTPUT-输出',
  `node_config` JSON COMMENT '节点配置JSON',
  
  -- 连接信息
  `prev_nodes` VARCHAR(500) COMMENT '前置节点ID列表，逗号分隔',
  `next_nodes` VARCHAR(500) COMMENT '后置节点ID列表，逗号分隔',
  
  -- 执行配置
  `exec_order` INT NOT NULL DEFAULT 0 COMMENT '执行顺序',
  `condition_expr` VARCHAR(500) COMMENT '执行条件表达式',
  `timeout` INT DEFAULT 30000 COMMENT '超时时间(ms)',
  `retry_count` INT DEFAULT 0 COMMENT '重试次数',
  
  -- 其他
  `position_x` INT DEFAULT 0 COMMENT '画布X坐标',
  `position_y` INT DEFAULT 0 COMMENT '画布Y坐标',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_api_id` (`api_id`),
  KEY `idx_flow_id` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API流程节点表';

-- =====================================================
-- 5. API分组表
-- =====================================================
CREATE TABLE `api_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_name` VARCHAR(100) NOT NULL COMMENT '分组名称',
  `group_code` VARCHAR(100) NOT NULL COMMENT '分组编码',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分组ID',
  `icon` VARCHAR(50) COMMENT '图标',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `description` VARCHAR(500) COMMENT '描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_code` (`group_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API分组表';

-- =====================================================
-- 6. 数据源配置表
-- =====================================================
CREATE TABLE `api_datasource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ds_name` VARCHAR(100) NOT NULL COMMENT '数据源名称',
  `ds_code` VARCHAR(100) NOT NULL COMMENT '数据源编码',
  `ds_type` VARCHAR(20) NOT NULL COMMENT '数据源类型: mysql, postgresql, mongodb, redis, http_api',
  
  -- 连接配置
  `host` VARCHAR(100) COMMENT '主机地址',
  `port` INT COMMENT '端口',
  `database` VARCHAR(100) COMMENT '数据库名',
  `username` VARCHAR(100) COMMENT '用户名',
  `password` VARCHAR(500) COMMENT '密码(加密存储)',
  `extra_config` JSON COMMENT '额外配置JSON',
  
  -- HTTP API配置
  `api_url` VARCHAR(500) COMMENT 'API地址',
  `api_method` VARCHAR(10) COMMENT 'API方法',
  `api_headers` JSON COMMENT 'API请求头',
  `api_auth_type` VARCHAR(20) COMMENT 'API鉴权类型',
  `api_auth_config` JSON COMMENT 'API鉴权配置',
  
  -- 环境配置
  `environment` VARCHAR(20) NOT NULL DEFAULT 'dev' COMMENT '环境: dev/test/prod',
  
  -- 状态
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `last_test_time` DATETIME COMMENT '最后测试时间',
  `last_test_result` VARCHAR(20) COMMENT '最后测试结果: success/failed',
  
  -- 审计
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ds_code` (`ds_code`, `environment`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 初始化API分组
INSERT INTO `api_group` (`group_name`, `group_code`, `parent_id`, `sort_order`, `description`) VALUES
('默认分组', 'default', 0, 0, '默认API分组'),
('用户管理', 'user', 0, 1, '用户相关API'),
('系统管理', 'system', 0, 2, '系统管理相关API');

-- 初始化数据源
INSERT INTO `api_datasource` (`ds_name`, `ds_code`, `ds_type`, `host`, `port`, `database`, `username`, `environment`, `status`) VALUES
('主数据库', 'master', 'mysql', '124.223.43.124', 3306, 'FastCRUD', 'root', 'prod', 1),
('测试数据库', 'master', 'mysql', '124.223.43.124', 3306, 'FastCRUD', 'root', 'dev', 1);
