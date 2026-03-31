package org.example.ecmo.service;

import java.util.List;
import java.util.Map;

public interface DatabaseImportService {
    
    List<Map<String, Object>> getSourceTables(Map<String, Object> sourceConfig);
    
    Map<String, Object> getSourceTableStructure(Map<String, Object> sourceConfig, String tableName);
    
    List<Map<String, Object>> getSourceTableData(Map<String, Object> sourceConfig, String tableName);
    
    boolean importTableFromSource(Map<String, Object> sourceConfig, String sourceTableName, String targetTableName);
    
    boolean importMultipleTables(Map<String, Object> sourceConfig, List<String> sourceTableNames);
}
