package org.example.ecmo.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ecmo.component.FileStorageContext;
import org.example.ecmo.entity.SysFileInfo;
import org.example.ecmo.exception.FileStorageException;
import org.example.ecmo.exception.ValidationException;
import org.example.ecmo.mapper.SysFileInfoMapper;
import org.example.ecmo.service.SysFileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class SysFileInfoServiceImpl extends ServiceImpl<SysFileInfoMapper, SysFileInfo> implements SysFileInfoService {

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "json", "xml",
            "jpg", "jpeg", "png", "gif", "bmp", "svg",
            "zip", "rar", "7z", "tar", "gz"
    );

    @Autowired
    private FileStorageContext storageContext;

    @Value("${file.local.path}")
    private String localPath;

    @Value("${file.local.prefix}")
    private String filePrefix;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String upload(MultipartFile file) throws IOException {
        return upload(file, null, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String upload(MultipartFile file, String bizType, Long bizId, String fileType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("文件不能为空");
        }

        validateFile(file);

        String url;
        String originalName = file.getOriginalFilename();
        String suffix = StrUtil.subAfter(originalName, ".", true).toLowerCase();

        String md5 = calculateMD5(file.getBytes());

        SysFileInfo exist = this.getOne(new LambdaQueryWrapper<SysFileInfo>()
                .eq(SysFileInfo::getFileMd5, md5)
                .last("LIMIT 1"));
        
        if (exist != null) {
            String physicalName = StrUtil.subAfter(exist.getFilePath(), filePrefix, true);
            String fullPhysicsPath = FileUtil.normalize(localPath + java.io.File.separator + physicalName);
            if (FileUtil.exist(fullPhysicsPath)) {
                url = exist.getFilePath();
                if (bizType != null && bizId != null) {
                    updateFileBiz(exist.getId(), bizType, bizId);
                }
                return url;
            }
            exist = null;
        }

        String fileName = IdUtil.simpleUUID() + "." + suffix;
        try {
            url = storageContext.upload(file.getInputStream(), fileName);
        } catch (Exception e) {
            throw new FileStorageException("文件上传失败: " + e.getMessage(), e);
        }

        saveFileInfo(originalName, url, md5, file.getSize(), suffix, bizType, bizId, fileType);
        return url;
    }
    
    private String calculateMD5(byte[] data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5计算失败", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("文件大小超过限制，最大允许100MB");
        }

        String originalName = file.getOriginalFilename();
        if (StrUtil.isBlank(originalName)) {
            throw new ValidationException("文件名不能为空");
        }

        String suffix = StrUtil.subAfter(originalName, ".", true).toLowerCase();
        if (StrUtil.isBlank(suffix)) {
            throw new ValidationException("文件缺少扩展名");
        }

        if (!ALLOWED_EXTENSIONS.contains(suffix)) {
            throw new ValidationException("不支持的文件类型: " + suffix);
        }
    }

    private void saveFileInfo(String fileName, String filePath, String md5, Long fileSize, String suffix, String bizType, Long bizId, String fileType) {
        SysFileInfo info = new SysFileInfo();
        info.setFileName(fileName);
        info.setFilePath(filePath);
        info.setFileMd5(md5);
        info.setFileSize(fileSize);
        info.setFileExt(suffix);
        info.setUploadUserId(StpUtil.getLoginIdAsLong());
        info.setBizType(bizType);
        info.setBizId(bizId);
        info.setFileType(fileType);
        this.save(info);
    }

    @Override
    public List<SysFileInfo> getFilesByBiz(String bizType, Long bizId) {
        return this.list(new LambdaQueryWrapper<SysFileInfo>()
                .eq(SysFileInfo::getBizType, bizType)
                .eq(SysFileInfo::getBizId, bizId)
                .orderByAsc(SysFileInfo::getSortOrder)
                .orderByAsc(SysFileInfo::getCreateTime));
    }

    @Override
    public void updateFileBiz(Long fileId, String bizType, Long bizId) {
        SysFileInfo updateInfo = new SysFileInfo();
        updateInfo.setId(fileId);
        updateInfo.setBizType(bizType);
        updateInfo.setBizId(bizId);
        this.updateById(updateInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long id) {
        SysFileInfo info = this.getById(id);
        if (info == null) {
            throw new ValidationException("文件记录不存在");
        }

        String url = info.getFilePath();
        String path;
        String storageType;

        if (url.startsWith(filePrefix)) {
            path = StrUtil.subAfter(url, filePrefix, true);
            storageType = "localFileStorage";
        } else {
            path = StrUtil.subAfter(url, "/", true);
            storageType = "minio";
        }

        if (StrUtil.isNotBlank(path)) {
            try {
                storageContext.deleteByType(storageType, path);
            } catch (Exception e) {
                throw new FileStorageException("文件删除失败: " + e.getMessage(), e);
            }
        }

        this.removeById(id);
    }

    @Override
    public void updateMetadata(SysFileInfo info) {
        if (info.getId() == null) {
            throw new ValidationException("ID不能为空");
        }
        SysFileInfo updateNode = new SysFileInfo();
        updateNode.setId(info.getId());
        updateNode.setFileName(info.getFileName());
        this.updateById(updateNode);
    }
}
