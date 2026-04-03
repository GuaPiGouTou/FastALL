package org.example.ecmo.service;

import java.util.List;
import java.util.Map;

public interface DataCenterService {
    
    Map<String, Object> getOverview();
    
    List<Map<String, Object>> getDataRanking();
    
    List<Map<String, Object>> getSystemQuota();
    
    boolean saveDbConfig(Map<String, Object> config);
    
    Map<String, Object> testConnection(Map<String, Object> config);
    
    Map<String, Object> getConnectionStatus();
    
    List<Map<String, Object>> getTableList();
    
    boolean createTable(Map<String, Object> tableConfig);
    
    Map<String, Object> getTableInfo(String tableName);
    
    boolean updateTable(String tableName, Map<String, Object> tableConfig);
    
    boolean deleteTable(String tableName);
    
    Map<String, Object> getTableData(String tableName, int page, int pageSize);
    
    Map<String, Object> getTableData(String tableName, int page, int pageSize, String search, Map<String, Object> filters);
    
    Map<String, Object> getTableDataWithSort(String tableName, int page, int pageSize, String search, Map<String, Object> filters, String sortField, String sortOrder);
    
    boolean insertData(String tableName, Map<String, Object> data, String currentUser);
    
    boolean updateData(String tableName, Object id, Map<String, Object> data, String currentUser);
    
    boolean deleteData(String tableName, Object id);
    
    boolean batchDeleteData(String tableName, List<Object> ids);
    
    byte[] exportTableData(String tableName, String format, String search, Map<String, Object> filters);
    
    List<Map<String, Object>> getTableColumns(String tableName);
    
    boolean addColumn(String tableName, Map<String, Object> columnConfig);
    
    boolean modifyColumn(String tableName, String columnName, Map<String, Object> columnConfig);
    
    boolean dropColumn(String tableName, String columnName);
    
    Map<String, Object> generateCrudApi(String tableName, Map<String, Object> options);
    
    Map<String, Object> dynamicQuery(Map<String, Object> queryConfig);
    
    List<Map<String, Object>> getTablesByGroupId(Long groupId);
    
    boolean updateTableGroup(String tableName, Long groupId, String groupName);
    
    List<Map<String, Object>> getRecycleBinTables();
    
    boolean softDeleteTable(String tableName);
    
    boolean restoreTable(String tableName);
    
    boolean permanentDeleteTable(String tableName);

    /** 正常状态且归属某 API 分组的数据表数量（用于删除分组前校验） */
    long countByGroupId(Long groupId);
}
