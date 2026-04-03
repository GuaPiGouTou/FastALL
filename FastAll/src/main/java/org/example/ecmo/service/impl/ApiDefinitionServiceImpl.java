package org.example.ecmo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ecmo.dto.ApiDefinitionDTO;
import org.example.ecmo.entity.ApiDefinition;
import org.example.ecmo.entity.ApiParameter;
import org.example.ecmo.entity.ApiVersion;
import org.example.ecmo.entity.ApiGroup;
import org.example.ecmo.mapper.ApiDefinitionMapper;
import org.example.ecmo.mapper.ApiParameterMapper;
import org.example.ecmo.mapper.ApiVersionMapper;
import org.example.ecmo.service.ApiDefinitionService;
import org.example.ecmo.service.ApiParameterService;
import org.example.ecmo.service.ApiVersionService;
import org.example.ecmo.service.ApiGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ApiDefinitionServiceImpl extends ServiceImpl<ApiDefinitionMapper, ApiDefinition> implements ApiDefinitionService {

    @Autowired
    private ApiDefinitionMapper apiDefinitionMapper;

    @Autowired
    private ApiParameterMapper apiParameterMapper;

    @Autowired
    private ApiVersionMapper apiVersionMapper;

    @Autowired
    private ApiParameterService apiParameterService;

    @Autowired
    private ApiVersionService apiVersionService;

    @Autowired
    private ApiGroupService apiGroupService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public ApiDefinition findByCode(String apiCode) {
        return apiDefinitionMapper.findByCode(apiCode);
    }

    @Override
    public ApiDefinition findPublishedByPathAndMethod(String apiPath, String apiMethod) {
        return apiDefinitionMapper.findPublishedByPathAndMethod(apiPath, apiMethod);
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalApis", apiDefinitionMapper.getTotalCount());
        overview.put("publishedApis", apiDefinitionMapper.getPublishedCount());
        overview.put("draftApis", apiDefinitionMapper.getDraftCount());
        overview.put("todayCalls", 0);
        overview.put("weeklyCalls", 0);
        return overview;
    }

    @Override
    public List<Map<String, Object>> getCountByGroup() {
        return apiDefinitionMapper.getCountByGroup();
    }

    @Override
    public List<Map<String, Object>> getCountByMethod() {
        return apiDefinitionMapper.getCountByMethod();
    }

    @Override
    public List<Map<String, Object>> getCountByEnvironment() {
        return apiDefinitionMapper.getCountByEnvironment();
    }

    @Override
    public List<ApiDefinition> findByGroupId(Long groupId) {
        return apiDefinitionMapper.findByGroupId(groupId);
    }

    @Override
    public long countByGroupId(Long groupId) {
        LambdaQueryWrapper<ApiDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiDefinition::getGroupId, groupId);
        wrapper.eq(ApiDefinition::getDeleted, 0);
        return count(wrapper);
    }

    @Override
    public List<ApiDefinition> findByTag(String tag) {
        return apiDefinitionMapper.findByTag(tag);
    }

    @Override
    public void incrementCallCount(Long id) {
        apiDefinitionMapper.incrementCallCount(id);
    }

    @Override
    public void incrementErrorCount(Long id) {
        apiDefinitionMapper.incrementErrorCount(id);
    }

    @Override
    public void updateStatistics(Long id, Integer executeTime) {
        ApiDefinition api = getById(id);
        if (api != null) {
            long newCount = api.getCallCount() + 1;
            int currentAvg = api.getAvgExecuteTime() != null ? api.getAvgExecuteTime() : 0;
            int newAvg = (int) ((currentAvg * (newCount - 1) + executeTime) / newCount);
            api.setAvgExecuteTime(newAvg);
            updateById(api);
        }
    }

    @Override
    public String generateApiCode() {
        return "API_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    @Override
    public void validateApi(ApiDefinition api) {
        if (api.getApiName() == null || api.getApiName().trim().isEmpty()) {
            throw new RuntimeException("API名称不能为空");
        }
        if (api.getApiPath() == null || api.getApiPath().trim().isEmpty()) {
            throw new RuntimeException("API路径不能为空");
        }
        if (api.getApiMethod() == null || api.getApiMethod().trim().isEmpty()) {
            throw new RuntimeException("请求方法不能为空");
        }
        ApiDefinition existing = findPublishedByPathAndMethod(api.getApiPath(), api.getApiMethod());
        if (existing != null && !existing.getId().equals(api.getId())) {
            throw new RuntimeException("该API路径和方法已存在");
        }
    }

    @Override
    public void testConnection(Long datasourceId) {
        // TODO: 实现数据源连接测试
    }

    @Override
    @Transactional
    public void copyApi(Long id, String newName, String user) {
        ApiDefinition original = getById(id);
        if (original == null) {
            throw new RuntimeException("API不存在");
        }
        
        ApiDefinition copy = new ApiDefinition();
        copy.setApiName(newName);
        copy.setApiCode(generateApiCode());
        copy.setApiPath(original.getApiPath() + "_copy");
        copy.setApiMethod(original.getApiMethod());
        copy.setApiVersion(original.getApiVersion());
        copy.setGroupId(original.getGroupId());
        copy.setGroupName(original.getGroupName());
        copy.setTags(original.getTags());
        copy.setDescription(original.getDescription());
        copy.setDatasourceId(original.getDatasourceId());
        copy.setDatasourceType(original.getDatasourceType());
        copy.setDataCenterGroupId(original.getDataCenterGroupId());
        copy.setTableName(original.getTableName());
        copy.setExecMode(original.getExecMode());
        copy.setSqlTemplate(original.getSqlTemplate());
        copy.setFlowConfig(original.getFlowConfig());
        copy.setResponseWrapper(original.getResponseWrapper());
        copy.setResponseTemplate(original.getResponseTemplate());
        copy.setAuthType(original.getAuthType());
        copy.setAuthConfig(original.getAuthConfig());
        copy.setRateLimit(original.getRateLimit());
        copy.setIpWhitelist(original.getIpWhitelist());
        copy.setIpBlacklist(original.getIpBlacklist());
        copy.setCorsEnabled(original.getCorsEnabled());
        copy.setMockEnabled(original.getMockEnabled());
        copy.setMockData(original.getMockData());
        copy.setEnvironment(original.getEnvironment());
        copy.setStatus("draft");
        copy.setCallCount(0L);
        copy.setErrorCount(0L);
        copy.setDeleted(0);
        copy.setCreateTime(LocalDateTime.now());
        copy.setUpdateTime(LocalDateTime.now());
        copy.setCreateUser(user);
        
        save(copy);
    }

    @Override
    @Transactional
    public ApiDefinitionDTO getApiDetail(Long id) {
        ApiDefinition api = getById(id);
        if (api == null) {
            return null;
        }
        
        ApiDefinitionDTO dto = convertToDTO(api);
        
        List<ApiParameter> params = apiParameterService.findByApiId(id);
        dto.setRequestParams(params.stream()
                .filter(p -> "REQUEST".equals(p.getParamCategory()))
                .map(this::convertParamToDTO)
                .collect(Collectors.toList()));
        dto.setResponseParams(params.stream()
                .filter(p -> "RESPONSE".equals(p.getParamCategory()))
                .map(this::convertParamToDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }

    @Override
    @Transactional
    public Long createApi(ApiDefinitionDTO dto) {
        ApiDefinition api = convertToEntity(dto);
        api.setApiCode(generateApiCode());
        api.setStatus("draft");
        api.setCallCount(0L);
        api.setErrorCount(0L);
        api.setDeleted(0);
        api.setCreateTime(LocalDateTime.now());
        api.setUpdateTime(LocalDateTime.now());
        
        if (dto.getGroupId() != null) {
            ApiGroup group = apiGroupService.getById(dto.getGroupId());
            if (group != null) {
                api.setGroupName(group.getGroupName());
            }
        }
        
        if (api.getEnvironment() == null) {
            api.setEnvironment("dev");
        }
        
        if ("crud".equals(api.getExecMode())) {
            api.setExecMode("AUTO");
        }
        
        validateApi(api);
        save(api);
        
        if (dto.getRequestParams() != null) {
            saveParameters(api.getId(), "REQUEST", dto.getRequestParams());
        }
        if (dto.getResponseParams() != null) {
            saveParameters(api.getId(), "RESPONSE", dto.getResponseParams());
        }
        
        apiVersionService.createVersion(api.getId(), "CREATE", "初始创建", api.getCreateUser());
        
        return api.getId();
    }

    @Override
    @Transactional
    public void updateApi(Long id, ApiDefinitionDTO dto) {
        ApiDefinition api = getById(id);
        if (api == null) {
            throw new RuntimeException("API不存在");
        }
        
        updateEntityFromDTO(api, dto);
        
        if (dto.getGroupId() != null) {
            ApiGroup group = apiGroupService.getById(dto.getGroupId());
            if (group != null) {
                api.setGroupName(group.getGroupName());
            }
        } else {
            api.setGroupName(null);
        }
        
        if (api.getEnvironment() == null) {
            api.setEnvironment("dev");
        }
        if ("crud".equals(api.getExecMode())) {
            api.setExecMode("AUTO");
        }
        
        api.setUpdateTime(LocalDateTime.now());
        validateApi(api);
        updateById(api);
        
        apiParameterService.deleteByApiId(id);
        if (dto.getRequestParams() != null) {
            saveParameters(id, "REQUEST", dto.getRequestParams());
        }
        if (dto.getResponseParams() != null) {
            saveParameters(id, "RESPONSE", dto.getResponseParams());
        }
        
        apiVersionService.createVersion(id, "UPDATE", dto.getChangeLog(), api.getUpdateUser());
    }

    @Override
    @Transactional
    public void publishApi(Long id, String user) {
        ApiDefinition api = getById(id);
        if (api == null) {
            throw new RuntimeException("API不存在");
        }
        
        api.setStatus("published");
        api.setPublishTime(LocalDateTime.now());
        api.setPublishUser(user);
        api.setUpdateTime(LocalDateTime.now());
        updateById(api);
        
        apiVersionService.createVersion(id, "PUBLISH", "API发布", user);
    }

    @Override
    @Transactional
    public void offlineApi(Long id, String user) {
        ApiDefinition api = getById(id);
        if (api == null) {
            throw new RuntimeException("API不存在");
        }
        
        api.setStatus("offline");
        api.setUpdateTime(LocalDateTime.now());
        updateById(api);
        
        apiVersionService.createVersion(id, "OFFLINE", "API下线", user);
    }

    @Override
    @Transactional
    public void deleteApi(Long id) {
        ApiDefinition api = getById(id);
        if (api != null) {
            api.setDeleted(1);
            api.setUpdateTime(LocalDateTime.now());
            updateById(api);
        }
    }

    @Override
    @Transactional
    public void rollbackToVersion(Long apiId, Long versionId) {
        apiVersionService.rollbackToVersion(apiId, versionId);
    }

    @Override
    public Object testApi(Long id, Map<String, Object> params) {
        ApiDefinition api = getById(id);
        if (api == null) {
            throw new RuntimeException("API 不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", api.getApiName());
        result.put("apiPath", api.getApiPath());
        result.put("apiMethod", api.getApiMethod());
        result.put("execMode", api.getExecMode());
        result.put("params", params);
        
        try {
            if ("AUTO".equals(api.getExecMode())) {
                String tableName = api.getTableName();
                if (tableName == null || tableName.isEmpty()) {
                    throw new RuntimeException("表名不能为空");
                }
                
                String sql = buildAutoSql(api.getApiMethod(), tableName, params);
                result.put("sql", sql);
                
                if ("GET".equalsIgnoreCase(api.getApiMethod())) {
                    List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
                    result.put("data", data);
                    result.put("total", data.size());
                } else if ("POST".equalsIgnoreCase(api.getApiMethod())) {
                    int affected = jdbcTemplate.update(sql);
                    result.put("affectedRows", affected);
                } else if ("PUT".equalsIgnoreCase(api.getApiMethod())) {
                    int affected = jdbcTemplate.update(sql);
                    result.put("affectedRows", affected);
                } else if ("DELETE".equalsIgnoreCase(api.getApiMethod())) {
                    int affected = jdbcTemplate.update(sql);
                    result.put("affectedRows", affected);
                }
                
                result.put("message", "执行成功");
                
            } else if ("SQL".equals(api.getExecMode())) {
                String sqlTemplate = api.getSqlTemplate();
                if (sqlTemplate == null || sqlTemplate.isEmpty()) {
                    throw new RuntimeException("SQL 模板不能为空");
                }
                
                String sql = replaceParams(sqlTemplate, params);
                result.put("sql", sql);
                
                if (sql.trim().toUpperCase().startsWith("SELECT")) {
                    List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
                    result.put("data", data);
                    result.put("total", data.size());
                } else {
                    int affected = jdbcTemplate.update(sql);
                    result.put("affectedRows", affected);
                }
                
                result.put("message", "执行成功");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "执行失败: " + e.getMessage());
        }
        
        return result;
    }
    
    private String buildAutoSql(String method, String tableName, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        
        if ("GET".equalsIgnoreCase(method)) {
            sql.append("SELECT * FROM ").append(tableName);
            if (params != null && !params.isEmpty()) {
                sql.append(" WHERE ");
                List<String> conditions = new ArrayList<>();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    conditions.add(entry.getKey() + " = '" + entry.getValue() + "'");
                }
                sql.append(String.join(" AND ", conditions));
            }
            sql.append(" LIMIT 100");
        } else if ("POST".equalsIgnoreCase(method)) {
            if (params == null || params.isEmpty()) {
                throw new RuntimeException("POST 请求需要参数");
            }
            sql.append("INSERT INTO ").append(tableName).append(" (");
            sql.append(String.join(", ", params.keySet()));
            sql.append(") VALUES ('");
            sql.append(String.join("', '", params.values().stream().map(Object::toString).collect(Collectors.toList())));
            sql.append("')");
        } else if ("PUT".equalsIgnoreCase(method)) {
            if (params == null || !params.containsKey("id")) {
                throw new RuntimeException("PUT 请求需要 id 参数");
            }
            sql.append("UPDATE ").append(tableName).append(" SET ");
            List<String> sets = new ArrayList<>();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (!"id".equals(entry.getKey())) {
                    sets.add(entry.getKey() + " = '" + entry.getValue() + "'");
                }
            }
            sql.append(String.join(", ", sets));
            sql.append(" WHERE id = '").append(params.get("id")).append("'");
        } else if ("DELETE".equalsIgnoreCase(method)) {
            if (params == null || !params.containsKey("id")) {
                throw new RuntimeException("DELETE 请求需要 id 参数");
            }
            sql.append("DELETE FROM ").append(tableName).append(" WHERE id = '").append(params.get("id")).append("'");
        }
        
        return sql.toString();
    }
    
    private String replaceParams(String template, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return template;
        }
        
        String result = template;
        Pattern pattern = Pattern.compile("\\{\\{(\\w+)\\}\\}");
        Matcher matcher = pattern.matcher(template);
        
        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object value = params.get(paramName);
            if (value != null) {
                result = result.replace("{{" + paramName + "}}", "'" + value.toString() + "'");
            }
        }
        
        return result;
    }

    private void saveParameters(Long apiId, String category, List<ApiDefinitionDTO.ApiParameterDTO> params) {
        int order = 0;
        for (ApiDefinitionDTO.ApiParameterDTO dto : params) {
            ApiParameter param = new ApiParameter();
            param.setApiId(apiId);
            param.setParamCategory(category);
            param.setParamPosition(dto.getParamPosition());
            param.setParamName(dto.getParamName());
            param.setParamLabel(dto.getParamLabel());
            param.setParamType(dto.getParamType());
            param.setParamFormat(dto.getParamFormat());
            param.setRequired(dto.getRequired());
            param.setDefaultValue(dto.getDefaultValue());
            param.setMinLength(dto.getMinLength());
            param.setMaxLength(dto.getMaxLength());
            param.setRegexPattern(dto.getRegexPattern());
            param.setEnumValues(dto.getEnumValues());
            param.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
            param.setSourceField(dto.getSourceField());
            param.setTargetField(dto.getTargetField());
            param.setSensitive(dto.getSensitive() != null ? dto.getSensitive() : 0);
            param.setMockRule(dto.getMockRule());
            param.setMockValue(dto.getMockValue());
            param.setDescription(dto.getDescription());
            param.setExampleValue(dto.getExampleValue());
            param.setSortOrder(order++);
            param.setCreateTime(LocalDateTime.now());
            param.setUpdateTime(LocalDateTime.now());
            apiParameterMapper.insert(param);
        }
    }

    private ApiDefinitionDTO convertToDTO(ApiDefinition api) {
        ApiDefinitionDTO dto = new ApiDefinitionDTO();
        dto.setId(api.getId());
        dto.setApiName(api.getApiName());
        dto.setApiCode(api.getApiCode());
        dto.setApiPath(api.getApiPath());
        dto.setApiMethod(api.getApiMethod());
        dto.setApiVersion(api.getApiVersion());
        dto.setGroupId(api.getGroupId());
        dto.setGroupName(api.getGroupName());
        dto.setTags(api.getTags());
        dto.setDescription(api.getDescription());
        dto.setDatasourceId(api.getDatasourceId());
        dto.setDatasourceType(api.getDatasourceType());
        dto.setDataCenterGroupId(api.getDataCenterGroupId());
        dto.setTableName(api.getTableName());
        dto.setExecMode(api.getExecMode());
        dto.setSqlTemplate(api.getSqlTemplate());
        dto.setFlowConfig(api.getFlowConfig());
        dto.setResponseWrapper(api.getResponseWrapper());
        dto.setResponseTemplate(api.getResponseTemplate());
        dto.setAuthType(api.getAuthType());
        dto.setAuthConfig(api.getAuthConfig());
        dto.setRateLimit(api.getRateLimit());
        dto.setIpWhitelist(api.getIpWhitelist());
        dto.setIpBlacklist(api.getIpBlacklist());
        dto.setCorsEnabled(api.getCorsEnabled());
        dto.setMockEnabled(api.getMockEnabled());
        dto.setMockData(api.getMockData());
        dto.setEnvironment(api.getEnvironment());
        dto.setStatus(api.getStatus());
        dto.setPublishTime(api.getPublishTime());
        dto.setPublishUser(api.getPublishUser());
        return dto;
    }

    private ApiDefinition convertToEntity(ApiDefinitionDTO dto) {
        ApiDefinition api = new ApiDefinition();
        updateEntityFromDTO(api, dto);
        return api;
    }

    private void updateEntityFromDTO(ApiDefinition api, ApiDefinitionDTO dto) {
        if (dto.getApiName() != null) api.setApiName(dto.getApiName());
        if (dto.getApiPath() != null) api.setApiPath(dto.getApiPath());
        if (dto.getApiMethod() != null) api.setApiMethod(dto.getApiMethod());
        if (dto.getApiVersion() != null) api.setApiVersion(dto.getApiVersion());
        if (dto.getGroupId() != null) api.setGroupId(dto.getGroupId());
        if (dto.getGroupName() != null) api.setGroupName(dto.getGroupName());
        if (dto.getTags() != null) api.setTags(dto.getTags());
        if (dto.getDescription() != null) api.setDescription(dto.getDescription());
        if (dto.getDatasourceId() != null) api.setDatasourceId(dto.getDatasourceId());
        if (dto.getDatasourceType() != null) api.setDatasourceType(dto.getDatasourceType());
        if (dto.getDataCenterGroupId() != null) api.setDataCenterGroupId(dto.getDataCenterGroupId());
        if (dto.getTableName() != null) api.setTableName(dto.getTableName());
        if (dto.getExecMode() != null) api.setExecMode(dto.getExecMode());
        if (dto.getSqlTemplate() != null) api.setSqlTemplate(dto.getSqlTemplate());
        if (dto.getFlowConfig() != null) api.setFlowConfig(dto.getFlowConfig());
        if (dto.getResponseWrapper() != null) api.setResponseWrapper(dto.getResponseWrapper());
        if (dto.getResponseTemplate() != null) api.setResponseTemplate(dto.getResponseTemplate());
        if (dto.getAuthType() != null) api.setAuthType(dto.getAuthType());
        if (dto.getAuthConfig() != null) api.setAuthConfig(dto.getAuthConfig());
        if (dto.getRateLimit() != null) api.setRateLimit(dto.getRateLimit());
        if (dto.getIpWhitelist() != null) api.setIpWhitelist(dto.getIpWhitelist());
        if (dto.getIpBlacklist() != null) api.setIpBlacklist(dto.getIpBlacklist());
        if (dto.getCorsEnabled() != null) api.setCorsEnabled(dto.getCorsEnabled());
        if (dto.getMockEnabled() != null) api.setMockEnabled(dto.getMockEnabled());
        if (dto.getMockData() != null) api.setMockData(dto.getMockData());
        if (dto.getEnvironment() != null) api.setEnvironment(dto.getEnvironment());
        if (dto.getStatus() != null) api.setStatus(dto.getStatus());
        if (dto.getOperationType() != null) api.setOperationType(dto.getOperationType());
        if (dto.getTenantAppId() != null) api.setTenantAppId(dto.getTenantAppId());
        if (dto.getReturnFields() != null) api.setReturnFields(dto.getReturnFields());
        if (dto.getRequestFields() != null) api.setRequestFields(dto.getRequestFields());
        if (dto.getConditionFields() != null) api.setConditionFields(dto.getConditionFields());
    }

    private ApiDefinitionDTO.ApiParameterDTO convertParamToDTO(ApiParameter param) {
        ApiDefinitionDTO.ApiParameterDTO dto = new ApiDefinitionDTO.ApiParameterDTO();
        dto.setId(param.getId());
        dto.setParamCategory(param.getParamCategory());
        dto.setParamPosition(param.getParamPosition());
        dto.setParamName(param.getParamName());
        dto.setParamLabel(param.getParamLabel());
        dto.setParamType(param.getParamType());
        dto.setParamFormat(param.getParamFormat());
        dto.setRequired(param.getRequired());
        dto.setDefaultValue(param.getDefaultValue());
        dto.setMinLength(param.getMinLength());
        dto.setMaxLength(param.getMaxLength());
        dto.setRegexPattern(param.getRegexPattern());
        dto.setEnumValues(param.getEnumValues());
        dto.setParentId(param.getParentId());
        dto.setSourceField(param.getSourceField());
        dto.setTargetField(param.getTargetField());
        dto.setSensitive(param.getSensitive());
        dto.setMockRule(param.getMockRule());
        dto.setMockValue(param.getMockValue());
        dto.setDescription(param.getDescription());
        dto.setExampleValue(param.getExampleValue());
        dto.setSortOrder(param.getSortOrder());
        return dto;
    }
}
