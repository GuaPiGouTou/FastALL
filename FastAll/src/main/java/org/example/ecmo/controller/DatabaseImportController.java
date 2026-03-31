package org.example.ecmo.controller;

import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.service.DatabaseImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data-center/import/database")
public class DatabaseImportController {

    @Autowired
    private DatabaseImportService databaseImportService;

    @AuditLog(title = "查询源数据库表", businessType = 0)
    @PostMapping("/tables")
    public JsonResult getSourceTables(@RequestBody Map<String, Object> sourceConfig) {
        try {
            List<Map<String, Object>> tables = databaseImportService.getSourceTables(sourceConfig);
            return new JsonResult(200, tables, "获取源数据库表列表成功");
        } catch (Exception e) {
            return new JsonResult(500, null, "获取源数据库表列表失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "查询源表结构", businessType = 0)
    @PostMapping("/table/structure")
    public JsonResult getSourceTableStructure(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> sourceConfig = (Map<String, Object>) request.get("sourceConfig");
            String tableName = (String) request.get("tableName");
            
            Map<String, Object> structure = databaseImportService.getSourceTableStructure(sourceConfig, tableName);
            return new JsonResult(200, structure, "获取源表结构成功");
        } catch (Exception e) {
            return new JsonResult(500, null, "获取源表结构失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "查询源表数据", businessType = 0)
    @PostMapping("/table/data")
    public JsonResult getSourceTableData(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> sourceConfig = (Map<String, Object>) request.get("sourceConfig");
            String tableName = (String) request.get("tableName");
            
            List<Map<String, Object>> data = databaseImportService.getSourceTableData(sourceConfig, tableName);
            return new JsonResult(200, data, "获取源表数据成功");
        } catch (Exception e) {
            return new JsonResult(500, null, "获取源表数据失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "导入数据表", businessType = 0)
    @PostMapping("/table")
    public JsonResult importTable(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> sourceConfig = (Map<String, Object>) request.get("sourceConfig");
            String sourceTableName = (String) request.get("sourceTableName");
            String targetTableName = (String) request.get("targetTableName");
            
            boolean result = databaseImportService.importTableFromSource(sourceConfig, sourceTableName, targetTableName);
            return result ? new JsonResult(200, null, "导入表成功") : new JsonResult(500, null, "导入表失败");
        } catch (Exception e) {
            return new JsonResult(500, null, "导入表失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "批量导入数据表", businessType = 0)
    @PostMapping("/tables/batch")
    public JsonResult importMultipleTables(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> sourceConfig = (Map<String, Object>) request.get("sourceConfig");
            List<String> sourceTableNames = (List<String>) request.get("sourceTableNames");
            
            boolean result = databaseImportService.importMultipleTables(sourceConfig, sourceTableNames);
            return result ? new JsonResult(200, null, "批量导入表成功") : new JsonResult(500, null, "批量导入表失败");
        } catch (Exception e) {
            return new JsonResult(500, null, "批量导入表失败: " + e.getMessage());
        }
    }
}
