package org.example.ecmo.controller;

import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.ApiDefinition;
import org.example.ecmo.entity.ApiCallLog;
import org.example.ecmo.service.ApiDefinitionService;
import org.example.ecmo.service.ApiCallLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/dynamic")
public class DynamicApiController {

    @Autowired
    private ApiDefinitionService apiDefinitionService;

    @Autowired
    private ApiCallLogService apiCallLogService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AuditLog(title = "动态API调用", businessType = 0)
    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public Object handleDynamicApi(
            HttpServletRequest request,
            @RequestBody(required = false) Map<String, Object> bodyParams,
            @RequestParam(required = false) Map<String, Object> queryParams) {
        
        String requestUri = request.getRequestURI();
        String apiPath = requestUri.replace("/api/dynamic", "");
        String method = request.getMethod();
        
        ApiDefinition api = apiDefinitionService.findPublishedByPathAndMethod(apiPath, method);
        if (api == null) {
            return JsonResult.error("API不存在: " + apiPath);
        }
        
        if (!"published".equals(api.getStatus())) {
            return JsonResult.error("API未发布");
        }
        
        Map<String, Object> params = new HashMap<>();
        if (queryParams != null) {
            params.putAll(queryParams);
        }
        if (bodyParams != null) {
            params.putAll(bodyParams);
        }
        
        return executeApi(api, params, request);
    }

    private Object executeApi(ApiDefinition api, Map<String, Object> params, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        ApiCallLog log = new ApiCallLog();
        log.setApiId(api.getId());
        log.setApiPath(api.getApiPath());
        log.setApiMethod(api.getApiMethod());
        log.setRequestParams(params != null ? params.toString() : null);
        log.setIpAddress(getClientIp(request));
        
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
            
            return JsonResult.success(result);
        } catch (Exception e) {
            log.setStatus("error");
            log.setErrorMsg(e.getMessage());
            return JsonResult.error("执行失败: " + e.getMessage());
        } finally {
            apiCallLogService.saveLog(log);
        }
    }

    private Object executeCrudOperation(ApiDefinition api, Map<String, Object> params) {
        String tableName = api.getTableName();
        String method = api.getApiMethod().toUpperCase();
        
        switch (method) {
            case "GET":
                return executeGet(tableName, params);
            case "POST":
                return executePost(tableName, params);
            case "PUT":
                return executePut(tableName, params);
            case "DELETE":
                return executeDelete(tableName, params);
            default:
                throw new RuntimeException("不支持的请求方法: " + method);
        }
    }

    private Object executeGet(String tableName, Map<String, Object> params) {
        if (params != null && params.containsKey("id")) {
            String sql = "SELECT * FROM " + tableName + " WHERE id = " + params.get("id");
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            return results.isEmpty() ? null : results.get(0);
        } else {
            StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);
            
            if (params != null && !params.isEmpty()) {
                sql.append(" WHERE ");
                boolean first = true;
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (!first) {
                        sql.append(" AND ");
                    }
                    sql.append(entry.getKey()).append(" = '").append(entry.getValue()).append("'");
                    first = false;
                }
            }
            
            return jdbcTemplate.queryForList(sql.toString());
        }
    }

    private Object executePost(String tableName, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("POST请求需要提供数据");
        }
        
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : params.entrySet()) {
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

    private Object executePut(String tableName, Map<String, Object> params) {
        if (params == null || !params.containsKey("id")) {
            throw new RuntimeException("PUT请求需要提供id参数");
        }
        
        Object id = params.remove("id");
        StringBuilder setClause = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (setClause.length() > 0) {
                setClause.append(", ");
            }
            setClause.append(entry.getKey()).append(" = '").append(entry.getValue()).append("'");
        }
        
        String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE id = " + id;
        return jdbcTemplate.update(sql);
    }

    private Object executeDelete(String tableName, Map<String, Object> params) {
        if (params == null || !params.containsKey("id")) {
            throw new RuntimeException("DELETE请求需要提供id参数");
        }
        
        String sql = "DELETE FROM " + tableName + " WHERE id = " + params.get("id");
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

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
