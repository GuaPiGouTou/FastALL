package org.example.ecmo.service;

import java.io.InputStream;

public interface FileStorageService {
    /**
     * 上传文件
     * @param is 输入流
     * @param path 相对路径 (比如 /2026/03/test.pdf)
     * @return 访问地址
     */
    String upload(InputStream is, String path);

    /**
     * 获取预览/下载地址
     */
    String getUrl(String path);

    /**
     * 删除文件
     */
    void delete(String path);

    /**
     * 获取文件下载流
     */
    InputStream download(String path);
}
