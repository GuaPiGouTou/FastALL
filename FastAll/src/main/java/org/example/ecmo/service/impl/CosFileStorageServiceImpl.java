package org.example.ecmo.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.example.ecmo.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service("cos")
public class CosFileStorageServiceImpl implements FileStorageService {

    private COSClient cosClient;

    @Value("${TENCENT_CLOUD_SECRET_ID}")
    private String secretId;

    @Value("${TENCENT_CLOUD_SECRET_KEY}")
    private String secretKey;

    @Value("${file.cos.bucket}")
    private String bucket;

    @Value("${file.cos.region}")
    private String region;

    @Value("${file.cos.baseUrl}")
    private String baseUrl;

    private COSClient getCosClient() {
        if (cosClient == null) {
            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            ClientConfig clientConfig = new ClientConfig(new Region(region));
            cosClient = new COSClient(cred, clientConfig);
        }
        return cosClient;
    }

    @Override
    public String upload(InputStream is, String path) {
        try {
            byte[] bytes = readAllBytes(is);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(bytes.length);
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, path, bais, objectMetadata);
            PutObjectResult putObjectResult = getCosClient().putObject(putObjectRequest);
            
            return getUrl(path);
        } catch (Exception e) {
            throw new RuntimeException("COS 上传失败: " + e.getMessage(), e);
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
        return baseUrl + "/" + path;
    }

    @Override
    public void delete(String path) {
        try {
            getCosClient().deleteObject(bucket, path);
        } catch (Exception e) {
            throw new RuntimeException("COS 删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String path) {
        try {
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, path);
            return getCosClient().getObject(getObjectRequest).getObjectContent();
        } catch (Exception e) {
            throw new RuntimeException("COS 下载流获取失败: " + e.getMessage(), e);
        }
    }
}