package org.example.ecmo.controller;

import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/crud")
public class CrudDashboardController {

    @Autowired
    private CrudService crudService;

    @AuditLog(title = "CRUD概览", businessType = 0)
    @GetMapping("/overview")
    public JsonResult<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("tableCount", crudService.getTableCount());
        overview.put("totalRecords", crudService.getTotalRecords());
        overview.put("storageUsed", crudService.getStorageUsed());
        overview.put("weeklyNew", crudService.getWeeklyNewRecords());
        return JsonResult.success(overview);
    }

    @AuditLog(title = "数据排名查询", businessType = 0)
    @GetMapping("/ranking")
    public JsonResult<List<Map<String, Object>>> getRanking() {
        return JsonResult.success(crudService.getDataRanking());
    }

    @AuditLog(title = "配额查询", businessType = 0)
    @GetMapping("/quota")
    public JsonResult<Map<String, Object>> getQuota() {
        Map<String, Object> quota = new HashMap<>();
        quota.put("tableLimit", 100);
        quota.put("tableUsed", crudService.getTableCount());
        quota.put("usagePercentage", (int) ((crudService.getTableCount() / 100.0) * 100));
        return JsonResult.success(quota);
    }

    @AuditLog(title = "数据库连接", businessType = 0)
    @PostMapping("/connect")
    public JsonResult<String> connect(@RequestBody Map<String, Object> params) {
        return JsonResult.success("连接成功");
    }

    @AuditLog(title = "创建数据表", businessType = 0)
    @PostMapping("/table/create")
    public JsonResult<String> createTable(@RequestBody Map<String, Object> params) {
        try {
            String tableName = (String) params.get("tableName");
            String moduleName = (String) params.get("moduleName");
            String description = (String) params.get("description");
            List<Map<String, Object>> fields = (List<Map<String, Object>>) params.get("fields");
            
            crudService.createTable(tableName, moduleName, description, fields);
            return JsonResult.success("创建成功");
        } catch (Exception e) {
            return JsonResult.error("创建失败: " + e.getMessage());
        }
    }
}
