package org.example.ecmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ecmo.entity.ApiDatasource;
import java.util.List;
import java.util.Map;

public interface ApiDatasourceService extends IService<ApiDatasource> {

    ApiDatasource findByCodeAndEnv(String dsCode, String environment);

    List<ApiDatasource> findByEnvironment(String environment);

    List<ApiDatasource> findByType(String dsType);

    boolean testConnection(Long id);

    List<Map<String, Object>> getTables(Long id);

    List<Map<String, Object>> getTableColumns(Long id, String tableName);
}
