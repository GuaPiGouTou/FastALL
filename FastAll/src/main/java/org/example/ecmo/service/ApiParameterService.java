package org.example.ecmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ecmo.entity.ApiParameter;
import java.util.List;

public interface ApiParameterService extends IService<ApiParameter> {

    List<ApiParameter> findByApiId(Long apiId);

    List<ApiParameter> findByApiIdAndCategory(Long apiId, String category);

    List<ApiParameter> findByApiIdAndPosition(Long apiId, String position);

    List<ApiParameter> findByVersionId(Long versionId);

    void saveParameters(Long apiId, Long versionId, List<ApiParameter> parameters);

    void deleteByApiId(Long apiId);
}
