-- 为api_definition表添加5个新字段（无注释版）
ALTER TABLE api_definition 
ADD COLUMN IF NOT EXISTS operation_type VARCHAR(20);

ALTER TABLE api_definition 
ADD COLUMN IF NOT EXISTS tenant_app_id VARCHAR(100);

ALTER TABLE api_definition 
ADD COLUMN IF NOT EXISTS return_fields JSON;

ALTER TABLE api_definition 
ADD COLUMN IF NOT EXISTS request_fields JSON;

ALTER TABLE api_definition 
ADD COLUMN IF NOT EXISTS condition_fields JSON;

SELECT '字段添加完成' AS result;