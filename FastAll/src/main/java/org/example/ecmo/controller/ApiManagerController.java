package org.example.ecmo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.ApiDefinition;
import org.example.ecmo.entity.ApiCallLog;
import org.example.ecmo.service.ApiDefinitionService;
import org.example.ecmo.service.ApiCallLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/api-manager")
public class ApiManagerController {

    @Autowired
    private ApiDefinitionService apiDefinitionService;

    @Autowired
    private ApiCallLogService apiCallLogService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AuditLog(title = "查询API列表", businessType = 0)
    @GetMapping("/list")
    public JsonResult getApiList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String apiMethod) {
        
        LambdaQueryWrapper<ApiDefinition> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ApiDefinition::getApiName, keyword)
                    .or().like(ApiDefinition::getApiPath, keyword)
                    .or().like(ApiDefinition::getDescription, keyword));
        }
        if (groupName != null && !groupName.isEmpty()) {
            wrapper.eq(ApiDefinition::getGroupName, groupName);
        }
        if (apiMethod != null && !apiMethod.isEmpty()) {
            wrapper.eq(ApiDefinition::getApiMethod, apiMethod);
        }
        
        wrapper.orderByDesc(ApiDefinition::getCreateTime);
        
        Page<ApiDefinition> pageResult = apiDefinitionService.page(new Page<>(page, size), wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return JsonResult.success(result);
    }

    @AuditLog(title = "查询API详情", businessType = 0)
    @GetMapping("/{id}")
    public JsonResult getApiDetail(@PathVariable Long id) {
        ApiDefinition api = apiDefinitionService.getById(id);
        if (api == null) {
            return JsonResult.error("API不存在");
        }
        return JsonResult.success(api);
    }

    @AuditLog(title = "创建API", businessType = 0)
    @PostMapping("/create")
    public JsonResult createApi(@RequestBody ApiDefinition apiDefinition) {
        ApiDefinition existing = apiDefinitionService.findPublishedByPathAndMethod(
                apiDefinition.getApiPath(), apiDefinition.getApiMethod());
        if (existing != null) {
            return JsonResult.error("该API路径和方法已存在");
        }
        
        apiDefinition.setDeleted(0);
        apiDefinition.setCallCount(0L);
        apiDefinition.setErrorCount(0L);
        boolean result = apiDefinitionService.save(apiDefinition);
        return result ? JsonResult.success("创建成功") : JsonResult.error("创建失败");
    }

    @AuditLog(title = "更新API", businessType = 0)
    @PutMapping("/{id}")
    public JsonResult updateApi(@PathVariable Long id, @RequestBody ApiDefinition apiDefinition) {
        apiDefinition.setId(id);
        boolean result = apiDefinitionService.updateById(apiDefinition);
        return result ? JsonResult.success("更新成功") : JsonResult.error("更新失败");
    }

    @AuditLog(title = "删除API", businessType = 0)
    @DeleteMapping("/{id}")
    public JsonResult deleteApi(@PathVariable Long id) {
        boolean result = apiDefinitionService.removeById(id);
        return result ? JsonResult.success("删除成功") : JsonResult.error("删除失败");
    }

    @AuditLog(title = "测试API", businessType = 0)
    @PostMapping("/{id}/test")
    public JsonResult testApi(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        ApiDefinition api = apiDefinitionService.getById(id);
        if (api == null) {
            return JsonResult.error("API不存在");
        }
        
        if (!"published".equals(api.getStatus())) {
            return JsonResult.error("API未发布");
        }
        
        try {
            Object result = executeApi(api, params);
            return JsonResult.success(result);
        } catch (Exception e) {
            return JsonResult.error("执行失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "API管理概览", businessType = 0)
    @GetMapping("/overview")
    public JsonResult getOverview() {
        Map<String, Object> overview = apiDefinitionService.getOverview();
        return JsonResult.success(overview);
    }

    @AuditLog(title = "API模块统计", businessType = 0)
    @GetMapping("/stats/module")
    public JsonResult getStatsByModule() {
        return JsonResult.success(apiDefinitionService.getCountByGroup());
    }

    @AuditLog(title = "API方法统计", businessType = 0)
    @GetMapping("/stats/method")
    public JsonResult getStatsByMethod() {
        return JsonResult.success(apiDefinitionService.getCountByMethod());
    }

    @AuditLog(title = "查询API调用日志", businessType = 0)
    @GetMapping("/logs")
    public JsonResult getCallLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long apiId,
            @RequestParam(required = false) String status) {
        
        LambdaQueryWrapper<ApiCallLog> wrapper = new LambdaQueryWrapper<>();
        
        if (apiId != null) {
            wrapper.eq(ApiCallLog::getApiId, apiId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ApiCallLog::getStatus, status);
        }
        
        wrapper.orderByDesc(ApiCallLog::getCreateTime);
        
        Page<ApiCallLog> pageResult = apiCallLogService.page(new Page<>(page, size), wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return JsonResult.success(result);
    }

    @AuditLog(title = "查询热门API", businessType = 0)
    @GetMapping("/logs/top")
    public JsonResult getTopCalledApis() {
        return JsonResult.success(apiCallLogService.getTopCalledApis());
    }

    @AuditLog(title = "查询API调用趋势", businessType = 0)
    @GetMapping("/logs/trend")
    public JsonResult getCallTrend() {
        return JsonResult.success(apiCallLogService.getCallTrend());
    }

    private Object executeApi(ApiDefinition api, Map<String, Object> params) {
        long startTime = System.currentTimeMillis();
        ApiCallLog log = new ApiCallLog();
        log.setApiId(api.getId());
        log.setApiPath(api.getApiPath());
        log.setApiMethod(api.getApiMethod());
        log.setRequestParams(params != null ? params.toString() : null);
        
        try {
            Object result;
            
            if (api.getSqlTemplate() != null && !api.getSqlTemplate().isEmpty()) {
                String sql = buildSql(api.getSqlTemplate(), params);
                result = jdbcTemplate.queryForList(sql);
            } else {
                result = executeCrudOperation(api, params);
            }
            
            log.setStatus("success");
            log.setResponseData(result != null ? result.toString() : null);
            
            long endTime = System.currentTimeMillis();
            log.setExecuteTime((int) (endTime - startTime));
            
            return result;
        } catch (Exception e) {
            log.setStatus("error");
            log.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            apiCallLogService.saveLog(log);
        }
    }

    private Object executeCrudOperation(ApiDefinition api, Map<String, Object> params) {
        String tableName = api.getTableName();
        String method = api.getApiMethod().toUpperCase();
        
        switch (method) {
            case "GET":
                if (params != null && params.containsKey("id")) {
                    String sql = "SELECT * FROM " + tableName + " WHERE id = " + params.get("id");
                    List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
                    return results.isEmpty() ? null : results.get(0);
                } else {
                    String listSql = "SELECT * FROM " + tableName;
                    return jdbcTemplate.queryForList(listSql);
                }
            case "POST":
                if (params != null && !params.isEmpty()) {
                    return insertData(tableName, params);
                }
                return null;
            case "PUT":
                if (params != null && params.containsKey("id")) {
                    return updateData(tableName, params);
                }
                return null;
            case "DELETE":
                if (params != null && params.containsKey("id")) {
                    String deleteSql = "DELETE FROM " + tableName + " WHERE id = " + params.get("id");
                    return jdbcTemplate.update(deleteSql);
                }
                return null;
            default:
                throw new RuntimeException("不支持的请求方法: " + method);
        }
    }

    private int insertData(String tableName, Map<String, Object> data) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (columns.length() > 0) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(entry.getKey());
            values.append("'").append(entry.getValue()).append("'");
        }
        
        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        return jdbcTemplate.update(sql);
    }

    private int updateData(String tableName, Map<String, Object> data) {
        StringBuilder setClause = new StringBuilder();
        Object id = data.remove("id");
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (setClause.length() > 0) {
                setClause.append(", ");
            }
            setClause.append(entry.getKey()).append(" = '").append(entry.getValue()).append("'");
        }
        
        String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE id = " + id;
        return jdbcTemplate.update(sql);
    }

    private String buildSql(String template, Map<String, Object> params) {
        String sql = template;
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                sql = sql.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
            }
        }
        return sql;
    }
}
