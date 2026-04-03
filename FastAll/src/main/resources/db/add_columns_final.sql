-- 为api_definition表添加5个新字段
-- 使用IF NOT EXISTS语法（MySQL 5.7.26+支持）

ALTER TABLE api_definition 
ADD COLUMN IF NOT EXISTS operation_type VARCHAR(20) COMMENT '操作类型: list/detail/add/update/delete';

ALTER TABLE api_definition 
ADD COLUMN IF NOT EXISTS tenant_app_id VARCHAR(100) COMMENT '租户应用ID';

ALTER TABLE api_definition 
ADD COLUMN IF NOT EXISTS return_fields JSON COMMENT '返回字段配置JSON';

ALTER TABLE api_definition 
ADD COLUMN IF NOT EXISTS request_fields JSON COMMENT '请求字段配置JSON';

ALTER TABLE api_definition 
ADD COLUMN IF NOT EXISTS condition_fields JSON COMMENT '查询条件字段配置JSON';

SHOW WARNINGS;
SELECT '字段添加完成' AS result;