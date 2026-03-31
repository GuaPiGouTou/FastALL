package org.example.ecmo.controller;

import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.service.FileImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/data-center/import")
public class FileImportController {

    @Autowired
    private FileImportService fileImportService;

    @AuditLog(title = "文件导入", businessType = 0)
    @PostMapping("/file")
    public JsonResult importFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tableName") String tableName) {
        try {
            if (file.isEmpty()) {
                return new JsonResult(400, null, "文件不能为空");
            }
            
            if (tableName == null || tableName.trim().isEmpty()) {
                return new JsonResult(400, null, "表名不能为空");
            }
            
            Map<String, Object> result = fileImportService.parseFile(file, tableName);
            return new JsonResult(200, result, "导入成功");
        } catch (Exception e) {
            return new JsonResult(500, null, "导入失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "Excel导入", businessType = 0)
    @PostMapping("/excel")
    public JsonResult importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tableName") String tableName) {
        try {
            Map<String, Object> result = fileImportService.importExcel(file, tableName);
            return new JsonResult(200, result, "Excel导入成功");
        } catch (Exception e) {
            return new JsonResult(500, null, "Excel导入失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "CSV导入", businessType = 0)
    @PostMapping("/csv")
    public JsonResult importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tableName") String tableName) {
        try {
            Map<String, Object> result = fileImportService.importCsv(file, tableName);
            return new JsonResult(200, result, "CSV导入成功");
        } catch (Exception e) {
            return new JsonResult(500, null, "CSV导入失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "JSON导入", businessType = 0)
    @PostMapping("/json")
    public JsonResult importJson(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tableName") String tableName) {
        try {
            Map<String, Object> result = fileImportService.importJson(file, tableName);
            return new JsonResult(200, result, "JSON导入成功");
        } catch (Exception e) {
            return new JsonResult(500, null, "JSON导入失败: " + e.getMessage());
        }
    }
}
