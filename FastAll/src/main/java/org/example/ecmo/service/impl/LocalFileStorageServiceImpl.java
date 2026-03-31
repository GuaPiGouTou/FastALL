package org.example.ecmo.service.impl;

import cn.hutool.core.io.FileUtil;
import org.example.ecmo.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.File;

@Service("localFileStorage")
public class LocalFileStorageServiceImpl implements FileStorageService {

    // 存储根路径，可以通过 application.yml 配置，默认为当前目录下的 uploads 文件夹
    @Value("${file.local.path:./uploads/}")
    private String localPath;

    // 访问前缀，比如 http://localhost:8080/files/
    @Value("${file.local.prefix:/files/}")
    private String prefix;

    @Override
    public String upload(InputStream is, String path) {
        // 使用 Hutool 的 FileUtil.normalize 处理路径，确保斜杠正确且包含目录
        String fullPath = FileUtil.normalize(localPath + File.separator + path);
        // 执行写入
        FileUtil.writeFromStream(is, fullPath);
        return getUrl(path);
    }

    @Override
    public String getUrl(String path) {
        return prefix + path;
    }

    @Override
    public void delete(String path) {
        FileUtil.del(localPath + path);
    }

    @Override
    public InputStream download(String path) {
        return FileUtil.getInputStream(localPath + path);
    }
}
