package org.example.ecmo.component;

import org.example.ecmo.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class FileStorageContext {

    // 默认存储策略，从配置文件读取，默认为 localFileStorage
    @Value("${file.storage.type:localFileStorage}")
    private String defaultType;

    // Spring 会自动将所有实现了 FileStorageService 的 Bean 注入到这个 Map 中
    // Key 是 Bean 的名字 (比如 @Service("localFileStorage") 里的名字)
    @Autowired
    private Map<String, FileStorageService> storageServiceMap;

    /**
     * 统一上传接口
     */
    public String upload(InputStream is, String path) {
        return getService().upload(is, path);
    }

    /**
     * 根据指定的存储策略执行删除
     */
    public void deleteByType(String type, String path) {
        FileStorageService service = storageServiceMap.get(type);
        if (service != null && org.springframework.util.StringUtils.hasText(path)) {
            service.delete(path);
        }
    }

    /**
     * 获取文件下载流
     */
    public InputStream downloadByType(String type, String path) {
        FileStorageService service = storageServiceMap.get(type);
        if (service != null && org.springframework.util.StringUtils.hasText(path)) {
            return service.download(path);
        }
        return null;
    }

    /**
     * 获取当前使用的存储服务
     */
    public FileStorageService getService() {
        FileStorageService service = storageServiceMap.get(defaultType);
        if (service == null) {
            throw new RuntimeException("找不到指定的存储策略: " + defaultType);
        }
        return service;
    }
}
