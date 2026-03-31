-- 数据中心表记录
CREATE TABLE IF NOT EXISTS `data_center_table` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `table_name` varchar(100) NOT NULL COMMENT '表名',
  `description` varchar(500) DEFAULT NULL COMMENT '表描述',
  `group_id` bigint DEFAULT NULL COMMENT '分组ID',
  `group_name` varchar(100) DEFAULT NULL COMMENT '分组名称',
  `source_type` varchar(50) NOT NULL COMMENT '来源类型: create/import_excel/import_csv/import_json/database_import',
  `source_config` text COMMENT '来源配置(JSON格式)',
  `record_count` bigint DEFAULT 0 COMMENT '记录数',
  `status` tinyint DEFAULT 1 COMMENT '状态: 1-正常 0-删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_table_name` (`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据中心表记录';

-- 数据表分组
CREATE TABLE IF NOT EXISTS `data_table_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_name` varchar(100) NOT NULL COMMENT '分组名称',
  `group_code` varchar(50) DEFAULT NULL COMMENT '分组编码',
  `parent_id` bigint DEFAULT NULL COMMENT '父分组ID',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据表分组';
