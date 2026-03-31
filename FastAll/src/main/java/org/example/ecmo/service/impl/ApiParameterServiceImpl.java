package org.example.ecmo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ecmo.entity.ApiParameter;
import org.example.ecmo.mapper.ApiParameterMapper;
import org.example.ecmo.service.ApiParameterService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ApiParameterServiceImpl extends ServiceImpl<ApiParameterMapper, ApiParameter> implements ApiParameterService {

    @Override
    public List<ApiParameter> findByApiId(Long apiId) {
        return baseMapper.findByApiId(apiId);
    }

    @Override
    public List<ApiParameter> findByApiIdAndCategory(Long apiId, String category) {
        return baseMapper.findByApiIdAndCategory(apiId, category);
    }

    @Override
    public List<ApiParameter> findByApiIdAndPosition(Long apiId, String position) {
        return baseMapper.findByApiIdAndPosition(apiId, position);
    }

    @Override
    public List<ApiParameter> findByVersionId(Long versionId) {
        return baseMapper.findByVersionId(versionId);
    }

    @Override
    public void saveParameters(Long apiId, Long versionId, List<ApiParameter> parameters) {
        for (ApiParameter param : parameters) {
            param.setApiId(apiId);
            param.setApiVersionId(versionId);
            save(param);
        }
    }

    @Override
    public void deleteByApiId(Long apiId) {
        remove(new LambdaQueryWrapper<ApiParameter>().eq(ApiParameter::getApiId, apiId));
    }
}
