package org.example.ecmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ecmo.entity.SysFileChunk;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

public interface SysFileChunkService extends IService<SysFileChunk> {
    /**
     * 检查分片是否存在（用于断点续传）
     * @param identifier 文件唯一标识(MD5)
     * @return 已上传的分片序号集合
     */
    Set<Integer> checkChunk(String identifier);

    /**
     * 上传分片
     * @param chunk 分片对象
     * @param file 分片二进制流
     */
    void uploadChunk(SysFileChunk chunk, MultipartFile file) throws IOException;
    /**
     * 合并分片
     * @param identifier 文件唯一标识
     * @param fileName 最终文件名
     * @return 最终访问 URL
     */
    String mergeChunk(String identifier, String fileName) throws IOException;
}
