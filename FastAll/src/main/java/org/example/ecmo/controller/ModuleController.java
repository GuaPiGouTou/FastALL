package org.example.ecmo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.Module;
import org.example.ecmo.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crud")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @AuditLog(title = "查询模块列表", businessType = 0)
    @GetMapping("/modules")
    public JsonResult<List<Module>> getModules() {
        List<Module> modules = moduleService.list();
        return JsonResult.success(modules);
    }

    @AuditLog(title = "删除模块", businessType = 0)
    @DeleteMapping("/module/{tableName}")
    public JsonResult<String> deleteModule(@PathVariable String tableName) {
        boolean removed = moduleService.remove(new LambdaQueryWrapper<Module>().eq(Module::getTableName, tableName));
        return removed ? JsonResult.success("删除成功") : JsonResult.error("删除失败");
    }

    @AuditLog(title = "添加模块", businessType = 0)
    @PostMapping("/module")
    public JsonResult<String> addModule(@RequestBody Module module) {
        boolean saved = moduleService.save(module);
        return saved ? JsonResult.success("添加成功") : JsonResult.error("添加失败");
    }

    @AuditLog(title = "更新模块", businessType = 0)
    @PutMapping("/module")
    public JsonResult<String> updateModule(@RequestBody Module module) {
        boolean updated = moduleService.updateById(module);
        return updated ? JsonResult.success("更新成功") : JsonResult.error("更新失败");
    }
}
