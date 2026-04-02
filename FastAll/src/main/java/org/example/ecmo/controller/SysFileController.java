package org.example.ecmo.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.component.FileStorageContext;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.SysFileInfo;
import org.example.ecmo.service.SysFileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class SysFileController {

    @Autowired
    private SysFileInfoService sysFileInfoService;

    @Autowired
    private FileStorageContext storageContext;

    @AuditLog(title = "上传文件", businessType = 0)
    @PostMapping("/upload")
    public JsonResult<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", required = false) String bizType,
            @RequestParam(value = "bizId", required = false) Long bizId,
            @RequestParam(value = "fileType", required = false) String fileType) throws IOException {
        
        System.out.println("=== File Upload Debug ===");
        System.out.println("file: " + file);
        if (file != null) {
            System.out.println("file.isEmpty(): " + file.isEmpty());
            System.out.println("file.getSize(): " + file.getSize());
            System.out.println("file.getOriginalFilename(): " + file.getOriginalFilename());
            System.out.println("file.getContentType(): " + file.getContentType());
            System.out.println("file.getBytes().length: " + file.getBytes().length);
        } else {
            System.out.println("file is null!");
        }
        System.out.println("bizType: " + bizType);
        System.out.println("bizId: " + bizId);
        System.out.println("fileType: " + fileType);
        System.out.println("========================");
        
        if (file == null || file.isEmpty()) {
            return JsonResult.error("文件不能为空");
        }

        String url = sysFileInfoService.upload(file, bizType, bizId, fileType);

        return JsonResult.success(url, "上传成功");
    }

    @AuditLog(title = "获取业务文件列表", businessType = 0)
    @GetMapping("/biz-files")
    public JsonResult<List<SysFileInfo>> getBizFiles(
            @RequestParam String bizType,
            @RequestParam Long bizId) {
        List<SysFileInfo> files = sysFileInfoService.getFilesByBiz(bizType, bizId);
        return JsonResult.success(files, "获取成功");
    }

    @AuditLog(title = "文件列表", businessType = 0)
    @GetMapping("/list")
    public JsonResult<IPage<SysFileInfo>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String fileExt,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        
        System.out.println("Query params - fileName: " + fileName + ", beginTime: " + beginTime + ", endTime: " + endTime);
        
        Page<SysFileInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysFileInfo> wrapper = new LambdaQueryWrapper<>();
        
        // 增加模糊查询和文件后缀过滤
        wrapper.like(StrUtil.isNotBlank(fileName), SysFileInfo::getFileName, fileName);
        wrapper.eq(StrUtil.isNotBlank(fileExt), SysFileInfo::getFileExt, fileExt);
        
        // 增加日期范围过滤
        wrapper.ge(beginTime != null, SysFileInfo::getCreatedAt, beginTime);
        wrapper.le(endTime != null, SysFileInfo::getCreatedAt, endTime);
        
        wrapper.orderByDesc(SysFileInfo::getCreatedAt);
        return JsonResult.success(sysFileInfoService.page(page, wrapper), "查询成功");
    }

    /**
     * 删除文件同步物理删除
     */
    @AuditLog(title = "删除文件", businessType = 0)
    @DeleteMapping("/delete/{id}")
    public JsonResult<String> delete(@PathVariable Long id) {
        sysFileInfoService.deleteFile(id);
        return JsonResult.success("删除成功");
    }

    /**
     * 更新文件元数据
     */
    @AuditLog(title = "更新文件元数据", businessType = 0)
    @PutMapping("/update")
    public JsonResult<String> update(@RequestBody SysFileInfo info) {
        sysFileInfoService.updateMetadata(info);
        return JsonResult.success("更新成功");
    }

    /**
     * 安全下载接口
     */
    @AuditLog(title = "安全下载", businessType = 0)
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) throws IOException {
        SysFileInfo info = sysFileInfoService.getById(id);
        if (info == null) {
            response.sendError(404, "文件不存在");
            return;
        }

        // 识别存储类型
        String storageType = info.getFilePath().startsWith("/files/") ? "localFileStorage" : "minio";
        String path = StrUtil.subAfter(info.getFilePath(), storageType.equals("localFileStorage") ? "/files/" : "/", true);

        try (InputStream is = storageContext.downloadByType(storageType, path);
             OutputStream os = response.getOutputStream()) {
            
            if (is == null) {
                response.sendError(500, "无法获取存储流");
                return;
            }

            // 设置响应头，强制下载并保留文件名
            response.setContentType("application/octet-stream");
            String fileName = java.net.URLEncoder.encode(info.getFileName(), "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            response.sendError(500, "文件流读取异常: " + e.getMessage());
        }
    }
}
