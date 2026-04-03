package org.example.ecmo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.dto.ApiDefinitionDTO;
import org.example.ecmo.entity.ApiDefinition;
import org.example.ecmo.entity.ApiGroup;
import org.example.ecmo.entity.ApiVersion;
import org.example.ecmo.entity.ApiDatasource;
import org.example.ecmo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v2/api-manager")
public class ApiManagerV2Controller {

    @Autowired
    private ApiDefinitionService apiDefinitionService;

    @Autowired
    private ApiGroupService apiGroupService;

    @Autowired
    private ApiVersionService apiVersionService;

    @Autowired
    private ApiDatasourceService apiDatasourceService;

    @Autowired
    private DataCenterService dataCenterService;

    @AuditLog(title = "API管理概览", businessType = 0)
    @GetMapping("/overview")
    public JsonResult getOverview() {
        return JsonResult.success(apiDefinitionService.getOverview());
    }

    @AuditLog(title = "查询API列表", businessType = 0)
    @GetMapping("/list")
    public JsonResult getApiList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String apiMethod,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String environment) {
        
        LambdaQueryWrapper<ApiDefinition> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ApiDefinition::getApiName, keyword)
                    .or().like(ApiDefinition::getApiPath, keyword)
                    .or().like(ApiDefinition::getDescription, keyword));
        }
        if (groupId != null) {
            wrapper.eq(ApiDefinition::getGroupId, groupId);
        }
        if (apiMethod != null && !apiMethod.isEmpty()) {
            wrapper.eq(ApiDefinition::getApiMethod, apiMethod);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ApiDefinition::getStatus, status);
        }
        if (environment != null && !environment.isEmpty()) {
            wrapper.eq(ApiDefinition::getEnvironment, environment);
        }
        
        wrapper.eq(ApiDefinition::getDeleted, 0);
        wrapper.orderByDesc(ApiDefinition::getUpdateTime);
        
        Page<ApiDefinition> pageResult = apiDefinitionService.page(new Page<>(page, size), wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return JsonResult.success(result);
    }

    @AuditLog(title = "查询API详情", businessType = 0)
    @GetMapping("/{id}")
    public JsonResult getApiDetail(@PathVariable Long id) {
        ApiDefinitionDTO dto = apiDefinitionService.getApiDetail(id);
        if (dto == null) {
            return JsonResult.error("API不存在");
        }
        return JsonResult.success(dto);
    }

    @AuditLog(title = "创建API", businessType = 0)
    @PostMapping("/create")
    public JsonResult createApi(@RequestBody ApiDefinitionDTO dto) {
        try {
            Long id = apiDefinitionService.createApi(dto);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "创建成功");
            result.put("id", id);
            return JsonResult.success(result);
        } catch (Exception e) {
            return JsonResult.error("创建失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "更新API", businessType = 0)
    @PutMapping("/{id}")
    public JsonResult updateApi(@PathVariable Long id, @RequestBody ApiDefinitionDTO dto) {
        try {
            apiDefinitionService.updateApi(id, dto);
            return JsonResult.success("更新成功");
        } catch (Exception e) {
            return JsonResult.error("更新失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "删除API", businessType = 0)
    @DeleteMapping("/{id}")
    public JsonResult deleteApi(@PathVariable Long id) {
        try {
        apiDefinitionService.deleteApi(id);
            return JsonResult.success("删除成功");
        } catch (Exception e) {
            return JsonResult.error("删除失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "发布API", businessType = 0)
    @PostMapping("/{id}/publish")
    public JsonResult publishApi(@PathVariable Long id, @RequestParam String user) {
        try {
            apiDefinitionService.publishApi(id, user);
            return JsonResult.success("发布成功");
        } catch (Exception e) {
            return JsonResult.error("发布失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "下线API", businessType = 0)
    @PostMapping("/{id}/offline")
    public JsonResult offlineApi(@PathVariable Long id, @RequestParam String user) {
        try {
            apiDefinitionService.offlineApi(id, user);
            return JsonResult.success("下线成功");
        } catch (Exception e) {
            return JsonResult.error("下线失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "测试API", businessType = 0)
    @PostMapping("/{id}/test")
    public JsonResult testApi(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        try {
            Object result = apiDefinitionService.testApi(id, params);
            return JsonResult.success(result);
        } catch (Exception e) {
            return JsonResult.error("测试失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "复制API", businessType = 0)
    @PostMapping("/{id}/copy")
    public JsonResult copyApi(@PathVariable Long id, @RequestParam String newName, @RequestParam String user) {
        try {
            apiDefinitionService.copyApi(id, newName, user);
            return JsonResult.success("复制成功");
        } catch (Exception e) {
            return JsonResult.error("复制失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "查询API版本", businessType = 0)
    @GetMapping("/{id}/versions")
    public JsonResult getApiVersions(@PathVariable Long id) {
        List<ApiVersion> versions = apiVersionService.findByApiId(id);
        return JsonResult.success(versions);
    }

    @AuditLog(title = "回滚API版本", businessType = 0)
    @PostMapping("/{id}/rollback/{versionId}")
    public JsonResult rollbackToVersion(@PathVariable Long id, @PathVariable Long versionId) {
        try {
            apiDefinitionService.rollbackToVersion(id, versionId);
            return JsonResult.success("回滚成功");
        } catch (Exception e) {
            return JsonResult.error("回滚失败: " + e.getMessage());
        }
    }

    @AuditLog(title = "查询API分组", businessType = 0)
    @GetMapping("/groups")
    public JsonResult getGroups() {
        List<ApiGroup> groups = apiGroupService.findAllActive();
        return JsonResult.success(groups);
    }

    @AuditLog(title = "查询API分组树", businessType = 0)
    @GetMapping("/groups/tree")
    public JsonResult getGroupTree() {
        List<ApiGroup> tree = apiGroupService.buildTree();
        return JsonResult.success(tree);
    }

    @AuditLog(title = "创建API分组", businessType = 0)
    @PostMapping("/groups")
    public JsonResult createGroup(@RequestBody ApiGroup group) {
        apiGroupService.save(group);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "创建成功");
        result.put("id", group.getId());
        return JsonResult.success(result);
    }

    @AuditLog(title = "更新API分组", businessType = 0)
    @PutMapping("/groups/{id}")
    public JsonResult updateGroup(@PathVariable Long id, @RequestBody ApiGroup group) {
        group.setId(id);
        apiGroupService.updateById(group);
        return JsonResult.success("更新成功");
    }

    @AuditLog(title = "删除API分组", businessType = 0)
    @DeleteMapping("/groups/{id}")
    public JsonResult deleteGroup(@PathVariable Long id) {
        long apiCount = apiDefinitionService.countByGroupId(id);
        if (apiCount > 0) {
            return JsonResult.error("该分组下有 " + apiCount + " 个API，请先移动或删除这些API");
        }
        long dataTableCount = dataCenterService.countByGroupId(id);
        if (dataTableCount > 0) {
            return JsonResult.error("该分组下仍有 " + dataTableCount + " 张数据中心表，请先在数据中心移动或取消分组");
        }
        apiGroupService.removeById(id);
        return JsonResult.success("删除成功");
    }

    @AuditLog(title = "查询数据源列表", businessType = 0)
    @GetMapping("/datasources")
    public JsonResult getDatasources(@RequestParam(required = false) String environment) {
        List<ApiDatasource> datasources;
        if (environment != null && !environment.isEmpty()) {
            datasources = apiDatasourceService.findByEnvironment(environment);
        } else {
            datasources = apiDatasourceService.list();
        }
        return JsonResult.success(datasources);
    }

    @AuditLog(title = "创建数据源", businessType = 0)
    @PostMapping("/datasources")
    public JsonResult createDatasource(@RequestBody ApiDatasource datasource) {
        apiDatasourceService.save(datasource);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "创建成功");
        result.put("id", datasource.getId());
        return JsonResult.success(result);
    }

    @AuditLog(title = "测试数据源连接", businessType = 0)
    @PostMapping("/datasources/{id}/test")
    public JsonResult testDatasource(@PathVariable Long id) {
        boolean success = apiDatasourceService.testConnection(id);
        return JsonResult.success(success ? "连接成功" : "连接失败");
    }

    @AuditLog(title = "查询数据源表", businessType = 0)
    @GetMapping("/datasources/{id}/tables")
    public JsonResult getDatasourceTables(@PathVariable Long id) {
        List<Map<String, Object>> tables = apiDatasourceService.getTables(id);
        return JsonResult.success(tables);
    }

    @AuditLog(title = "查询数据源表列", businessType = 0)
    @GetMapping("/datasources/{id}/tables/{tableName}/columns")
    public JsonResult getTableColumns(@PathVariable Long id, @PathVariable String tableName) {
        List<Map<String, Object>> columns = apiDatasourceService.getTableColumns(id, tableName);
        return JsonResult.success(columns);
    }

    @AuditLog(title = "API分组统计", businessType = 0)
    @GetMapping("/stats/group")
    public JsonResult getStatsByGroup() {
        return JsonResult.success(apiDefinitionService.getCountByGroup());
    }

    @AuditLog(title = "API方法统计", businessType = 0)
    @GetMapping("/stats/method")
    public JsonResult getStatsByMethod() {
        return JsonResult.success(apiDefinitionService.getCountByMethod());
    }

    @AuditLog(title = "API环境统计", businessType = 0)
    @GetMapping("/stats/environment")
    public JsonResult getStatsByEnvironment() {
        return JsonResult.success(apiDefinitionService.getCountByEnvironment());
    }
}
