package org.example.ecmo.service.impl;

import org.example.ecmo.service.DatabaseImportService;
import org.example.ecmo.factory.DatabaseConnectionFactory;
import org.example.ecmo.entity.DataCenterTable;
import org.example.ecmo.mapper.DataCenterTableMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class DatabaseImportServiceImpl implements DatabaseImportService {

    @Autowired
    private DatabaseConnectionFactory databaseConnectionFactory;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DataCenterTableMapper dataCenterTableMapper;

    private void saveTableRecord(String tableName, String sourceType, int rowCount) {
        try {
            DataCenterTable record = new DataCenterTable();
            record.setTableName(tableName);
            record.setSourceType(sourceType);
            record.setRecordCount((long) rowCount);
            record.setStatus(1);
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            dataCenterTableMapper.insert(record);
        } catch (Exception e) {
            log.error("保存表记录失败: {}", tableName, e);
        }
    }

    @Override
    public List<Map<String, Object>> getSourceTables(Map<String, Object> sourceConfig) {
        List<Map<String, Object>> tables = new ArrayList<>();
        
        try (Connection connection = createSourceConnection(sourceConfig)) {
            DatabaseMetaData metaData = connection.getMetaData();
            String schema = connection.getCatalog();
            
            try (ResultSet rs = metaData.getTables(schema, schema, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    Map<String, Object> table = new HashMap<>();
                    table.put("tableName", rs.getString("TABLE_NAME"));
                    table.put("description", rs.getString("REMARKS"));
                    tables.add(table);
                }
            }
            
            log.info("获取源数据库表列表成功，共{}个表", tables.size());
        } catch (Exception e) {
            log.error("获取源数据库表列表失败", e);
        }
        
        return tables;
    }

    @Override
    public Map<String, Object> getSourceTableStructure(Map<String, Object> sourceConfig, String tableName) {
        Map<String, Object> structure = new HashMap<>();
        
        try (Connection connection = createSourceConnection(sourceConfig)) {
            DatabaseMetaData metaData = connection.getMetaData();
            String schema = connection.getCatalog();
            
            structure.put("tableName", tableName);
            
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
            structure.put("columns", columns);
            
            log.info("获取源表结构成功: {}", tableName);
        } catch (Exception e) {
            log.error("获取源表结构失败: {}", tableName, e);
        }
        
        return structure;
    }

    @Override
    public List<Map<String, Object>> getSourceTableData(Map<String, Object> sourceConfig, String tableName) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        try (Connection connection = createSourceConnection(sourceConfig)) {
            String sql = "SELECT * FROM " + tableName;
            
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = resultSet.getObject(i);
                        row.put(columnName, value);
                    }
                    data.add(row);
                }
            }
            
            log.info("获取源表数据成功: {} - {}条记录", tableName, data.size());
        } catch (Exception e) {
            log.error("获取源表数据失败: {}", tableName, e);
        }
        
        return data;
    }

    @Override
    public boolean importTableFromSource(Map<String, Object> sourceConfig, String sourceTableName, String targetTableName) {
        try {
            Map<String, Object> structure = getSourceTableStructure(sourceConfig, sourceTableName);
            List<Map<String, Object>> data = getSourceTableData(sourceConfig, sourceTableName);
            
            createTargetTable(targetTableName, structure);
            insertTargetData(targetTableName, data);
            saveTableRecord(targetTableName, "database_import", data.size());
            
            log.info("从源数据库导入表成功: {} -> {}", sourceTableName, targetTableName);
            return true;
        } catch (Exception e) {
            log.error("从源数据库导入表失败: {} -> {}", sourceTableName, targetTableName, e);
            return false;
        }
    }

    @Override
    public boolean importMultipleTables(Map<String, Object> sourceConfig, List<String> sourceTableNames) {
        try {
            for (String tableName : sourceTableNames) {
                importTableFromSource(sourceConfig, tableName, tableName);
            }
            
            log.info("批量导入表成功，共{}个表", sourceTableNames.size());
            return true;
        } catch (Exception e) {
            log.error("批量导入表失败", e);
            return false;
        }
    }
    
    private Connection createSourceConnection(Map<String, Object> sourceConfig) throws Exception {
        String dbType = (String) sourceConfig.get("type");
        String host = (String) sourceConfig.get("host");
        Integer port = (Integer) sourceConfig.get("port");
        String database = (String) sourceConfig.get("database");
        String username = (String) sourceConfig.get("username");
        String password = (String) sourceConfig.get("password");
        
        return databaseConnectionFactory.createConnection(dbType, host, port, database, username, password);
    }
    
    private void createTargetTable(String tableName, Map<String, Object> structure) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        sql.append("id BIGINT PRIMARY KEY AUTO_INCREMENT");
        
        List<Map<String, Object>> columns = (List<Map<String, Object>>) structure.get("columns");
        if (columns != null) {
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("columnName");
                String dataType = (String) column.get("dataType");
                
                sql.append(", ").append(columnName).append(" ");
                sql.append(mapDataType(dataType));
            }
        }
        
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        
        jdbcTemplate.execute(sql.toString());
        log.info("创建目标表成功: {}", tableName);
    }
    
    private void insertTargetData(String tableName, List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            return;
        }
        
        List<String> columns = new ArrayList<>(data.get(0).keySet());
        
        for (Map<String, Object> row : data) {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(tableName).append(" (");
            
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append(columns.get(i));
            }
            
            sql.append(") VALUES (");
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
            }
            sql.append(")");
            
            Object[] values = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                values[i] = row.get(columns.get(i));
            }
            
            jdbcTemplate.update(sql.toString(), values);
        }
        
        log.info("插入目标表数据成功: {} - {}条记录", tableName, data.size());
    }
    
    private String mapDataType(String sourceType) {
        if (sourceType == null) {
            return "TEXT";
        }
        
        String type = sourceType.toUpperCase();
        
        if (type.contains("INT") || type.contains("INTEGER")) {
            return "BIGINT";
        } else if (type.contains("VARCHAR") || type.contains("CHAR")) {
            return "VARCHAR(255)";
        } else if (type.contains("TEXT") || type.contains("CLOB")) {
            return "TEXT";
        } else if (type.contains("DATE") || type.contains("TIME")) {
            return "DATETIME";
        } else if (type.contains("DECIMAL") || type.contains("NUMERIC") || type.contains("FLOAT") || type.contains("DOUBLE")) {
            return "DECIMAL(10,2)";
        } else if (type.contains("BOOL")) {
            return "BOOLEAN";
        } else if (type.contains("BLOB") || type.contains("BINARY")) {
            return "BLOB";
        } else {
            return "TEXT";
        }
    }
}
