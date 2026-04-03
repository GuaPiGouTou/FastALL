ALTER TABLE api_definition ADD COLUMN operation_type VARCHAR(20);
ALTER TABLE api_definition ADD COLUMN tenant_app_id VARCHAR(100);
ALTER TABLE api_definition ADD COLUMN return_fields JSON;
ALTER TABLE api_definition ADD COLUMN request_fields JSON;
ALTER TABLE api_definition ADD COLUMN condition_fields JSON;