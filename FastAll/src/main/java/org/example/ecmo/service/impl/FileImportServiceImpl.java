package org.example.ecmo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.ecmo.service.FileImportService;
import org.example.ecmo.entity.DataCenterTable;
import org.example.ecmo.mapper.DataCenterTableMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class FileImportServiceImpl implements FileImportService {

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
    public Map<String, Object> importExcel(MultipartFile file, String tableName) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            Row headerRow = sheet.getRow(0);
            List<String> columns = new ArrayList<>();
            for (Cell cell : headerRow) {
                columns.add(cell.getStringCellValue());
            }
            
            List<Map<String, Object>> data = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Map<String, Object> rowData = new HashMap<>();
                for (int j = 0; j < columns.size(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                rowData.put(columns.get(j), cell.getStringCellValue());
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    rowData.put(columns.get(j), cell.getDateCellValue());
                                } else {
                                    rowData.put(columns.get(j), cell.getNumericCellValue());
                                }
                                break;
                            case BOOLEAN:
                                rowData.put(columns.get(j), cell.getBooleanCellValue());
                                break;
                            default:
                                rowData.put(columns.get(j), "");
                        }
                    } else {
                        rowData.put(columns.get(j), "");
                    }
                }
                data.add(rowData);
            }
            
            createTableFromData(tableName, columns, data);
            insertData(tableName, data);
            saveTableRecord(tableName, "import_excel", data.size());
            
            result.put("success", true);
            result.put("tableName", tableName);
            result.put("rowCount", data.size());
            result.put("columns", columns);
            
            log.info("Excel导入成功: {} - {}行数据", tableName, data.size());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> importCsv(MultipartFile file, String tableName) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            String[] headers = headerLine.split(",");
            List<String> columns = Arrays.asList(headers);
            
            List<Map<String, Object>> data = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 0; i < columns.size() && i < values.length; i++) {
                    rowData.put(columns.get(i), values[i]);
                }
                data.add(rowData);
            }
            
            createTableFromData(tableName, columns, data);
            insertData(tableName, data);
            saveTableRecord(tableName, "import_csv", data.size());
            
            result.put("success", true);
            result.put("tableName", tableName);
            result.put("rowCount", data.size());
            result.put("columns", columns);
            
            log.info("CSV导入成功: {} - {}行数据", tableName, data.size());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> importJson(MultipartFile file, String tableName) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> data = mapper.readValue(file.getInputStream(), List.class);
        
        if (data.isEmpty()) {
            throw new Exception("JSON文件为空");
        }
        
        List<String> columns = new ArrayList<>(data.get(0).keySet());
        
        createTableFromData(tableName, columns, data);
        insertData(tableName, data);
        saveTableRecord(tableName, "import_json", data.size());
        
        result.put("success", true);
        result.put("tableName", tableName);
        result.put("rowCount", data.size());
        result.put("columns", columns);
        
        log.info("JSON导入成功: {} - {}行数据", tableName, data.size());
        
        return result;
    }

    @Override
    public Map<String, Object> parseFile(MultipartFile file, String tableName) throws Exception {
        String filename = file.getOriginalFilename();
        
        if (filename == null) {
            throw new Exception("文件名为空");
        }
        
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        
        switch (extension) {
            case ".xlsx":
            case ".xls":
                return importExcel(file, tableName);
            case ".csv":
                return importCsv(file, tableName);
            case ".json":
                return importJson(file, tableName);
            default:
                throw new Exception("不支持的文件格式: " + extension);
        }
    }

    @Override
    public boolean createTableFromData(String tableName, List<String> columns, List<Map<String, Object>> data) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
            sql.append("id BIGINT PRIMARY KEY AUTO_INCREMENT");
            
            for (String column : columns) {
                String safeColumn = column.replaceAll("[^a-zA-Z0-9_]", "_");
                sql.append(", ").append(safeColumn).append(" TEXT");
            }
            
            sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            
            jdbcTemplate.execute(sql.toString());
            log.info("创建表成功: {}", tableName);
            return true;
        } catch (Exception e) {
            log.error("创建表失败: {}", tableName, e);
            return false;
        }
    }

    @Override
    public boolean insertData(String tableName, List<Map<String, Object>> data) {
        try {
            if (data.isEmpty()) {
                return true;
            }
            
            List<String> columns = new ArrayList<>(data.get(0).keySet());
            
            for (Map<String, Object> row : data) {
                StringBuilder sql = new StringBuilder();
                sql.append("INSERT INTO ").append(tableName).append(" (");
                
                for (int i = 0; i < columns.size(); i++) {
                    if (i > 0) sql.append(", ");
                    String safeColumn = columns.get(i).replaceAll("[^a-zA-Z0-9_]", "_");
                    sql.append(safeColumn);
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
            
            log.info("插入数据成功: {} - {}行", tableName, data.size());
            return true;
        } catch (Exception e) {
            log.error("插入数据失败: {}", tableName, e);
            return false;
        }
    }
}
