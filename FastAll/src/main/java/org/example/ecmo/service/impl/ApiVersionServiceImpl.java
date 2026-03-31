package org.example.ecmo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.ecmo.entity.ApiDefinition;
import org.example.ecmo.entity.ApiVersion;
import org.example.ecmo.mapper.ApiDefinitionMapper;
import org.example.ecmo.mapper.ApiVersionMapper;
import org.example.ecmo.service.ApiVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApiVersionServiceImpl extends ServiceImpl<ApiVersionMapper, ApiVersion> implements ApiVersionService {

    @Autowired
    private ApiVersionMapper apiVersionMapper;

    @Autowired
    private ApiDefinitionMapper apiDefinitionMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<ApiVersion> findByApiId(Long apiId) {
        return apiVersionMapper.findByApiId(apiId);
    }

    @Override
    public ApiVersion findCurrentVersion(Long apiId) {
        return apiVersionMapper.findCurrentVersion(apiId);
    }

    @Override
    public ApiVersion findByVersionNo(Long apiId, String versionNo) {
        return apiVersionMapper.findByVersionNo(apiId, versionNo);
    }

    @Override
    @Transactional
    public ApiVersion createVersion(Long apiId, String changeType, String changeLog, String user) {
        ApiDefinition api = apiDefinitionMapper.selectById(apiId);
        if (api == null) {
            throw new RuntimeException("API不存在");
        }

        int versionCount = apiVersionMapper.countByApiId(apiId);
        String versionNo = "v" + (versionCount + 1);

        ApiVersion version = new ApiVersion();
        version.setApiId(apiId);
        version.setVersionNo(versionNo);
        version.setVersionName("版本 " + versionNo);
        version.setVersionDesc(changeLog);
        version.setChangeType(changeType);
        version.setChangeLog(changeLog);
        version.setStatus("active");
        version.setIsCurrent(1);
        version.setCreateUser(user);
        version.setCreateTime(LocalDateTime.now());

        try {
            version.setSnapshot(objectMapper.writeValueAsString(api));
        } catch (Exception e) {
            version.setSnapshot("{}");
        }

        apiVersionMapper.clearCurrentVersion(apiId);
        save(version);

        return version;
    }

    @Override
    @Transactional
    public void rollbackToVersion(Long apiId, Long versionId) {
        ApiVersion version = getById(versionId);
        if (version == null || !version.getApiId().equals(apiId)) {
            throw new RuntimeException("版本不存在");
        }

        try {
            ApiDefinition api = objectMapper.readValue(version.getSnapshot().toString(), ApiDefinition.class);
            api.setId(apiId);
            api.setUpdateTime(LocalDateTime.now());
            apiDefinitionMapper.updateById(api);

            setCurrentVersion(apiId, versionId);
        } catch (Exception e) {
            throw new RuntimeException("回滚失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void setCurrentVersion(Long apiId, Long versionId) {
        apiVersionMapper.clearCurrentVersion(apiId);
        
        ApiVersion version = getById(versionId);
        if (version != null) {
            version.setIsCurrent(1);
            updateById(version);
        }
    }
}
