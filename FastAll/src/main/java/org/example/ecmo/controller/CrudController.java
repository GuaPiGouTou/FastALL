package org.example.ecmo.controller;

import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crud")
public class CrudController {

    @Autowired
    private CrudService crudService;

    /**
     * 获取表结构定义
     * @param tableName 表名
     * @return 字段配置列表
     */
    @AuditLog(title = "查询表结构", businessType = 0)
    @GetMapping("/{tableName}/schema")
    public JsonResult getTableSchema(@PathVariable String tableName) {
        List<Map<String, Object>> schema = crudService.getTableSchema(tableName);
        return JsonResult.success(schema);
    }

    /**
     * 获取数据列表
     * @param tableName 表名
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param sortField 排序字段
     * @param sortOrder 排序顺序
     * @return 数据列表和总数
     */
    @AuditLog(title = "查询数据列表", businessType = 0)
    @GetMapping("/{tableName}/list")
    public JsonResult getTableData(@PathVariable String tableName,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String sortField,
                                   @RequestParam(required = false) String sortOrder) {
        Map<String, Object> data = crudService.getTableData(tableName, page, size, keyword, sortField, sortOrder);
        return JsonResult.success(data);
    }

    /**
     * 保存数据
     * @param tableName 表名
     * @param data 数据
     * @return 保存结果
     */
    @AuditLog(title = "保存数据", businessType = 0)
    @PostMapping("/{tableName}/save")
    public JsonResult saveData(@PathVariable String tableName, @RequestBody Map<String, Object> data) {
        boolean result = crudService.saveData(tableName, data);
        return result ? JsonResult.success("保存成功") : JsonResult.error("保存失败");
    }

    /**
     * 删除数据
     * @param tableName 表名
     * @param id 主键
     * @return 删除结果
     */
    @AuditLog(title = "删除数据", businessType = 0)
    @DeleteMapping("/{tableName}/{id}")
    public JsonResult deleteData(@PathVariable String tableName, @PathVariable Long id) {
        boolean result = crudService.deleteData(tableName, id);
        return result ? JsonResult.success("删除成功") : JsonResult.error("删除失败");
    }

    /**
     * 新增字段
     * @param tableName 表名
     * @param fieldConfig 字段配置
     * @return 操作结果
     */
    @AuditLog(title = "新增字段", businessType = 0)
    @PostMapping("/{tableName}/column")
    public JsonResult addField(@PathVariable String tableName, @RequestBody Map<String, Object> fieldConfig) {
        String prop = (String) fieldConfig.get("prop");
        String label = (String) fieldConfig.get("label");
        String uiType = (String) fieldConfig.get("uiType");
        String options = (String) fieldConfig.get("options");
        Integer isShowInList = (Integer) fieldConfig.get("isShowInList");
        Integer sortOrder = (Integer) fieldConfig.get("sortOrder");

        boolean result = crudService.addFieldConfig(tableName, prop, label, uiType, options, isShowInList, sortOrder);
        return result ? JsonResult.success("新增字段成功") : JsonResult.error("新增字段失败");
    }

    /**
     * 修改字段配置
     * @param tableName 表名
     * @param fieldConfig 字段配置
     * @return 操作结果
     */
    @AuditLog(title = "修改字段配置", businessType = 0)
    @PutMapping("/{tableName}/column")
    public JsonResult updateField(@PathVariable String tableName, @RequestBody Map<String, Object> fieldConfig) {
        String prop = (String) fieldConfig.get("prop");
        String label = (String) fieldConfig.get("label");
        String uiType = (String) fieldConfig.get("uiType");
        String options = (String) fieldConfig.get("options");
        Integer isShowInList = (Integer) fieldConfig.get("isShowInList");
        Integer sortOrder = (Integer) fieldConfig.get("sortOrder");

        boolean result = crudService.updateFieldConfig(tableName, prop, label, uiType, options, isShowInList, sortOrder);
        return result ? JsonResult.success("修改字段配置成功") : JsonResult.error("修改字段配置失败");
    }

    /**
     * 导出数据到 Excel
     * @param tableName 表名
     * @param params 导出参数
     * @return 导出结果
     */
    @AuditLog(title = "导出数据", businessType = 0)
    @PostMapping("/{tableName}/export")
    public JsonResult exportData(@PathVariable String tableName, @RequestBody Map<String, Object> params) {
        // 这里可以实现 Excel 导出逻辑
        // 暂时返回成功
        return JsonResult.success("导出成功");
    }
}