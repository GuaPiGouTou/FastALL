package org.example.ecmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ecmo.entity.ApiDefinition;
import org.example.ecmo.dto.ApiDefinitionDTO;
import java.util.List;
import java.util.Map;

public interface ApiDefinitionService extends IService<ApiDefinition> {

    ApiDefinition findByCode(String apiCode);

    ApiDefinition findPublishedByPathAndMethod(String apiPath, String apiMethod);

    Map<String, Object> getOverview();

    List<Map<String, Object>> getCountByGroup();

    List<Map<String, Object>> getCountByMethod();

    List<Map<String, Object>> getCountByEnvironment();

    Long createApi(ApiDefinitionDTO dto);

    void updateApi(Long id, ApiDefinitionDTO dto);

    void deleteApi(Long id);

    void publishApi(Long id, String user);

    void offlineApi(Long id, String user);

    void copyApi(Long id, String newName, String user);

    ApiDefinitionDTO getApiDetail(Long id);

    List<ApiDefinition> findByGroupId(Long groupId);

    long countByGroupId(Long groupId);

    List<ApiDefinition> findByTag(String tag);

    void incrementCallCount(Long id);

    void incrementErrorCount(Long id);

    void updateStatistics(Long id, Integer executeTime);

    String generateApiCode();

    void validateApi(ApiDefinition api);

    void testConnection(Long datasourceId);

    void rollbackToVersion(Long apiId, Long versionId);

    Object testApi(Long id, Map<String, Object> params);
}
