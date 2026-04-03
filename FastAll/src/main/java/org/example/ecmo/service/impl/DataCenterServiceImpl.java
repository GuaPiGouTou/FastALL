package org.example.ecmo.service.impl;

import org.example.ecmo.service.DataCenterService;
import org.example.ecmo.factory.DatabaseConnectionFactory;
import org.example.ecmo.entity.DataCenterTable;
import org.example.ecmo.mapper.DataCenterTableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class DataCenterServiceImpl implements DataCenterService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private DatabaseConnectionFactory databaseConnectionFactory;
    
    @Autowired
    private DataCenterTableMapper dataCenterTableMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            int tableCount = getTableCount();
            long totalRecords = getTotalRecords();
            String storageUsage = getStorageUsage();
            long weeklyNewData = getWeeklyNewData();
            
            overview.put("tableCount", tableCount);
            overview.put("totalRecords", formatNumber(totalRecords));
            overview.put("storageUsage", storageUsage);
            overview.put("weeklyNewData", formatNumber(weeklyNewData));
        } catch (Exception e) {
            log.error("获取系统总览数据失败", e);
            overview.put("tableCount", 0);
            overview.put("totalRecords", "0");
            overview.put("storageUsage", "0 MB");
            overview.put("weeklyNewData", "0");
        }
        
        return overview;
    }

    @Override
    public List<Map<String, Object>> getDataRanking() {
        List<Map<String, Object>> ranking = new ArrayList<>();
        
        try {
            List<Map<String, Object>> tables = getTableList();
            
            tables.sort((a, b) -> {
                Long countA = (Long) a.get("recordCount");
                Long countB = (Long) b.get("recordCount");
                return countB.compareTo(countA);
            });
            
            int limit = Math.min(5, tables.size());
            for (int i = 0; i < limit; i++) {
                Map<String, Object> table = tables.get(i);
                Map<String, Object> item = new HashMap<>();
                item.put("tableName", table.get("tableName"));
                item.put("description", table.get("description"));
                item.put("recordCount", formatNumber((Long) table.get("recordCount")));
                ranking.add(item);
            }
        } catch (Exception e) {
            log.error("获取数据排行失败", e);
        }
        
        return ranking;
    }

    @Override
    public List<Map<String, Object>> getSystemQuota() {
        List<Map<String, Object>> quota = new ArrayList<>();
        
        try {
            int tableCount = getTableCount();
            int tableLimit = 50;
            double tablePercentage = (tableCount * 100.0) / tableLimit;
            
            Map<String, Object> tableQuota = new HashMap<>();
            tableQuota.put("name", "表数量上限");
            tableQuota.put("usage", tableCount);
            tableQuota.put("limit", tableLimit);
            tableQuota.put("percentage", Math.round(tablePercentage * 10) / 10.0);
            quota.add(tableQuota);
            
            Map<String, Object> storageQuota = new HashMap<>();
            storageQuota.put("name", "存储空间");
            storageQuota.put("usage", getStorageUsage());
            storageQuota.put("limit", "500 MB");
            storageQuota.put("percentage", getStoragePercentage());
            quota.add(storageQuota);
            
            Map<String, Object> connectionQuota = new HashMap<>();
            connectionQuota.put("name", "并发连接数");
            connectionQuota.put("usage", getActiveConnections());
            connectionQuota.put("limit", 50);
            connectionQuota.put("percentage", (getActiveConnections() * 100.0) / 50);
            quota.add(connectionQuota);
            
            Map<String, Object> queryQuota = new HashMap<>();
            queryQuota.put("name", "查询速率");
            queryQuota.put("usage", "120 QPS");
            queryQuota.put("limit", "500 QPS");
            queryQuota.put("percentage", 24.0);
            quota.add(queryQuota);
            
        } catch (Exception e) {
            log.error("获取系统配额失败", e);
        }
        
        return quota;
    }

    @Override
    public boolean saveDbConfig(Map<String, Object> config) {
        try {
            log.info("保存数据库配置: {}", config);
            return true;
        } catch (Exception e) {
            log.error("保存数据库配置失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> testConnection(Map<String, Object> config) {
        String dbType = (String) config.get("type");
        String host = (String) config.get("host");
        Integer port = (Integer) config.get("port");
        String database = (String) config.get("database");
        String username = (String) config.get("username");
        String password = (String) config.get("password");
        
        return databaseConnectionFactory.testConnection(dbType, host, port, database, username, password);
    }

    @Override
    public Map<String, Object> getConnectionStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            status.put("status", "connected");
            status.put("dbType", connection.getMetaData().getDatabaseProductName());
            status.put("responseTime", "12ms");
            status.put("lastConnected", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (Exception e) {
            log.error("获取连接状态失败", e);
            status.put("status", "disconnected");
            status.put("dbType", "未知");
            status.put("responseTime", "-");
            status.put("lastConnected", "-");
        }
        
        return status;
    }

    @Override
    public List<Map<String, Object>> getTableList() {
        List<Map<String, Object>> tables = new ArrayList<>();
        
        try {
            QueryWrapper<DataCenterTable> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1).orderByDesc("create_time");
            List<DataCenterTable> tableRecords = dataCenterTableMapper.selectList(queryWrapper);
            
            for (DataCenterTable record : tableRecords) {
                Map<String, Object> table = new HashMap<>();
                table.put("tableName", record.getTableName());
                table.put("description", record.getDescription() != null ? record.getDescription() : record.getTableName());
                table.put("groupId", record.getGroupId());
                table.put("groupName", record.getGroupName());
                table.put("sourceType", record.getSourceType());
                table.put("createTime", record.getCreateTime());
                
                try {
                    Long recordCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM " + record.getTableName(), Long.class);
                    table.put("recordCount", recordCount != null ? recordCount : 0L);
                    
                    if (!recordCount.equals(record.getRecordCount())) {
                        record.setRecordCount(recordCount);
                        dataCenterTableMapper.updateById(record);
                    }
                } catch (Exception e) {
                    table.put("recordCount", 0L);
                }
                
                tables.add(table);
            }
        } catch (Exception e) {
            log.error("获取数据表列表失败", e);
        }
        
        return tables;
    }

    @Override
    public boolean createTable(Map<String, Object> tableConfig) {
        try {
            String tableName = (String) tableConfig.get("tableName");
            if (tableName == null || tableName.trim().isEmpty()) {
                throw new RuntimeException("表名不能为空");
            }
            
            tableName = tableName.trim();
            
            if (!tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                throw new RuntimeException("表名格式不正确，只能包含字母、数字和下划线，且必须以字母或下划线开头");
            }
            
            String description = (String) tableConfig.get("description");
            
            String createSql = buildCreateTableSql(tableConfig);
            log.info("执行建表SQL: {}", createSql);
            jdbcTemplate.execute(createSql);
            
            DataCenterTable record = new DataCenterTable();
            record.setTableName(tableName);
            record.setDescription(description);
            record.setGroupId(tableConfig.get("groupId") != null ? Long.valueOf(tableConfig.get("groupId").toString()) : null);
            record.setGroupName((String) tableConfig.get("groupName"));
            record.setSourceType("create");
            record.setRecordCount(0L);
            record.setStatus(1);
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            dataCenterTableMapper.insert(record);
            
            log.info("创建表成功: {}", tableName);
            return true;
        } catch (Exception e) {
            log.error("创建表失败", e);
            throw new RuntimeException("创建表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getTableInfo(String tableName) {
        Map<String, Object> tableInfo = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String schema = connection.getCatalog();
            
            try (ResultSet rs = metaData.getTables(schema, schema, tableName, new String[]{"TABLE"})) {
                if (rs.next()) {
                    tableInfo.put("tableName", rs.getString("TABLE_NAME"));
                    tableInfo.put("description", rs.getString("REMARKS"));
                }
            }
            
            List<Map<String, Object>> columns = new ArrayList<>();
            try (ResultSet rs = metaData.getColumns(schema, schema, tableName, "%")) {
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();
                    column.put("columnName", rs.getString("COLUMN_NAME"));
                    column.put("dataType", rs.getString("TYPE_NAME"));
                    column.put("columnSize", rs.getInt("COLUMN_SIZE"));
                    column.put("nullable", rs.getInt("NULLABLE") == 1);
                    column.put("remarks", rs.getString("REMARKS"));
                    columns.add(column);
                }
            }
            tableInfo.put("columns", columns);
            
        } catch (Exception e) {
            log.error("获取表信息失败: {}", tableName, e);
        }
        
        return tableInfo;
    }

    @Override
    public boolean updateTable(String tableName, Map<String, Object> tableConfig) {
        try {
            log.info("更新表: {}", tableName);
            return true;
        } catch (Exception e) {
            log.error("更新表失败: {}", tableName, e);
            return false;
        }
    }

    @Override
    public boolean deleteTable(String tableName) {
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);
            
            QueryWrapper<DataCenterTable> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("table_name", tableName);
            dataCenterTableMapper.delete(queryWrapper);
            
            log.info("删除表成功: {}", tableName);
            return true;
        } catch (Exception e) {
            log.error("删除表失败: {}", tableName, e);
            return false;
        }
    }
    
    private int getTableCount() {
        try {
            QueryWrapper<DataCenterTable> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1);
            return Math.toIntExact(dataCenterTableMapper.selectCount(queryWrapper));
        } catch (Exception e) {
            log.error("获取表数量失败", e);
            return 0;
        }
    }
    
    private long getTotalRecords() {
        long total = 0;
        try {
            List<Map<String, Object>> tables = getTableList();
            for (Map<String, Object> table : tables) {
                Long count = (Long) table.get("recordCount");
                if (count != null) {
                    total += count;
                }
            }
        } catch (Exception e) {
            log.error("获取总记录数失败", e);
        }
        return total;
    }
    
    private String getStorageUsage() {
        try {
            String query = "SELECT SUM(data_length + index_length) / 1024 / 1024 AS size_mb " +
                          "FROM information_schema.tables WHERE table_schema = DATABASE()";
            Double sizeMb = jdbcTemplate.queryForObject(query, Double.class);
            return sizeMb != null ? String.format("%.1f MB", sizeMb) : "0 MB";
        } catch (Exception e) {
            log.error("获取存储占用失败", e);
            return "0 MB";
        }
    }
    
    private double getStoragePercentage() {
        try {
            String query = "SELECT SUM(data_length + index_length) / 1024 / 1024 AS size_mb " +
                          "FROM information_schema.tables WHERE table_schema = DATABASE()";
            Double sizeMb = jdbcTemplate.queryForObject(query, Double.class);
            return sizeMb != null ? (sizeMb * 100.0 / 500) : 0.0;
        } catch (Exception e) {
            log.error("获取存储占用百分比失败", e);
            return 0.0;
        }
    }
    
    private long getWeeklyNewData() {
        try {
            String query = "SELECT COUNT(*) FROM sys_oper_log WHERE DATE_FORMAT(create_time, '%Y-%m-%d') >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            Long count = jdbcTemplate.queryForObject(query, Long.class);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("获取本周新增数据量失败", e);
            return 0L;
        }
    }
    
    private int getActiveConnections() {
        try {
            String query = "SHOW STATUS LIKE 'Threads_connected'";
            Map<String, Object> result = jdbcTemplate.queryForMap(query);
            String value = (String) result.get("Value");
            return Integer.parseInt(value);
        } catch (Exception e) {
            log.error("获取活动连接数失败", e);
            return 0;
        }
    }
    
    private String formatNumber(long number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.valueOf(number);
        }
    }
    
    private String buildCreateTableSql(Map<String, Object> tableConfig) {
        StringBuilder sql = new StringBuilder();
        String tableName = (String) tableConfig.get("tableName");
        sql.append("CREATE TABLE ").append(tableName).append(" (");
        
        List<Map<String, Object>> columns = (List<Map<String, Object>>) tableConfig.get("columns");
        List<String> primaryKeys = new ArrayList<>();
        
        boolean hasUserIdColumn = false;
        boolean hasUserPrimaryKey = false;
        if (columns != null && !columns.isEmpty()) {
            for (Map<String, Object> column : columns) {
                String colName = (String) column.get("name");
                if ("id".equalsIgnoreCase(colName)) {
                    hasUserIdColumn = true;
                }
                if (Boolean.TRUE.equals(column.get("primaryKey"))) {
                    hasUserPrimaryKey = true;
                    primaryKeys.add(colName);
                }
            }
        }
        
        if (!hasUserIdColumn) {
            sql.append("id BIGINT PRIMARY KEY AUTO_INCREMENT");
        }
        
        if (columns != null && !columns.isEmpty()) {
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("name");
                String columnType = (String) column.get("type");
                
                if (columnName == null || columnName.trim().isEmpty()) {
                    continue;
                }
                
                if (columnType == null || columnType.trim().isEmpty()) {
                    columnType = "VARCHAR(255)";
                }
                
                if (!hasUserIdColumn || !"id".equalsIgnoreCase(columnName)) {
                    sql.append(", ");
                }
                sql.append(columnName).append(" ").append(columnType);
                
                if (Boolean.TRUE.equals(column.get("notNull"))) {
                    sql.append(" NOT NULL");
                }
                
                String extra = (String) column.get("extra");
                String comment = (String) column.get("comment");
                if (extra != null && !extra.isEmpty()) {
                    if (comment != null && !comment.isEmpty()) {
                        comment = comment + " [" + extra + "]";
                    } else {
                        comment = "[" + extra + "]";
                    }
                }
                
                if (comment != null && !comment.isEmpty()) {
                    sql.append(" COMMENT '").append(comment.replace("'", "\\'")).append("'");
                }
                
                if (column.get("defaultValue") != null && !column.get("defaultValue").toString().isEmpty()) {
                    String defaultValue = column.get("defaultValue").toString();
                    String dataTypeUpper = columnType.toUpperCase();
                    if (!dataTypeUpper.contains("TEXT") && !dataTypeUpper.contains("BLOB")) {
                        sql.append(" DEFAULT '").append(defaultValue).append("'");
                    }
                }
            }
            
            if (hasUserPrimaryKey && !primaryKeys.isEmpty()) {
                sql.append(", PRIMARY KEY (");
                for (int i = 0; i < primaryKeys.size(); i++) {
                    if (i > 0) sql.append(", ");
                    sql.append(primaryKeys.get(i));
                }
                sql.append(")");
            }
        }
        
        sql.append(", created_at DATETIME DEFAULT CURRENT_TIMESTAMP");
        sql.append(", updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        sql.append(", created_by VARCHAR(100)");
        
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        
        String description = (String) tableConfig.get("description");
        if (description != null && !description.trim().isEmpty()) {
            sql.append(" COMMENT='").append(description.replace("'", "\\'")).append("'");
        }
        
        log.info("创建表SQL: {}", sql.toString());
        return sql.toString();
    }

    @Override
    public Map<String, Object> getTableData(String tableName, int page, int pageSize) {
        return getTableDataWithSort(tableName, page, pageSize, null, null, null, null);
    }

    @Override
    public Map<String, Object> getTableData(String tableName, int page, int pageSize, String search, Map<String, Object> filters) {
        return getTableDataWithSort(tableName, page, pageSize, search, filters, null, null);
    }

    @Override
    public Map<String, Object> getTableDataWithSort(String tableName, int page, int pageSize, String search, Map<String, Object> filters, String sortField, String sortOrder) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
            List<Object> params = new ArrayList<>();
            
            if (search != null && !search.trim().isEmpty()) {
                List<String> columns = getTableColumnNames(tableName);
                if (!columns.isEmpty()) {
                    whereClause.append(" AND (");
                    for (int i = 0; i < columns.size(); i++) {
                        if (i > 0) whereClause.append(" OR ");
                        whereClause.append(columns.get(i)).append(" LIKE ?");
                        params.add("%" + search + "%");
                    }
                    whereClause.append(")");
                }
            }
            
            if (filters != null && !filters.isEmpty()) {
                for (Map.Entry<String, Object> entry : filters.entrySet()) {
                    if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
                        whereClause.append(" AND ").append(entry.getKey()).append(" = ?");
                        params.add(entry.getValue());
                    }
                }
            }
            
            String countSql = "SELECT COUNT(*) FROM " + tableName + whereClause;
            Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
            
            StringBuilder dataSql = new StringBuilder("SELECT * FROM ");
            dataSql.append(tableName).append(whereClause);
            
            if (sortField != null && !sortField.trim().isEmpty()) {
                dataSql.append(" ORDER BY ").append(sortField);
                if ("desc".equalsIgnoreCase(sortOrder)) {
                    dataSql.append(" DESC");
                }
            }
            
            dataSql.append(" LIMIT ? OFFSET ?");
            params.add(pageSize);
            params.add((page - 1) * pageSize);
            
            List<Map<String, Object>> data = jdbcTemplate.queryForList(dataSql.toString(), params.toArray());
            
            List<Map<String, Object>> columnInfos = getTableColumnInfos(tableName);
            List<String> columns = new ArrayList<>();
            for (Map<String, Object> col : columnInfos) {
                columns.add((String) col.get("columnName"));
            }
            
            result.put("data", data);
            result.put("columns", columnInfos);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) total / pageSize));
            
            log.info("获取表数据成功: {} - 第{}页, 共{}条记录", tableName, page, total);
        } catch (Exception e) {
            log.error("获取表数据失败: {}", tableName, e);
            result.put("data", new ArrayList<>());
            result.put("columns", new ArrayList<>());
            result.put("total", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", 0);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    private List<String> getTableColumnNames(String tableName) throws Exception {
        List<String> columns = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String schema = connection.getCatalog();
            try (ResultSet rs = metaData.getColumns(schema, schema, tableName, "%")) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"));
                }
            }
        }
        return columns;
    }
    
    private List<Map<String, Object>> getTableColumnInfos(String tableName) throws Exception {
        List<Map<String, Object>> columns = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            String schema = connection.getCatalog();
            String sql = "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_TYPE, COLUMN_KEY, IS_NULLABLE, COLUMN_DEFAULT, EXTRA, COLUMN_COMMENT " +
                        "FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                        "ORDER BY ORDINAL_POSITION";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, schema);
                ps.setString(2, tableName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> column = new HashMap<>();
                        column.put("columnName", rs.getString("COLUMN_NAME"));
                        column.put("dataType", rs.getString("COLUMN_TYPE"));
                        column.put("columnKey", rs.getString("COLUMN_KEY"));
                        column.put("nullable", "YES".equals(rs.getString("IS_NULLABLE")));
                        column.put("columnDefault", rs.getString("COLUMN_DEFAULT"));
                        column.put("extra", rs.getString("EXTRA"));
                        column.put("remarks", rs.getString("COLUMN_COMMENT"));
                        columns.add(column);
                    }
                }
            }
        }
        return columns;
    }

    @Override
    public boolean insertData(String tableName, Map<String, Object> data, String currentUser) {
        try {
            if (data == null || data.isEmpty()) {
                throw new RuntimeException("插入数据不能为空");
            }
            
            List<Map<String, Object>> columnInfos = getTableColumnInfos(tableName);
            Set<String> primaryKeys = getPrimaryKeys(tableName);
            Map<String, Boolean> autoIncrementMap = getAutoIncrementColumns(tableName);
            Set<String> columnNames = getColumnNames(tableName);
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowStr = sdf.format(new java.util.Date());
            
            if (columnNames.contains("created_at") && !data.containsKey("created_at")) {
                data.put("created_at", nowStr);
            }
            if (columnNames.contains("create_time") && !data.containsKey("create_time")) {
                data.put("create_time", nowStr);
            }
            if (columnNames.contains("updated_at") && !data.containsKey("updated_at")) {
                data.put("updated_at", nowStr);
            }
            if (columnNames.contains("update_time") && !data.containsKey("update_time")) {
                data.put("update_time", nowStr);
            }
            if (columnNames.contains("created_by") && !data.containsKey("created_by") && currentUser != null) {
                data.put("created_by", currentUser);
            }
            if (columnNames.contains("create_user") && !data.containsKey("create_user") && currentUser != null) {
                data.put("create_user", currentUser);
            }
            if (columnNames.contains("updated_by") && !data.containsKey("updated_by") && currentUser != null) {
                data.put("updated_by", currentUser);
            }
            if (columnNames.contains("update_user") && !data.containsKey("update_user") && currentUser != null) {
                data.put("update_user", currentUser);
            }
            
            StringBuilder sql = new StringBuilder("INSERT INTO ");
            sql.append(tableName).append(" (");
            
            StringBuilder placeholders = new StringBuilder();
            List<Object> values = new ArrayList<>();
            boolean first = true;
            
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String columnName = entry.getKey();
                
                if (primaryKeys.contains(columnName.toLowerCase()) && 
                    Boolean.TRUE.equals(autoIncrementMap.get(columnName.toLowerCase()))) {
                    continue;
                }
                
                Object value = validateAndConvertValue(entry.getValue(), columnName, columnInfos, true);
                if (value != null) {
                    if (!first) {
                        sql.append(", ");
                        placeholders.append(", ");
                    }
                    sql.append(columnName);
                    placeholders.append("?");
                    values.add(value);
                    first = false;
                }
            }
            
            if (values.isEmpty()) {
                throw new RuntimeException("请至少填写一个有效字段");
            }
            
            sql.append(") VALUES (").append(placeholders).append(")");
            
            jdbcTemplate.update(sql.toString(), values.toArray());
            log.info("插入数据成功: {}", tableName);
            return true;
        } catch (Exception e) {
            log.error("插入数据失败: {}", tableName, e);
            throw new RuntimeException("插入数据失败: " + e.getMessage());
        }
    }
    
    private Set<String> getColumnNames(String tableName) {
        Set<String> columnNames = new HashSet<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String schema = connection.getCatalog();
            try (ResultSet rs = metaData.getColumns(schema, schema, tableName, "%")) {
                while (rs.next()) {
                    columnNames.add(rs.getString("COLUMN_NAME").toLowerCase());
                }
            }
        } catch (Exception e) {
            log.error("获取列名失败: {}", tableName, e);
        }
        return columnNames;
    }
    
    private Map<String, Boolean> getAutoIncrementColumns(String tableName) {
        Map<String, Boolean> autoIncrementMap = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String schema = connection.getCatalog();
            try (ResultSet rs = metaData.getColumns(schema, schema, tableName, "%")) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME").toLowerCase();
                    String isAutoIncrement = rs.getString("IS_AUTOINCREMENT");
                    autoIncrementMap.put(columnName, "YES".equalsIgnoreCase(isAutoIncrement));
                }
            }
        } catch (Exception e) {
            log.error("获取自增列失败: {}", tableName, e);
        }
        return autoIncrementMap;
    }
    
    private Set<String> getPrimaryKeys(String tableName) {
        Set<String> primaryKeys = new HashSet<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String schema = connection.getCatalog();
            try (ResultSet rs = metaData.getPrimaryKeys(schema, schema, tableName)) {
                while (rs.next()) {
                    primaryKeys.add(rs.getString("COLUMN_NAME").toLowerCase());
                }
            }
        } catch (Exception e) {
            log.error("获取主键失败: {}", tableName, e);
        }
        return primaryKeys;
    }
    
    private Object validateAndConvertValue(Object value, String columnName, List<Map<String, Object>> columnInfos, boolean isInsert) {
        if (value == null) {
            return null;
        }
        
        String strValue = value.toString();
        
        if (strValue.trim().isEmpty()) {
            return isInsert ? "" : null;
        }
        
        String dataType = null;
        boolean nullable = true;
        for (Map<String, Object> col : columnInfos) {
            if (columnName.equalsIgnoreCase((String) col.get("columnName"))) {
                dataType = (String) col.get("dataType");
                nullable = Boolean.TRUE.equals(col.get("nullable"));
                break;
            }
        }
        
        if (dataType == null) {
            return value;
        }
        
        String dataTypeUpper = dataType.toUpperCase();
        
        try {
            if (dataTypeUpper.contains("INT") || dataTypeUpper.contains("BIGINT") || 
                dataTypeUpper.contains("SMALLINT") || dataTypeUpper.contains("TINYINT")) {
                if (value instanceof String) {
                    return Long.parseLong(value.toString().trim());
                }
                return value;
            } else if (dataTypeUpper.contains("DECIMAL") || dataTypeUpper.contains("NUMERIC")) {
                return new java.math.BigDecimal(value.toString().trim());
            } else if (dataTypeUpper.contains("FLOAT") || dataTypeUpper.contains("DOUBLE")) {
                return Double.parseDouble(value.toString().trim());
            } else if (dataTypeUpper.contains("DATE") || dataTypeUpper.contains("TIME")) {
                return value;
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("字段 '" + columnName + "' 的值 '" + value + "' 格式不正确，需要 " + dataType + " 类型");
        }
        
        return value;
    }

    @Override
    public boolean updateData(String tableName, Object id, Map<String, Object> data, String currentUser) {
        try {
            if (data == null || data.isEmpty()) {
                throw new RuntimeException("更新数据不能为空");
            }
            
            List<Map<String, Object>> columnInfos = getTableColumnInfos(tableName);
            Set<String> primaryKeys = getPrimaryKeys(tableName);
            Set<String> columnNames = getColumnNames(tableName);
            String primaryKeyColumn = primaryKeys.isEmpty() ? "id" : primaryKeys.iterator().next();
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowStr = sdf.format(new java.util.Date());
            
            if (columnNames.contains("updated_at") && !data.containsKey("updated_at")) {
                data.put("updated_at", nowStr);
            }
            if (columnNames.contains("update_time") && !data.containsKey("update_time")) {
                data.put("update_time", nowStr);
            }
            if (columnNames.contains("updated_by") && !data.containsKey("updated_by") && currentUser != null) {
                data.put("updated_by", currentUser);
            }
            if (columnNames.contains("update_user") && !data.containsKey("update_user") && currentUser != null) {
                data.put("update_user", currentUser);
            }
            
            StringBuilder sql = new StringBuilder("UPDATE ");
            sql.append(tableName).append(" SET ");
            
            List<Object> values = new ArrayList<>();
            boolean first = true;
            
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String columnName = entry.getKey();
                if (primaryKeys.contains(columnName.toLowerCase())) {
                    continue;
                }
                
                Object value = validateAndConvertValue(entry.getValue(), columnName, columnInfos, false);
                if (value != null) {
                    if (!first) {
                        sql.append(", ");
                    }
                    sql.append(columnName).append(" = ?");
                    values.add(value);
                    first = false;
                }
            }
            
            if (values.isEmpty()) {
                throw new RuntimeException("没有需要更新的字段（主键字段不可更新）");
            }
            
            sql.append(" WHERE ").append(primaryKeyColumn).append(" = ?");
            values.add(id);
            
            int rows = jdbcTemplate.update(sql.toString(), values.toArray());
            if (rows == 0) {
                throw new RuntimeException("未找到要更新的记录");
            }
            
            log.info("更新数据成功: {} - {}: {}", tableName, primaryKeyColumn, id);
            return true;
        } catch (Exception e) {
            log.error("更新数据失败: {}", tableName, e);
            throw new RuntimeException("更新数据失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteData(String tableName, Object id) {
        try {
            Set<String> primaryKeys = getPrimaryKeys(tableName);
            String primaryKeyColumn = primaryKeys.isEmpty() ? "id" : primaryKeys.iterator().next();
            String sql = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumn + " = ?";
            int rows = jdbcTemplate.update(sql, id);
            if (rows == 0) {
                throw new RuntimeException("未找到要删除的记录");
            }
            log.info("删除数据成功: {} - {}: {}", tableName, primaryKeyColumn, id);
            return true;
        } catch (Exception e) {
            log.error("删除数据失败: {}", tableName, e);
            throw new RuntimeException("删除数据失败: " + e.getMessage());
        }
    }

    @Override
    public boolean batchDeleteData(String tableName, List<Object> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                throw new RuntimeException("请选择要删除的记录");
            }
            
            Set<String> primaryKeys = getPrimaryKeys(tableName);
            String primaryKeyColumn = primaryKeys.isEmpty() ? "id" : primaryKeys.iterator().next();
            
            StringBuilder sql = new StringBuilder("DELETE FROM ");
            sql.append(tableName).append(" WHERE ").append(primaryKeyColumn).append(" IN (");
            
            for (int i = 0; i < ids.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
            }
            sql.append(")");
            
            int rows = jdbcTemplate.update(sql.toString(), ids.toArray());
            log.info("批量删除数据成功: {} - 删除{}条记录", tableName, rows);
            return true;
        } catch (Exception e) {
            log.error("批量删除数据失败: {}", tableName, e);
            throw new RuntimeException("批量删除数据失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportTableData(String tableName, String format, String search, Map<String, Object> filters) {
        try {
            Map<String, Object> result = getTableData(tableName, 1, Integer.MAX_VALUE, search, filters);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
            
            if ("csv".equalsIgnoreCase(format)) {
                return exportAsCsv(data);
            } else if ("json".equalsIgnoreCase(format)) {
                return exportAsJson(data);
            } else {
                return exportAsCsv(data);
            }
        } catch (Exception e) {
            log.error("导出数据失败: {}", tableName, e);
            throw new RuntimeException("导出数据失败: " + e.getMessage());
        }
    }
    
    private byte[] exportAsCsv(List<Map<String, Object>> data) {
        StringBuilder csv = new StringBuilder();
        
        if (!data.isEmpty()) {
            Map<String, Object> firstRow = data.get(0);
            List<String> headers = new ArrayList<>(firstRow.keySet());
            
            csv.append(String.join(",", headers)).append("\n");
            
            for (Map<String, Object> row : data) {
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    Object value = row.get(header);
                    String strValue = value == null ? "" : value.toString();
                    if (strValue.contains(",") || strValue.contains("\"") || strValue.contains("\n")) {
                        strValue = "\"" + strValue.replace("\"", "\"\"") + "\"";
                    }
                    values.add(strValue);
                }
                csv.append(String.join(",", values)).append("\n");
            }
        }
        
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    
    private byte[] exportAsJson(List<Map<String, Object>> data) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
        } catch (Exception e) {
            throw new RuntimeException("JSON序列化失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getTableColumns(String tableName) {
        try {
            return getTableColumnInfos(tableName);
        } catch (Exception e) {
            log.error("获取表列信息失败: {}", tableName, e);
            throw new RuntimeException("获取表列信息失败: " + e.getMessage());
        }
    }

    @Override
    public boolean addColumn(String tableName, Map<String, Object> columnConfig) {
        try {
            String columnName = (String) columnConfig.get("columnName");
            String dataType = (String) columnConfig.get("dataType");
            
            if (columnName == null || columnName.trim().isEmpty()) {
                throw new RuntimeException("列名不能为空");
            }
            if (dataType == null || dataType.trim().isEmpty()) {
                throw new RuntimeException("数据类型不能为空");
            }
            
            String extra = (String) columnConfig.get("extra");
            String comment = (String) columnConfig.get("comment");
            
            if (extra != null && !extra.isEmpty()) {
                if (comment != null && !comment.isEmpty()) {
                    comment = comment + " [" + extra + "]";
                } else {
                    comment = "[" + extra + "]";
                }
            }
            
            StringBuilder sql = new StringBuilder("ALTER TABLE ");
            sql.append(tableName).append(" ADD COLUMN ").append(columnName).append(" ").append(dataType);
            
            String dataTypeUpper = dataType.toUpperCase();
            boolean isTextType = dataTypeUpper.contains("TEXT") || dataTypeUpper.contains("BLOB") || 
                                     dataTypeUpper.contains("GEOMETRY") || dataTypeUpper.contains("JSON");
            
            if (Boolean.TRUE.equals(columnConfig.get("notNull"))) {
                sql.append(" NOT NULL");
            }
            
            if (columnConfig.get("defaultValue") != null && !columnConfig.get("defaultValue").toString().isEmpty() && !isTextType) {
                sql.append(" DEFAULT '").append(columnConfig.get("defaultValue")).append("'");
            }
            
            if (comment != null && !comment.isEmpty()) {
                sql.append(" COMMENT '").append(comment.replace("'", "\\'")).append("'");
            }
            
            jdbcTemplate.execute(sql.toString());
            log.info("添加列成功: {} - {}", tableName, columnName);
            return true;
        } catch (Exception e) {
            log.error("添加列失败: {}", tableName, e);
            throw new RuntimeException("添加列失败: " + e.getMessage());
        }
    }

    @Override
    public boolean modifyColumn(String tableName, String columnName, Map<String, Object> columnConfig) {
        try {
            String newDataType = (String) columnConfig.get("dataType");
            
            if (newDataType == null || newDataType.trim().isEmpty()) {
                throw new RuntimeException("数据类型不能为空");
            }
            
            String extra = (String) columnConfig.get("extra");
            String comment = (String) columnConfig.get("comment");
            
            if (extra != null && !extra.isEmpty()) {
                if (comment != null && !comment.isEmpty()) {
                    comment = comment + " [" + extra + "]";
                } else {
                    comment = "[" + extra + "]";
                }
            }
            
            StringBuilder sql = new StringBuilder("ALTER TABLE ");
            sql.append(tableName).append(" MODIFY COLUMN ").append(columnName).append(" ").append(newDataType);
            
            if (Boolean.TRUE.equals(columnConfig.get("notNull"))) {
                sql.append(" NOT NULL");
            } else {
                sql.append(" NULL");
            }
            
            if (columnConfig.get("defaultValue") != null && !columnConfig.get("defaultValue").toString().isEmpty()) {
                sql.append(" DEFAULT '").append(columnConfig.get("defaultValue")).append("'");
            }
            
            if (comment != null && !comment.isEmpty()) {
                sql.append(" COMMENT '").append(comment.replace("'", "\\'")).append("'");
            }
            
            jdbcTemplate.execute(sql.toString());
            log.info("修改列成功: {} - {}", tableName, columnName);
            return true;
        } catch (Exception e) {
            log.error("修改列失败: {}", tableName, e);
            throw new RuntimeException("修改列失败: " + e.getMessage());
        }
    }

    @Override
    public boolean dropColumn(String tableName, String columnName) {
        try {
            String sql = "ALTER TABLE " + tableName + " DROP COLUMN " + columnName;
            jdbcTemplate.execute(sql);
            log.info("删除列成功: {} - {}", tableName, columnName);
            return true;
        } catch (Exception e) {
            log.error("删除列失败: {}", tableName, e);
            throw new RuntimeException("删除列失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> generateCrudApi(String tableName, Map<String, Object> options) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> generatedApis = new ArrayList<>();
        
        try {
            List<Map<String, Object>> columns = getTableColumnInfos(tableName);
            
            if (Boolean.TRUE.equals(options.get("generateList"))) {
                Map<String, Object> listApi = new HashMap<>();
                listApi.put("apiName", "获取" + tableName + "列表");
                listApi.put("apiPath", "/api/v1/" + tableName + "/list");
                listApi.put("apiMethod", "GET");
                listApi.put("description", "获取" + tableName + "表的分页列表数据");
                listApi.put("tableName", tableName);
                listApi.put("execMode", "crud");
                listApi.put("operationType", "list");
                listApi.put("authType", "TOKEN");
                listApi.put("returnFields", new ArrayList<>());
                listApi.put("requestFields", new HashMap<>());
                listApi.put("conditionFields", new HashMap<>());
                listApi.put("columns", columns);
                generatedApis.add(listApi);
            }
            
            if (Boolean.TRUE.equals(options.get("generateDetail"))) {
                Map<String, Object> detailApi = new HashMap<>();
                detailApi.put("apiName", "获取" + tableName + "详情");
                detailApi.put("apiPath", "/api/v1/" + tableName + "/detail");
                detailApi.put("apiMethod", "GET");
                detailApi.put("description", "根据ID获取" + tableName + "表的单条记录详情");
                detailApi.put("tableName", tableName);
                detailApi.put("execMode", "crud");
                detailApi.put("operationType", "detail");
                detailApi.put("authType", "TOKEN");
                detailApi.put("returnFields", new ArrayList<>());
                detailApi.put("requestFields", new HashMap<>());
                detailApi.put("conditionFields", new HashMap<>());
                detailApi.put("columns", columns);
                generatedApis.add(detailApi);
            }
            
            if (Boolean.TRUE.equals(options.get("generateCreate"))) {
                Map<String, Object> createApi = new HashMap<>();
                createApi.put("apiName", "创建" + tableName);
                createApi.put("apiPath", "/api/v1/" + tableName + "/add");
                createApi.put("apiMethod", "POST");
                createApi.put("description", "在" + tableName + "表中创建新记录");
                createApi.put("tableName", tableName);
                createApi.put("execMode", "crud");
                createApi.put("operationType", "add");
                createApi.put("authType", "TOKEN");
                createApi.put("returnFields", new ArrayList<>());
                createApi.put("requestFields", new HashMap<>());
                createApi.put("conditionFields", new HashMap<>());
                createApi.put("columns", columns);
                generatedApis.add(createApi);
            }
            
            if (Boolean.TRUE.equals(options.get("generateUpdate"))) {
                Map<String, Object> updateApi = new HashMap<>();
                updateApi.put("apiName", "更新" + tableName);
                updateApi.put("apiPath", "/api/v1/" + tableName + "/update");
                updateApi.put("apiMethod", "POST");
                updateApi.put("description", "根据ID更新" + tableName + "表的记录");
                updateApi.put("tableName", tableName);
                updateApi.put("execMode", "crud");
                updateApi.put("operationType", "update");
                updateApi.put("authType", "TOKEN");
                updateApi.put("returnFields", new ArrayList<>());
                updateApi.put("requestFields", new HashMap<>());
                updateApi.put("conditionFields", new HashMap<>());
                updateApi.put("columns", columns);
                generatedApis.add(updateApi);
            }
            
            if (Boolean.TRUE.equals(options.get("generateDelete"))) {
                Map<String, Object> deleteApi = new HashMap<>();
                deleteApi.put("apiName", "删除" + tableName);
                deleteApi.put("apiPath", "/api/v1/" + tableName + "/delete");
                deleteApi.put("apiMethod", "POST");
                deleteApi.put("description", "根据ID删除" + tableName + "表的记录");
                deleteApi.put("tableName", tableName);
                deleteApi.put("execMode", "crud");
                deleteApi.put("operationType", "delete");
                deleteApi.put("authType", "TOKEN");
                deleteApi.put("returnFields", new ArrayList<>());
                deleteApi.put("requestFields", new HashMap<>());
                deleteApi.put("conditionFields", new HashMap<>());
                generatedApis.add(deleteApi);
            }
            
            result.put("success", true);
            result.put("generatedApis", generatedApis);
            result.put("message", "成功生成 " + generatedApis.size() + " 个API");
            
            log.info("为表 {} 生成了 {} 个CRUD API", tableName, generatedApis.size());
        } catch (Exception e) {
            log.error("生成CRUD API失败: {}", tableName, e);
            result.put("success", false);
            result.put("message", "生成失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> dynamicQuery(Map<String, Object> queryConfig) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String tableName = (String) queryConfig.get("table");
            if (tableName == null || tableName.trim().isEmpty()) {
                throw new RuntimeException("表名不能为空");
            }
            
            List<String> fields = (List<String>) queryConfig.get("fields");
            Map<String, Object> where = (Map<String, Object>) queryConfig.get("where");
            Integer limit = (Integer) queryConfig.get("limit");
            Integer offset = (Integer) queryConfig.get("offset");
            String orderBy = (String) queryConfig.get("orderBy");
            String orderDir = (String) queryConfig.get("orderDir");
            
            StringBuilder sql = new StringBuilder("SELECT ");
            
            if (fields == null || fields.isEmpty()) {
                sql.append("*");
            } else {
                sql.append(String.join(", ", fields));
            }
            
            sql.append(" FROM ").append(tableName);
            
            List<Object> params = new ArrayList<>();
            if (where != null && !where.isEmpty()) {
                sql.append(" WHERE ");
                boolean first = true;
                
                for (Map.Entry<String, Object> entry : where.entrySet()) {
                    if (!first) sql.append(" AND ");
                    
                    String fieldName = entry.getKey();
                    Object fieldValue = entry.getValue();
                    
                    if (fieldValue instanceof Map) {
                        Map<String, Object> opValue = (Map<String, Object>) fieldValue;
                        for (Map.Entry<String, Object> opEntry : opValue.entrySet()) {
                            String op = opEntry.getKey();
                            Object val = opEntry.getValue();
                            
                            switch (op) {
                                case "$eq":
                                    sql.append(fieldName).append(" = ?");
                                    params.add(val);
                                    break;
                                case "$ne":
                                    sql.append(fieldName).append(" != ?");
                                    params.add(val);
                                    break;
                                case "$gt":
                                    sql.append(fieldName).append(" > ?");
                                    params.add(val);
                                    break;
                                case "$gte":
                                    sql.append(fieldName).append(" >= ?");
                                    params.add(val);
                                    break;
                                case "$lt":
                                    sql.append(fieldName).append(" < ?");
                                    params.add(val);
                                    break;
                                case "$lte":
                                    sql.append(fieldName).append(" <= ?");
                                    params.add(val);
                                    break;
                                case "$like":
                                    sql.append(fieldName).append(" LIKE ?");
                                    params.add("%" + val + "%");
                                    break;
                                case "$in":
                                    sql.append(fieldName).append(" IN (");
                                    List<?> inValues = (List<?>) val;
                                    for (int i = 0; i < inValues.size(); i++) {
                                        if (i > 0) sql.append(", ");
                                        sql.append("?");
                                        params.add(inValues.get(i));
                                    }
                                    sql.append(")");
                                    break;
                            }
                        }
                    } else {
                        sql.append(fieldName).append(" = ?");
                        params.add(fieldValue);
                    }
                    
                    first = false;
                }
            }
            
            if (orderBy != null && !orderBy.trim().isEmpty()) {
                sql.append(" ORDER BY ").append(orderBy);
                if ("desc".equalsIgnoreCase(orderDir)) {
                    sql.append(" DESC");
                }
            }
            
            if (limit != null && limit > 0) {
                sql.append(" LIMIT ").append(limit);
                if (offset != null && offset > 0) {
                    sql.append(" OFFSET ").append(offset);
                }
            }
            
            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            
            result.put("success", true);
            result.put("data", data);
            result.put("total", data.size());
            
            log.info("动态查询成功: {}", tableName);
        } catch (Exception e) {
            log.error("动态查询失败", e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getTablesByGroupId(Long groupId) {
        List<Map<String, Object>> tables = new ArrayList<>();
        
        try {
            QueryWrapper<DataCenterTable> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1);
            if (groupId != null) {
                queryWrapper.eq("group_id", groupId);
            }
            queryWrapper.orderByDesc("create_time");
            List<DataCenterTable> tableRecords = dataCenterTableMapper.selectList(queryWrapper);
            
            for (DataCenterTable record : tableRecords) {
                Map<String, Object> table = new HashMap<>();
                table.put("tableName", record.getTableName());
                table.put("description", record.getDescription() != null ? record.getDescription() : record.getTableName());
                table.put("groupId", record.getGroupId());
                table.put("groupName", record.getGroupName());
                table.put("sourceType", record.getSourceType());
                table.put("createTime", record.getCreateTime());
                table.put("recordCount", record.getRecordCount() != null ? record.getRecordCount() : 0L);
                tables.add(table);
            }
        } catch (Exception e) {
            log.error("获取分组下的表列表失败: {}", groupId, e);
        }
        
        return tables;
    }

    @Override
    public boolean updateTableGroup(String tableName, Long groupId, String groupName) {
        try {
            QueryWrapper<DataCenterTable> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("table_name", tableName);
            DataCenterTable tableRecord = dataCenterTableMapper.selectOne(queryWrapper);
            
            if (tableRecord != null) {
                tableRecord.setGroupId(groupId);
                tableRecord.setGroupName(groupName);
                dataCenterTableMapper.updateById(tableRecord);
                log.info("更新表分组成功: {} -> {}", tableName, groupName);
                return true;
            } else {
                log.warn("未找到表记录: {}", tableName);
                return false;
            }
        } catch (Exception e) {
            log.error("更新表分组失败: {}", tableName, e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getRecycleBinTables() {
        List<Map<String, Object>> tables = new ArrayList<>();
        
        try {
            QueryWrapper<DataCenterTable> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 0).orderByDesc("update_time");
            List<DataCenterTable> tableRecords = dataCenterTableMapper.selectList(queryWrapper);
            
            for (DataCenterTable record : tableRecords) {
                Map<String, Object> table = new HashMap<>();
                table.put("tableName", record.getTableName());
                table.put("description", record.getDescription() != null ? record.getDescription() : record.getTableName());
                table.put("groupId", record.getGroupId());
                table.put("groupName", record.getGroupName());
                table.put("sourceType", record.getSourceType());
                table.put("recordCount", record.getRecordCount() != null ? record.getRecordCount() : 0L);
                table.put("deleteTime", record.getUpdateTime());
                tables.add(table);
            }
        } catch (Exception e) {
            log.error("获取回收站表列表失败", e);
        }
        
        return tables;
    }

    @Override
    public boolean softDeleteTable(String tableName) {
        try {
            QueryWrapper<DataCenterTable> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("table_name", tableName);
            DataCenterTable tableRecord = dataCenterTableMapper.selectOne(queryWrapper);
            
            if (tableRecord != null) {
                tableRecord.setStatus(0);
                tableRecord.setUpdateTime(LocalDateTime.now());
                dataCenterTableMapper.updateById(tableRecord);
                log.info("软删除表成功: {}", tableName);
                return true;
            } else {
                log.warn("未找到表记录: {}", tableName);
                return false;
            }
        } catch (Exception e) {
            log.error("软删除表失败: {}", tableName, e);
            return false;
        }
    }

    @Override
    public boolean restoreTable(String tableName) {
        try {
            QueryWrapper<DataCenterTable> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("table_name", tableName);
            DataCenterTable tableRecord = dataCenterTableMapper.selectOne(queryWrapper);
            
            if (tableRecord != null) {
                tableRecord.setStatus(1);
                tableRecord.setUpdateTime(LocalDateTime.now());
                dataCenterTableMapper.updateById(tableRecord);
                log.info("恢复表成功: {}", tableName);
                return true;
            } else {
                log.warn("未找到表记录: {}", tableName);
                return false;
            }
        } catch (Exception e) {
            log.error("恢复表失败: {}", tableName, e);
            return false;
        }
    }

    @Override
    public boolean permanentDeleteTable(String tableName) {
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);
            
            QueryWrapper<DataCenterTable> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("table_name", tableName);
            dataCenterTableMapper.delete(queryWrapper);
            
            log.info("永久删除表成功: {}", tableName);
            return true;
        } catch (Exception e) {
            log.error("永久删除表失败: {}", tableName, e);
            return false;
        }
    }
}
