package org.example.ecmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ecmo.entity.ApiVersion;
import java.util.List;

public interface ApiVersionService extends IService<ApiVersion> {

    List<ApiVersion> findByApiId(Long apiId);

    ApiVersion findCurrentVersion(Long apiId);

    ApiVersion findByVersionNo(Long apiId, String versionNo);

    ApiVersion createVersion(Long apiId, String changeType, String changeLog, String user);

    void rollbackToVersion(Long apiId, Long versionId);

    void setCurrentVersion(Long apiId, Long versionId);
}
