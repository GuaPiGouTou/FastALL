package org.example.ecmo.service.impl;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.example.ecmo.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service("minio")
public class MinioFileStorageServiceImpl implements FileStorageService {

    @Autowired
    private MinioClient minioClient;

    @Value("${file.minio.bucket}")
    private String bucket;

    @Value("${file.minio.endpoint}")
    private String endpoint;

    @Override
    public String upload(InputStream is, String path) {
        try {
            byte[] bytes = readAllBytes(is);
            long size = bytes.length;
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .stream(bais, size, -1)
                            .build()
            );
            return getUrl(path);
        } catch (Exception e) {
            throw new RuntimeException("MinIO 上传失败: " + e.getMessage(), e);
        }
    }
    
    private byte[] readAllBytes(InputStream is) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    @Override
    public String getUrl(String path) {
        return endpoint + "/" + bucket + "/" + path;
    }

    @Override
    public void delete(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("MinIO 删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String path) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("MinIO 下载流获取失败: " + e.getMessage(), e);
        }
    }
}
