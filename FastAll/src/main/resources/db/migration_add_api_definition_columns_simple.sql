-- 简化版API定义表字段迁移脚本
-- 为api_definition表添加5个新字段（如果不存在）

-- 1. 添加operation_type字段
SET @operation_type_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'api_definition' 
    AND COLUMN_NAME = 'operation_type');

SET @sql = IF(@operation_type_exists = 0, 
    'ALTER TABLE api_definition ADD COLUMN operation_type VARCHAR(20) COMMENT "操作类型: list/detail/add/update/delete"',
    'SELECT "operation_type字段已存在，跳过"');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 添加tenant_app_id字段
SET @tenant_app_id_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'api_definition' 
    AND COLUMN_NAME = 'tenant_app_id');

SET @sql = IF(@tenant_app_id_exists = 0, 
    'ALTER TABLE api_definition ADD COLUMN tenant_app_id VARCHAR(100) COMMENT "租户应用ID"',
    'SELECT "tenant_app_id字段已存在，跳过"');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 添加return_fields字段
SET @return_fields_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'api_definition' 
    AND COLUMN_NAME = 'return_fields');

SET @sql = IF(@return_fields_exists = 0, 
    'ALTER TABLE api_definition ADD COLUMN return_fields JSON COMMENT "返回字段配置JSON"',
    'SELECT "return_fields字段已存在，跳过"');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 添加request_fields字段
SET @request_fields_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'api_definition' 
    AND COLUMN_NAME = 'request_fields');

SET @sql = IF(@request_fields_exists = 0, 
    'ALTER TABLE api_definition ADD COLUMN request_fields JSON COMMENT "请求字段配置JSON"',
    'SELECT "request_fields字段已存在，跳过"');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 添加condition_fields字段
SET @condition_fields_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'api_definition' 
    AND COLUMN_NAME = 'condition_fields');

SET @sql = IF(@condition_fields_exists = 0, 
    'ALTER TABLE api_definition ADD COLUMN condition_fields JSON COMMENT "查询条件字段配置JSON"',
    'SELECT "condition_fields字段已存在，跳过"');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '字段迁移完成' AS result;