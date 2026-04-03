package org.example.ecmo.controller;

import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.service.DataCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/data-center")
public class DataCenterController {

    @Autowired
    private DataCenterService dataCenterService;

    @AuditLog(title = "数据中心概览", businessType = 0)
    @GetMapping("/overview")
    public JsonResult getOverview() {
        Map<String, Object> overview = dataCenterService.getOverview();
        return new JsonResult(200, overview, "获取成功");
    }

    @AuditLog(title = "数据排名查询", businessType = 0)
    @GetMapping("/ranking")
    public JsonResult getRanking() {
        List<Map<String, Object>> ranking = dataCenterService.getDataRanking();
        return new JsonResult(200, ranking, "获取成功");
    }

    @AuditLog(title = "系统配额查询", businessType = 0)
    @GetMapping("/quota")
    public JsonResult getQuota() {
        List<Map<String, Object>> quota = dataCenterService.getSystemQuota();
        return new JsonResult(200, quota, "获取成功");
    }

    @AuditLog(title = "保存数据库配置", businessType = 0)
    @PostMapping("/db-config")
    public JsonResult saveDbConfig(@RequestBody Map<String, Object> config) {
        boolean result = dataCenterService.saveDbConfig(config);
        return result ? new JsonResult(200, null, "配置保存成功") : new JsonResult(500, null, "配置保存失败");
    }

    @AuditLog(title = "测试数据库连接", businessType = 0)
    @PostMapping("/test-connection")
    public JsonResult testConnection(@RequestBody Map<String, Object> config) {
        Map<String, Object> result = dataCenterService.testConnection(config);
        return new JsonResult(200, result, "测试完成");
    }

    @AuditLog(title = "查询连接状态", businessType = 0)
    @GetMapping("/connection-status")
    public JsonResult getConnectionStatus() {
        Map<String, Object> status = dataCenterService.getConnectionStatus();
        return new JsonResult(200, status, "获取成功");
    }

    @AuditLog(title = "查询数据表列表", businessType = 0)
    @GetMapping("/tables")
    public JsonResult getTables() {
        List<Map<String, Object>> tables = dataCenterService.getTableList();
        return new JsonResult(200, tables, "获取成功");
    }

    @AuditLog(title = "创建数据表", businessType = 0)
    @PostMapping("/tables")
    public JsonResult createTable(@RequestBody Map<String, Object> tableConfig) {
        log.info("收到创建表请求: {}", tableConfig);
        try {
            boolean result = dataCenterService.createTable(tableConfig);
            return result ? new JsonResult(200, null, "表创建成功") : new JsonResult(500, null, "表创建失败");
        } catch (Exception e) {
            log.error("创建表异常", e);
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "表创建失败: 未知错误";
            }
            return new JsonResult(500, null, "表创建失败: " + errorMsg);
        }
    }

    @AuditLog(title = "查询数据表详情", businessType = 0)
    @GetMapping("/tables/{tableName}")
    public JsonResult getTableInfo(@PathVariable String tableName) {
        Map<String, Object> tableInfo = dataCenterService.getTableInfo(tableName);
        return new JsonResult(200, tableInfo, "获取成功");
    }

    @AuditLog(title = "更新数据表", businessType = 0)
    @PutMapping("/tables/{tableName}")
    public JsonResult updateTable(@PathVariable String tableName, @RequestBody Map<String, Object> tableConfig) {
        boolean result = dataCenterService.updateTable(tableName, tableConfig);
        return result ? new JsonResult(200, null, "表更新成功") : new JsonResult(500, null, "表更新失败");
    }

    @AuditLog(title = "删除数据表", businessType = 0)
    @DeleteMapping("/tables/{tableName}")
    public JsonResult deleteTable(@PathVariable String tableName) {
        boolean result = dataCenterService.deleteTable(tableName);
        return result ? new JsonResult(200, null, "表删除成功") : new JsonResult(500, null, "表删除失败");
    }

    @AuditLog(title = "查询表数据", businessType = 0)
    @GetMapping("/tables/{tableName}/data")
    public JsonResult getTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        Map<String, Object> result = dataCenterService.getTableDataWithSort(tableName, page, pageSize, search, null, sortField, sortOrder);
        return new JsonResult(200, result, "获取成功");
    }

    @AuditLog(title = "新增表数据", businessType = 0)
    @PostMapping("/tables/{tableName}/data")
    public JsonResult insertData(@PathVariable String tableName, @RequestBody Map<String, Object> data, HttpServletRequest request) {
        try {
            String currentUser = request.getHeader("X-User-Name");
            if (currentUser == null || currentUser.isEmpty()) {
                currentUser = "admin";
            }
            boolean result = dataCenterService.insertData(tableName, data, currentUser);
            return result ? new JsonResult(200, null, "数据添加成功") : new JsonResult(500, null, "数据添加失败");
        } catch (Exception e) {
            log.error("添加数据异常", e);
            return new JsonResult(500, null, "数据添加失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "更新表数据", businessType = 0)
    @PutMapping("/tables/{tableName}/data/{id}")
    public JsonResult updateData(@PathVariable String tableName, @PathVariable Object id, @RequestBody Map<String, Object> data, HttpServletRequest request) {
        try {
            String currentUser = request.getHeader("X-User-Name");
            if (currentUser == null || currentUser.isEmpty()) {
                currentUser = "admin";
            }
            boolean result = dataCenterService.updateData(tableName, id, data, currentUser);
            return result ? new JsonResult(200, null, "数据更新成功") : new JsonResult(500, null, "数据更新失败");
        } catch (Exception e) {
            log.error("更新数据异常", e);
            return new JsonResult(500, null, "数据更新失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "删除表数据", businessType = 0)
    @DeleteMapping("/tables/{tableName}/data/{id}")
    public JsonResult deleteData(@PathVariable String tableName, @PathVariable Object id) {
        try {
            boolean result = dataCenterService.deleteData(tableName, id);
            return result ? new JsonResult(200, null, "数据删除成功") : new JsonResult(500, null, "数据删除失败");
        } catch (Exception e) {
            log.error("删除数据异常", e);
            return new JsonResult(500, null, "数据删除失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "批量删除表数据", businessType = 0)
    @DeleteMapping("/tables/{tableName}/data/batch")
    public JsonResult batchDeleteData(@PathVariable String tableName, @RequestBody List<Object> ids) {
        try {
            boolean result = dataCenterService.batchDeleteData(tableName, ids);
            return result ? new JsonResult(200, null, "批量删除成功") : new JsonResult(500, null, "批量删除失败");
        } catch (Exception e) {
            log.error("批量删除数据异常", e);
            return new JsonResult(500, null, "批量删除失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "导出表数据", businessType = 0)
    @GetMapping("/tables/{tableName}/export")
    public ResponseEntity<byte[]> exportTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String search) {
        try {
            byte[] data = dataCenterService.exportTableData(tableName, format, search, null);
            
            String fileName = tableName + "." + format;
            String contentType = "csv".equalsIgnoreCase(format) ? "text/csv" : "application/json";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()) + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);
        } catch (Exception e) {
            log.error("导出数据异常", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @AuditLog(title = "查询表列信息", businessType = 0)
    @GetMapping("/tables/{tableName}/columns")
    public JsonResult getTableColumns(@PathVariable String tableName) {
        try {
            List<Map<String, Object>> columns = dataCenterService.getTableColumns(tableName);
            return new JsonResult(200, columns, "获取成功");
        } catch (Exception e) {
            log.error("获取表列信息异常", e);
            return new JsonResult(500, null, "获取表列信息失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "添加表列", businessType = 0)
    @PostMapping("/tables/{tableName}/columns")
    public JsonResult addColumn(@PathVariable String tableName, @RequestBody Map<String, Object> columnConfig) {
        try {
            boolean result = dataCenterService.addColumn(tableName, columnConfig);
            return result ? new JsonResult(200, null, "添加列成功") : new JsonResult(500, null, "添加列失败");
        } catch (Exception e) {
            log.error("添加列异常", e);
            return new JsonResult(500, null, "添加列失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "修改表列", businessType = 0)
    @PutMapping("/tables/{tableName}/columns/{columnName}")
    public JsonResult modifyColumn(@PathVariable String tableName, @PathVariable String columnName, @RequestBody Map<String, Object> columnConfig) {
        try {
            boolean result = dataCenterService.modifyColumn(tableName, columnName, columnConfig);
            return result ? new JsonResult(200, null, "修改列成功") : new JsonResult(500, null, "修改列失败");
        } catch (Exception e) {
            log.error("修改列异常", e);
            return new JsonResult(500, null, "修改列失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "删除表列", businessType = 0)
    @DeleteMapping("/tables/{tableName}/columns/{columnName}")
    public JsonResult dropColumn(@PathVariable String tableName, @PathVariable String columnName) {
        try {
            boolean result = dataCenterService.dropColumn(tableName, columnName);
            return result ? new JsonResult(200, null, "删除列成功") : new JsonResult(500, null, "删除列失败");
        } catch (Exception e) {
            log.error("删除列异常", e);
            return new JsonResult(500, null, "删除列失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "生成CRUD API", businessType = 0)
    @PostMapping("/tables/{tableName}/generate-api")
    public JsonResult generateCrudApi(@PathVariable String tableName, @RequestBody Map<String, Object> options) {
        try {
            Map<String, Object> result = dataCenterService.generateCrudApi(tableName, options);
            if (Boolean.TRUE.equals(result.get("success"))) {
                return new JsonResult(200, result, (String) result.get("message"));
            } else {
                return new JsonResult(500, null, (String) result.get("message"));
            }
        } catch (Exception e) {
            log.error("生成CRUD API异常", e);
            return new JsonResult(500, null, "生成API失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "动态查询", businessType = 0)
    @PostMapping("/dynamic-query")
    public JsonResult dynamicQuery(@RequestBody Map<String, Object> queryConfig) {
        try {
            Map<String, Object> result = dataCenterService.dynamicQuery(queryConfig);
            if (Boolean.TRUE.equals(result.get("success"))) {
                return new JsonResult(200, result, "查询成功");
            } else {
                return new JsonResult(500, null, (String) result.get("message"));
            }
        } catch (Exception e) {
            log.error("动态查询异常", e);
            return new JsonResult(500, null, "查询失败: " + e.getMessage());
        }
    }
    
    @AuditLog(title = "按分组查询表", businessType = 0)
    @GetMapping("/table-groups/{groupId}/tables")
    public JsonResult getTablesByGroup(@PathVariable Long groupId) {
        List<Map<String, Object>> tables = dataCenterService.getTablesByGroupId(groupId);
        return new JsonResult(200, tables, "获取成功");
    }
    
    @AuditLog(title = "查询所有表", businessType = 0)
    @GetMapping("/table-groups/all/tables")
    public JsonResult getAllTables() {
        List<Map<String, Object>> tables = dataCenterService.getTablesByGroupId(null);
        return new JsonResult(200, tables, "获取成功");
    }
    
    @AuditLog(title = "更新表分组", businessType = 0)
    @PutMapping("/tables/{tableName}/group")
    public JsonResult updateTableGroup(@PathVariable String tableName, @RequestBody Map<String, Object> data) {
        try {
            Long groupId = data.get("groupId") != null ? Long.valueOf(data.get("groupId").toString()) : null;
            String groupName = (String) data.get("groupName");
            boolean result = dataCenterService.updateTableGroup(tableName, groupId, groupName);
            return result ? new JsonResult(200, null, "更新成功") : new JsonResult(500, null, "更新失败");
        } catch (Exception e) {
            log.error("更新表分组异常", e);
            return new JsonResult(500, null, "更新失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "获取回收站表列表", businessType = 0)
    @GetMapping("/recycle-bin")
    public JsonResult getRecycleBinTables() {
        List<Map<String, Object>> tables = dataCenterService.getRecycleBinTables();
        return new JsonResult(200, tables, "获取成功");
    }

    @AuditLog(title = "软删除表", businessType = 0)
    @PutMapping("/tables/{tableName}/soft-delete")
    public JsonResult softDeleteTable(@PathVariable String tableName) {
        boolean result = dataCenterService.softDeleteTable(tableName);
        return result ? new JsonResult(200, null, "删除成功，已移入回收站") : new JsonResult(500, null, "删除失败");
    }

    @AuditLog(title = "恢复表", businessType = 0)
    @PutMapping("/tables/{tableName}/restore")
    public JsonResult restoreTable(@PathVariable String tableName) {
        boolean result = dataCenterService.restoreTable(tableName);
        return result ? new JsonResult(200, null, "恢复成功") : new JsonResult(500, null, "恢复失败");
    }

    @AuditLog(title = "永久删除表", businessType = 0)
    @DeleteMapping("/tables/{tableName}/permanent")
    public JsonResult permanentDeleteTable(@PathVariable String tableName) {
        boolean result = dataCenterService.permanentDeleteTable(tableName);
        return result ? new JsonResult(200, null, "永久删除成功") : new JsonResult(500, null, "删除失败");
    }
}
