package org.example.ecmo.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
public class DataCenterTableInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            checkAndCreateDataCenterTable();
            checkAndCreateDataTableGroup();
        } catch (Exception e) {
            log.error("初始化数据中心表失败", e);
        }
    }

    private void checkAndCreateDataCenterTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'data_center_table'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                log.info("data_center_table 表不存在，开始创建...");
                executeCreateScript();
            } else {
                addMissingColumns();
                log.info("data_center_table 表已存在且结构正确");
            }
        } catch (Exception e) {
            log.error("检查/创建 data_center_table 表失败", e);
        }
    }
    
    private void addMissingColumns() {
        try {
            String checkCreateTimeSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'data_center_table' AND column_name = 'create_time'";
            Integer createTimeCount = jdbcTemplate.queryForObject(checkCreateTimeSql, Integer.class);
            
            if (createTimeCount == null || createTimeCount == 0) {
                log.info("添加 create_time 列...");
                jdbcTemplate.execute("ALTER TABLE data_center_table ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");
            }
            
            String checkUpdateTimeSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'data_center_table' AND column_name = 'update_time'";
            Integer updateTimeCount = jdbcTemplate.queryForObject(checkUpdateTimeSql, Integer.class);
            
            if (updateTimeCount == null || updateTimeCount == 0) {
                log.info("添加 update_time 列...");
                jdbcTemplate.execute("ALTER TABLE data_center_table ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'");
            }
            
            String checkGroupIdSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'data_center_table' AND column_name = 'group_id'";
            Integer groupIdCount = jdbcTemplate.queryForObject(checkGroupIdSql, Integer.class);
            
            if (groupIdCount == null || groupIdCount == 0) {
                log.info("添加 group_id 列...");
                jdbcTemplate.execute("ALTER TABLE data_center_table ADD COLUMN group_id BIGINT DEFAULT NULL COMMENT '分组ID'");
            }
            
            String checkGroupNameSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'data_center_table' AND column_name = 'group_name'";
            Integer groupNameCount = jdbcTemplate.queryForObject(checkGroupNameSql, Integer.class);
            
            if (groupNameCount == null || groupNameCount == 0) {
                log.info("添加 group_name 列...");
                jdbcTemplate.execute("ALTER TABLE data_center_table ADD COLUMN group_name VARCHAR(100) DEFAULT NULL COMMENT '分组名称'");
            }
        } catch (Exception e) {
            log.error("添加缺失列失败", e);
        }
    }
    
    private void checkAndCreateDataTableGroup() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'data_table_group'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count == null || count == 0) {
                log.info("data_table_group 表不存在，开始创建...");
                jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS `data_table_group` (" +
                    "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID'," +
                    "  `group_name` varchar(100) NOT NULL COMMENT '分组名称'," +
                    "  `group_code` varchar(50) DEFAULT NULL COMMENT '分组编码'," +
                    "  `parent_id` bigint DEFAULT NULL COMMENT '父分组ID'," +
                    "  `sort_order` int DEFAULT 0 COMMENT '排序'," +
                    "  `status` tinyint DEFAULT 1 COMMENT '状态: 1-正常 0-禁用'," +
                    "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                    "  PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据表分组'"
                );
                log.info("data_table_group 表创建成功");
            } else {
                log.info("data_table_group 表已存在");
            }
        } catch (Exception e) {
            log.error("检查/创建 data_table_group 表失败", e);
        }
    }
    
    private void executeCreateScript() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/data_center_tables.sql"));
        populator.execute(dataSource);
        log.info("data_center_table 表创建成功");
    }
}
