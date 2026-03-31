package org.example.ecmo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.SysExperimentTask;
import org.example.ecmo.service.SysExperimentTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 实验任务管理控制层
 */
@RestController
@RequestMapping("/api/experimentTask")
public class SysExperimentTaskController {

    @Autowired
    private SysExperimentTaskService sysExperimentTaskService;

    @AuditLog(title = "查询实验任务列表", businessType = 1)
    @GetMapping("/list")
    public JsonResult<IPage<SysExperimentTask>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String taskName) {
        Page<SysExperimentTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysExperimentTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(taskName), SysExperimentTask::getTaskName, taskName);
        wrapper.orderByDesc(SysExperimentTask::getCreateTime);
        return new JsonResult<>(200, sysExperimentTaskService.page(page, wrapper), "查询成功");
    }

    @AuditLog(title = "新增实验任务", businessType = 2)
    @PostMapping("/add")
    public JsonResult<Boolean> add(@RequestBody SysExperimentTask task) {
        return new JsonResult<>(200, sysExperimentTaskService.save(task), "新增成功");
    }

    @AuditLog(title = "修改实验任务", businessType = 2)
    @PutMapping("/update")
    public JsonResult<Boolean> update(@RequestBody SysExperimentTask task) {
        return new JsonResult<>(200, sysExperimentTaskService.updateById(task), "修改成功");
    }

    @AuditLog(title = "删除实验任务", businessType = 3)
    @DeleteMapping("/delete/{id}")
    public JsonResult<Boolean> delete(@PathVariable Long id) {
        return new JsonResult<>(200, sysExperimentTaskService.removeById(id), "删除成功");
    }
}
