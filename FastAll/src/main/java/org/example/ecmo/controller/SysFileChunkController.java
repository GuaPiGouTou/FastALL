package org.example.ecmo.controller;

import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.SysFileChunk;
import org.example.ecmo.service.SysFileChunkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/api/file/chunk")
public class SysFileChunkController {

    @Autowired
    private SysFileChunkService chunkService;

    /**
     * 检查分片进度 (断点续传查询)
     * @param identifier 文件全文件MD5
     * @return 返回已收到的分片序号列表
     */
    @AuditLog(title = "文件上传", businessType = 0)
    @GetMapping
    public JsonResult<Set<Integer>> check(@RequestParam("identifier") String identifier) {
        Set<Integer> chunks = chunkService.checkChunk(identifier);
        return new JsonResult<>(200, chunks, "查询成功");
    }

    /**
     * 上传分片块
     */
    @AuditLog(title = "文件上传", businessType = 0)
    @PostMapping
    public JsonResult<String> upload(SysFileChunk chunk, @RequestParam("file") MultipartFile file) throws IOException {
        chunkService.uploadChunk(chunk, file);
        return new JsonResult<>(200, "success", "分片处理成功");
    }
    /**
     * 合并分片
     * @param identifier 文件全文件MD5
     * @param fileName 原始文件名
     * @return 最终文件访问URL
     */
    @AuditLog(title = "文件上传", businessType = 0)
    @PostMapping("/merge")
    public JsonResult<String> merge(@RequestParam("identifier") String identifier,
                                    @RequestParam("fileName") String fileName) throws IOException {
        String url = chunkService.mergeChunk(identifier, fileName);
        return new JsonResult<>(200, url, "文件合并并入库成功");
    }

}
