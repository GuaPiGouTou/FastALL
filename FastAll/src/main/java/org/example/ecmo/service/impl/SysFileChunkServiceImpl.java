package org.example.ecmo.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ecmo.component.FileStorageContext; // [关键] 引入上下文
import org.example.ecmo.entity.SysFileChunk;
import org.example.ecmo.entity.SysFileInfo;
import org.example.ecmo.mapper.SysFileChunkMapper;
import org.example.ecmo.service.SysFileChunkService;
import org.example.ecmo.service.SysFileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SysFileChunkServiceImpl extends ServiceImpl<SysFileChunkMapper, SysFileChunk> implements SysFileChunkService {
    @Autowired
    private SysFileInfoService fileInfoService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private FileStorageContext storageContext; // [关键] 注入上下文

    @Value("${file.local.path}")
    private String localPath;

    private static final String CHUNK_KEY_PREFIX = "file:chunks:";

    @Override
    public Set<Integer> checkChunk(String identifier) {
        Set<String> members = redisTemplate.opsForSet().members(CHUNK_KEY_PREFIX + identifier);
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        return members.stream().map(Integer::parseInt).collect(Collectors.toSet());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadChunk(SysFileChunk chunk, MultipartFile file) throws IOException {
        String chunkDir = localPath + "chunks" + File.separator + chunk.getIdentifier();
        if (!FileUtil.exist(chunkDir)) {
            FileUtil.mkdir(chunkDir);
        }
        String chunkPath = chunkDir + File.separator + chunk.getChunkNumber() + ".part";
        file.transferTo(new File(chunkPath));

        chunk.setRelativePath(chunkPath);
        this.save(chunk);

        redisTemplate.opsForSet().add(CHUNK_KEY_PREFIX + chunk.getIdentifier(), chunk.getChunkNumber().toString());
        redisTemplate.expire(CHUNK_KEY_PREFIX + chunk.getIdentifier(), 24, TimeUnit.HOURS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String mergeChunk(String identifier, String fileName) throws IOException {
        List<SysFileChunk> chunkList = this.list(new LambdaQueryWrapper<SysFileChunk>()
                .eq(SysFileChunk::getIdentifier, identifier)
                .orderByAsc(SysFileChunk::getChunkNumber));
        if (chunkList.isEmpty()) {
            throw new RuntimeException("分片不存在，无法合并");
        }

        String suffix = StrUtil.subAfter(fileName, ".", true);
        String fullFileName = IdUtil.simpleUUID() + "." + suffix;
        // 临时大文件路径
        String fullPath = localPath + File.separator + "temp_" + fullFileName;
        File fullFile = new File(fullPath);

        // 1. 物理合并分片
        try (FileOutputStream fos = new FileOutputStream(fullFile, true)) {
            for (SysFileChunk chunk : chunkList) {
                File part = new File(chunk.getRelativePath());
                if (part.exists()) {
                    Files.copy(part.toPath(), fos);
                }
            }
        }

        // 2. 将合并后的文件推送到存储引擎 (MinIO 或 Local)
        String finalUrl;
        long fileSize = fullFile.length();
        try (FileInputStream fis = new FileInputStream(fullFile)) {
            finalUrl = storageContext.upload(fis, fullFileName);
        }

        // 3. 清理：删除临时分片目录 和 刚刚合并出的临时大文件
        FileUtil.del(localPath + "chunks" + File.separator + identifier);
        FileUtil.del(fullFile);

        // 4. 删除数据库分片记录与清理 Redis
        this.remove(new LambdaQueryWrapper<SysFileChunk>().eq(SysFileChunk::getIdentifier, identifier));
        redisTemplate.delete(CHUNK_KEY_PREFIX + identifier);

        // 5. 存入 sys_file_info (存入的是 finalUrl)
        SysFileInfo info = new SysFileInfo();
        info.setFileName(fileName);
        info.setFilePath(finalUrl);
        info.setFileMd5(identifier);
        info.setFileSize(fileSize);
        info.setFileExt(suffix);
        info.setUploadUserId(StpUtil.getLoginIdAsLong());
        fileInfoService.save(info);

        return finalUrl;
    }
}
