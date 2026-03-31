package org.example.ecmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ecmo.entity.SysFileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SysFileInfoService extends IService<SysFileInfo> {
    /**
     * 上传文件
     * @param file 前端传来的文件对象
     * @return 返回文件的最终访问 URL
     */
    String upload(MultipartFile file) throws IOException;

    /**
     * 上传文件并关联业务
     * @param file 前端传来的文件对象
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param fileType 文件类型(image/video/document)
     * @return 返回文件的最终访问 URL
     */
    String upload(MultipartFile file, String bizType, Long bizId, String fileType) throws IOException;

    /**
     * 删除文件
     * @param id 文件ID
     */
    void deleteFile(Long id);

    /**
     * 更新文件元数据
     * @param info 文件信息对象
     */
    void updateMetadata(SysFileInfo info);

    /**
     * 根据业务类型和业务ID获取文件列表
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @return 文件列表
     */
    List<SysFileInfo> getFilesByBiz(String bizType, Long bizId);

    /**
     * 更新文件的业务关联
     * @param fileId 文件ID
     * @param bizType 业务类型
     * @param bizId 业务ID
     */
    void updateFileBiz(Long fileId, String bizType, Long bizId);
}
