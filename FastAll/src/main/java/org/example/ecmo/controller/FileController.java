package org.example.ecmo.controller;

import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.service.ExcelImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/excel-import")
public class FileController {

    @Autowired
    private ExcelImportService excelImportService;

    @AuditLog(title = "Excel导入", businessType = 0)
    @PostMapping("/import-excel")
    public JsonResult importExcel(@RequestParam("file") MultipartFile file, @RequestParam("moduleName") String moduleName, @RequestParam(required = false) String tableName) {
        Map<String, Object> result = excelImportService.importExcel(file, moduleName, tableName);
        boolean success = (boolean) result.get("success");
        String message = (String) result.get("message");
        return success ? JsonResult.success(result) : JsonResult.error(message);
    }
}