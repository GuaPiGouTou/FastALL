package org.example.ecmo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ecmo.entity.ApiDatasource;
import org.example.ecmo.mapper.ApiDatasourceMapper;
import org.example.ecmo.service.ApiDatasourceService;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiDatasourceServiceImpl extends ServiceImpl<ApiDatasourceMapper, ApiDatasource> implements ApiDatasourceService {

    @Override
    public ApiDatasource findByCodeAndEnv(String dsCode, String environment) {
        return baseMapper.findByCodeAndEnv(dsCode, environment);
    }

    @Override
    public List<ApiDatasource> findByEnvironment(String environment) {
        return baseMapper.findByEnvironment(environment);
    }

    @Override
    public List<ApiDatasource> findByType(String dsType) {
        return baseMapper.findByType(dsType);
    }

    @Override
    public boolean testConnection(Long id) {
        ApiDatasource ds = getById(id);
        if (ds == null) {
            return false;
        }

        try {
            switch (ds.getDsType().toLowerCase()) {
                case "mysql":
                    return testMysqlConnection(ds);
                case "postgresql":
                    return testPostgresqlConnection(ds);
                case "http_api":
                    return testHttpConnection(ds);
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean testMysqlConnection(ApiDatasource ds) {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=GMT%%2B8",
                ds.getHost(), ds.getPort(), ds.getDatabase());
        
        try (Connection conn = DriverManager.getConnection(url, ds.getUsername(), ds.getPassword())) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean testPostgresqlConnection(ApiDatasource ds) {
        String url = String.format("jdbc:postgresql://%s:%d/%s",
                ds.getHost(), ds.getPort(), ds.getDatabase());
        
        try (Connection conn = DriverManager.getConnection(url, ds.getUsername(), ds.getPassword())) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean testHttpConnection(ApiDatasource ds) {
        return ds.getApiUrl() != null && !ds.getApiUrl().isEmpty();
    }

    @Override
    public List<Map<String, Object>> getTables(Long id) {
        ApiDatasource ds = getById(id);
        if (ds == null) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> tables = new ArrayList<>();
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=GMT%%2B8",
                ds.getHost(), ds.getPort(), ds.getDatabase());

        try (Connection conn = DriverManager.getConnection(url, ds.getUsername(), ds.getPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
            
            while (rs.next()) {
                Map<String, Object> table = new HashMap<>();
                table.put("tableName", rs.getString(1));
                tables.add(table);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    @Override
    public List<Map<String, Object>> getTableColumns(Long id, String tableName) {
        ApiDatasource ds = getById(id);
        if (ds == null || tableName == null) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> columns = new ArrayList<>();
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=GMT%%2B8",
                ds.getHost(), ds.getPort(), ds.getDatabase());

        try (Connection conn = DriverManager.getConnection(url, ds.getUsername(), ds.getPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("DESCRIBE `" + tableName + "`")) {
            
            while (rs.next()) {
                Map<String, Object> column = new HashMap<>();
                column.put("fieldName", rs.getString("Field"));
                column.put("fieldType", rs.getString("Type"));
                column.put("nullable", "YES".equals(rs.getString("Null")));
                column.put("key", rs.getString("Key"));
                column.put("defaultValue", rs.getString("Default"));
                columns.add(column);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columns;
    }
}
