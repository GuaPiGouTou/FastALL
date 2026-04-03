-- =====================================================
-- API定义表字段扩展迁移脚本
-- 为api_definition表添加标准CRUD配置字段
-- 执行时间: 2026-04-03
-- =====================================================

-- 1. 检查表是否存在
SELECT COUNT(*) INTO @table_exists FROM information_schema.tables 
WHERE table_schema = DATABASE() AND table_name = 'api_definition';

-- 2. 如果表存在，添加新字段
SET @sql = NULL;
SELECT
  GROUP_CONCAT(
    CONCAT(
      'ALTER TABLE api_definition ADD COLUMN IF NOT EXISTS ',
      column_def
    ) SEPARATOR ';\n'
  ) INTO @sql
FROM (
  SELECT 'operation_type VARCHAR(20) COMMENT "操作类型: list/detail/add/update/delete"' AS column_def
  UNION ALL
  SELECT 'tenant_app_id VARCHAR(100) COMMENT "租户应用ID"'
  UNION ALL
  SELECT 'return_fields JSON COMMENT "返回字段配置JSON"'
  UNION ALL
  SELECT 'request_fields JSON COMMENT "请求字段配置JSON"'
  UNION ALL
  SELECT 'condition_fields JSON COMMENT "查询条件字段配置JSON"'
) AS columns_to_add;

-- 3. 执行添加字段的SQL
IF @table_exists > 0 AND @sql IS NOT NULL THEN
  PREPARE stmt FROM @sql;
  EXECUTE stmt;
  DEALLOCATE PREPARE stmt;
  
  SELECT '字段添加成功' AS result;
ELSEIF @table_exists = 0 THEN
  SELECT 'api_definition表不存在，请先创建表' AS result;
ELSE
  SELECT '无需添加字段，所有字段已存在' AS result;
END IF;

-- =====================================================
-- 简化版本（如果上面存储过程有问题，使用下面的单条SQL语句）
-- =====================================================

-- 添加 operation_type 字段
-- ALTER TABLE api_definition 
-- ADD COLUMN IF NOT EXISTS operation_type VARCHAR(20) COMMENT '操作类型: list/detail/add/update/delete';

-- 添加 tenant_app_id 字段
-- ALTER TABLE api_definition 
-- ADD COLUMN IF NOT EXISTS tenant_app_id VARCHAR(100) COMMENT '租户应用ID';

-- 添加 return_fields 字段
-- ALTER TABLE api_definition 
-- ADD COLUMN IF NOT EXISTS return_fields JSON COMMENT '返回字段配置JSON';

-- 添加 request_fields 字段
-- ALTER TABLE api_definition 
-- ADD COLUMN IF NOT EXISTS request_fields JSON COMMENT '请求字段配置JSON';

-- 添加 condition_fields 字段
-- ALTER TABLE api_definition 
-- ADD COLUMN IF NOT EXISTS condition_fields JSON COMMENT '查询条件字段配置JSON';