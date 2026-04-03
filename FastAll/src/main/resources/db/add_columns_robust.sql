-- 稳健的API定义表字段添加脚本
-- 使用存储过程检查字段是否存在，避免重复添加

DELIMITER $$

DROP PROCEDURE IF EXISTS AddColumnIfNotExists $$
CREATE PROCEDURE AddColumnIfNotExists(
    IN tableName VARCHAR(64),
    IN columnName VARCHAR(64),
    IN columnDefinition VARCHAR(256)
)
BEGIN
    DECLARE columnCount INT DEFAULT 0;
    
    -- 检查字段是否已存在
    SELECT COUNT(*) INTO columnCount
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = tableName
      AND COLUMN_NAME = columnName;
    
    -- 如果字段不存在，则添加
    IF columnCount = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', columnName, ' ', columnDefinition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('已添加字段: ', columnName) AS result;
    ELSE
        SELECT CONCAT('字段已存在，跳过: ', columnName) AS result;
    END IF;
END $$

DELIMITER ;

-- 执行添加字段的操作
CALL AddColumnIfNotExists('api_definition', 'operation_type', 'VARCHAR(20) COMMENT "操作类型: list/detail/add/update/delete"');
CALL AddColumnIfNotExists('api_definition', 'tenant_app_id', 'VARCHAR(100) COMMENT "租户应用ID"');
CALL AddColumnIfNotExists('api_definition', 'return_fields', 'JSON COMMENT "返回字段配置JSON"');
CALL AddColumnIfNotExists('api_definition', 'request_fields', 'JSON COMMENT "请求字段配置JSON"');
CALL AddColumnIfNotExists('api_definition', 'condition_fields', 'JSON COMMENT "查询条件字段配置JSON"');

-- 清理存储过程
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;

SELECT '所有字段添加完成' AS final_result;