package org.example.ecmo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.ApiGroup;
import org.example.ecmo.service.ApiDefinitionService;
import org.example.ecmo.service.ApiGroupService;
import org.example.ecmo.service.DataCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据中心「数据表分组」接口，与 API 管理中的 {@code api_group} 共用同一套分组。
 */
@RestController
@RequestMapping("/api/data-center/groups")
public class DataTableGroupController {

    @Autowired
    private ApiGroupService apiGroupService;

    @Autowired
    private ApiDefinitionService apiDefinitionService;

    @Autowired
    private DataCenterService dataCenterService;

    @AuditLog(title = "查询数据表分组列表", businessType = 0)
    @GetMapping("/list")
    public JsonResult getGroupList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<ApiGroup> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ApiGroup::getGroupName, keyword)
                    .or().like(ApiGroup::getGroupCode, keyword));
        }

        wrapper.orderByAsc(ApiGroup::getSortOrder)
                .orderByDesc(ApiGroup::getCreateTime);

        Page<ApiGroup> pageResult = apiGroupService.page(new Page<>(page, size), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return JsonResult.success(result);
    }

    @AuditLog(title = "查询所有数据表分组", businessType = 0)
    @GetMapping("/all")
    public JsonResult getAllGroups() {
        List<ApiGroup> groups = apiGroupService.findAllActive();
        return JsonResult.success(groups);
    }

    @AuditLog(title = "查询数据表分组树", businessType = 0)
    @GetMapping("/tree")
    public JsonResult getGroupTree() {
        List<Map<String, Object>> tree = apiGroupService.getGroupTreeNested();
        return JsonResult.success(tree);
    }

    @AuditLog(title = "查询数据表分组详情", businessType = 0)
    @GetMapping("/{id}")
    public JsonResult getGroupDetail(@PathVariable Long id) {
        ApiGroup group = apiGroupService.getById(id);
        if (group == null) {
            return JsonResult.error("分组不存在");
        }
        return JsonResult.success(group);
    }

    @AuditLog(title = "创建数据表分组", businessType = 0)
    @PostMapping
    public JsonResult createGroup(@RequestBody ApiGroup group) {
        if (group.getGroupName() == null || group.getGroupName().trim().isEmpty()) {
            return JsonResult.error("分组名称不能为空");
        }

        if (group.getGroupCode() != null && !group.getGroupCode().isEmpty()) {
            ApiGroup existing = apiGroupService.findByCode(group.getGroupCode());
            if (existing != null) {
                return JsonResult.error("分组编码已存在");
            }
        }

        group.setStatus(1);
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());

        boolean result = apiGroupService.save(group);
        return result ? JsonResult.success("创建成功") : JsonResult.error("创建失败");
    }

    @AuditLog(title = "更新数据表分组", businessType = 0)
    @PutMapping("/{id}")
    public JsonResult updateGroup(@PathVariable Long id, @RequestBody ApiGroup group) {
        group.setId(id);
        group.setUpdateTime(LocalDateTime.now());

        boolean result = apiGroupService.updateById(group);
        return result ? JsonResult.success("更新成功") : JsonResult.error("更新失败");
    }

    @AuditLog(title = "删除数据表分组", businessType = 0)
    @DeleteMapping("/{id}")
    public JsonResult deleteGroup(@PathVariable Long id) {
        long apiCount = apiDefinitionService.countByGroupId(id);
        if (apiCount > 0) {
            return JsonResult.error("该分组下仍有 " + apiCount + " 个 API，请先移动或删除");
        }
        long tableCount = dataCenterService.countByGroupId(id);
        if (tableCount > 0) {
            return JsonResult.error("该分组下仍有 " + tableCount + " 张数据表，请先移动或取消分组");
        }
        boolean result = apiGroupService.removeById(id);
        return result ? JsonResult.success("删除成功") : JsonResult.error("删除失败");
    }
}
