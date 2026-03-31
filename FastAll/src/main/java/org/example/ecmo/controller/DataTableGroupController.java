package org.example.ecmo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.DataTableGroup;
import org.example.ecmo.service.DataTableGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data-center/groups")
public class DataTableGroupController {

    @Autowired
    private DataTableGroupService dataTableGroupService;

    @AuditLog(title = "查询数据表分组列表", businessType = 0)
    @GetMapping("/list")
    public JsonResult getGroupList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        
        LambdaQueryWrapper<DataTableGroup> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(DataTableGroup::getGroupName, keyword)
                   .or().like(DataTableGroup::getGroupCode, keyword);
        }
        
        wrapper.orderByAsc(DataTableGroup::getSortOrder)
               .orderByDesc(DataTableGroup::getCreateTime);
        
        Page<DataTableGroup> pageResult = dataTableGroupService.page(new Page<>(page, size), wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return JsonResult.success(result);
    }

    @AuditLog(title = "查询所有数据表分组", businessType = 0)
    @GetMapping("/all")
    public JsonResult getAllGroups() {
        List<DataTableGroup> groups = dataTableGroupService.findAllActive();
        return JsonResult.success(groups);
    }

    @AuditLog(title = "查询数据表分组树", businessType = 0)
    @GetMapping("/tree")
    public JsonResult getGroupTree() {
        List<Map<String, Object>> tree = dataTableGroupService.getGroupTree();
        return JsonResult.success(tree);
    }

    @AuditLog(title = "查询数据表分组详情", businessType = 0)
    @GetMapping("/{id}")
    public JsonResult getGroupDetail(@PathVariable Long id) {
        DataTableGroup group = dataTableGroupService.getById(id);
        if (group == null) {
            return JsonResult.error("分组不存在");
        }
        return JsonResult.success(group);
    }

    @AuditLog(title = "创建数据表分组", businessType = 0)
    @PostMapping
    public JsonResult createGroup(@RequestBody DataTableGroup group) {
        if (group.getGroupName() == null || group.getGroupName().trim().isEmpty()) {
            return JsonResult.error("分组名称不能为空");
        }
        
        if (group.getGroupCode() != null && !group.getGroupCode().isEmpty()) {
            DataTableGroup existing = dataTableGroupService.findByCode(group.getGroupCode());
            if (existing != null) {
                return JsonResult.error("分组编码已存在");
            }
        }
        
        group.setStatus(1);
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        
        boolean result = dataTableGroupService.save(group);
        return result ? JsonResult.success("创建成功") : JsonResult.error("创建失败");
    }

    @AuditLog(title = "更新数据表分组", businessType = 0)
    @PutMapping("/{id}")
    public JsonResult updateGroup(@PathVariable Long id, @RequestBody DataTableGroup group) {
        group.setId(id);
        group.setUpdateTime(LocalDateTime.now());
        
        boolean result = dataTableGroupService.updateById(group);
        return result ? JsonResult.success("更新成功") : JsonResult.error("更新失败");
    }

    @AuditLog(title = "删除数据表分组", businessType = 0)
    @DeleteMapping("/{id}")
    public JsonResult deleteGroup(@PathVariable Long id) {
        boolean result = dataTableGroupService.removeById(id);
        return result ? JsonResult.success("删除成功") : JsonResult.error("删除失败");
    }
}
