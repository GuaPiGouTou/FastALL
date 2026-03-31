package org.example.ecmo.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExcelImportService {

    /**
     * 导入 Excel 文件
     * @param file Excel 文件
     * @param moduleName 模块名称
     * @param tableName 表名（可选）
     * @return 导入结果
     */
    public Map<String, Object> importExcel(MultipartFile file, String moduleName, String tableName) {
        try {
            // 解析 Excel 文件
            List<Map<String, Object>> dataList = parseExcel(file);
            
            // 推断字段类型
            Map<String, String> fieldTypes = inferFieldTypes(dataList);
            
            // 创建表结构
            if (tableName == null || tableName.isEmpty()) {
                tableName = "tb_" + moduleName.toLowerCase().replace(" ", "_");
            }
            createTable(tableName, fieldTypes);
            
            // 插入元数据配置
            insertFieldConfigs(tableName, fieldTypes);
            
            // 批量导入数据
            batchImportData(tableName, dataList);
            
            // 注册为系统模块
            registerModule(moduleName, tableName);
            
            // 构建返回结果
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("message", "导入成功");
            result.put("tableName", tableName);
            result.put("dataCount", dataList.size());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 解析 Excel 文件
     * @param file Excel 文件
     * @return 数据列表
     * @throws IOException IO 异常
     */
    private List<Map<String, Object>> parseExcel(MultipartFile file) throws IOException {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<String, Object>>() {
            @Override
            public void invoke(Map<String, Object> data, AnalysisContext context) {
                dataList.add(data);
            }
            
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // 解析完成后的操作
            }
        }).sheet().doRead();
        
        return dataList;
    }

    /**
     * 推断字段类型
     * @param dataList 数据列表
     * @return 字段类型映射
     */
    private Map<String, String> inferFieldTypes(List<Map<String, Object>> dataList) {
        Map<String, String> fieldTypes = new java.util.HashMap<>();
        
        if (dataList.isEmpty()) {
            return fieldTypes;
        }
        
        // 获取所有字段名
        Map<String, Object> firstRow = dataList.get(0);
        for (String fieldName : firstRow.keySet()) {
            // 默认为字符串类型
            String fieldType = "VARCHAR(255)";
            
            // 尝试推断字段类型
            boolean isNumber = true;
            boolean isDate = false;
            
            for (Map<String, Object> row : dataList) {
                Object value = row.get(fieldName);
                if (value != null) {
                    if (value instanceof Number) {
                        // 数字类型
                    } else if (value instanceof String) {
                        String strValue = (String) value;
                        // 尝试解析为数字
                        try {
                            Double.parseDouble(strValue);
                        } catch (NumberFormatException e) {
                            isNumber = false;
                        }
                        // 尝试解析为日期
                        if (strValue.matches("\\d{4}-\\d{2}-\\d{2}") || strValue.matches("\\d{4}/\\d{2}/\\d{2}")) {
                            isDate = true;
                        }
                    } else {
                        isNumber = false;
                    }
                }
            }
            
            if (isNumber) {
                fieldType = "DOUBLE";
            } else if (isDate) {
                fieldType = "DATE";
            }
            
            fieldTypes.put(fieldName, fieldType);
        }
        
        return fieldTypes;
    }

    /**
     * 创建表结构
     * @param tableName 表名
     * @param fieldTypes 字段类型映射
     */
    private void createTable(String tableName, Map<String, String> fieldTypes) {
        // 构建 CREATE TABLE 语句
        StringBuilder createTableSql = new StringBuilder();
        createTableSql.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (");
        createTableSql.append("`id` BIGINT NOT NULL AUTO_INCREMENT,");
        
        for (Map.Entry<String, String> entry : fieldTypes.entrySet()) {
            String fieldName = entry.getKey();
            String fieldType = entry.getValue();
            createTableSql.append("`").append(fieldName).append("` " ).append(fieldType).append(",");
        }
        
        createTableSql.append("`created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        createTableSql.append("`updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,");
        createTableSql.append("PRIMARY KEY (`id`)");
        createTableSql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='").append(tableName).append("';");
        
        // 执行 SQL 语句
        // 这里需要使用 JdbcTemplate 或其他方式执行 SQL
        System.out.println("Create table SQL: " + createTableSql.toString());
    }

    /**
     * 插入字段配置
     * @param tableName 表名
     * @param fieldTypes 字段类型映射
     */
    private void insertFieldConfigs(String tableName, Map<String, String> fieldTypes) {
        int sortOrder = 1;
        for (Map.Entry<String, String> entry : fieldTypes.entrySet()) {
            String fieldName = entry.getKey();
            String fieldType = entry.getValue();
            
            // 推断 UI 类型
            String uiType = "Input";
            if (fieldType.contains("INT") || fieldType.contains("DOUBLE") || fieldType.contains("FLOAT")) {
                uiType = "InputNumber";
            } else if (fieldType.contains("DATE") || fieldType.contains("DATETIME")) {
                uiType = "DatePicker";
            } else if (fieldType.contains("BOOLEAN")) {
                uiType = "Switch";
            }
            
            // 构建插入字段配置的 SQL
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("INSERT INTO sys_field_config (table_name, prop, label, ui_type, options, is_show_in_list, sort_order, create_time, update_time)");
            insertSql.append(" VALUES ('").append(tableName).append("', '").append(fieldName).append("', '").append(fieldName).append("', '").append(uiType).append("', NULL, 1, " ).append(sortOrder).append(", NOW(), NOW());");
            
            // 执行 SQL 语句
            System.out.println("Insert field config SQL: " + insertSql.toString());
            sortOrder++;
        }
    }

    /**
     * 批量导入数据
     * @param tableName 表名
     * @param dataList 数据列表
     */
    private void batchImportData(String tableName, List<Map<String, Object>> dataList) {
        if (dataList.isEmpty()) {
            return;
        }
        
        // 获取字段名
        Map<String, Object> firstRow = dataList.get(0);
        List<String> fieldNames = new ArrayList<>(firstRow.keySet());
        
        // 构建批量插入 SQL
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO `").append(tableName).append("` (");
        
        // 添加字段名
        for (String fieldName : fieldNames) {
            insertSql.append("`").append(fieldName).append("` ,");
        }
        insertSql.append("created_time, updated_time");
        insertSql.append(") VALUES ");
        
        // 添加数据
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> row = dataList.get(i);
            insertSql.append("(");
            
            for (String fieldName : fieldNames) {
                Object value = row.get(fieldName);
                if (value == null) {
                    insertSql.append("NULL ,");
                } else if (value instanceof String) {
                    insertSql.append("'").append(value).append("' ,");
                } else {
                    insertSql.append(value).append(" ,");
                }
            }
            insertSql.append("NOW(), NOW()");
            insertSql.append(")");
            
            if (i < dataList.size() - 1) {
                insertSql.append(",");
            }
        }
        
        // 执行 SQL 语句
        System.out.println("Batch insert data SQL: " + insertSql.toString());
    }

    /**
     * 注册为系统模块
     * @param moduleName 模块名称
     * @param tableName 表名
     */
    private void registerModule(String moduleName, String tableName) {
        // 构建插入模块的 SQL
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO module (module_name, table_name, description, status, create_time, update_time)");
        insertSql.append(" VALUES ('").append(moduleName).append("', '").append(tableName).append("', '").append(moduleName).append("', 1, NOW(), NOW()) ON DUPLICATE KEY UPDATE description = '").append(moduleName).append("', status = 1, update_time = NOW();");
        
        // 执行 SQL 语句
        System.out.println("Register module SQL: " + insertSql.toString());
    }
}