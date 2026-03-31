package org.example.ecmo.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface FileImportService {
    
    Map<String, Object> importExcel(MultipartFile file, String tableName) throws Exception;
    
    Map<String, Object> importCsv(MultipartFile file, String tableName) throws Exception;
    
    Map<String, Object> importJson(MultipartFile file, String tableName) throws Exception;
    
    Map<String, Object> parseFile(MultipartFile file, String tableName) throws Exception;
    
    boolean createTableFromData(String tableName, List<String> columns, List<Map<String, Object>> data);
    
    boolean insertData(String tableName, List<Map<String, Object>> data);
}
