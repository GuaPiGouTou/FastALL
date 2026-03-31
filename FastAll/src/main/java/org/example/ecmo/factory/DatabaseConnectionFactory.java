package org.example.ecmo.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DatabaseConnectionFactory {
    
    private static final Map<String, DatabaseConfig> DATABASE_CONFIGS = new HashMap<>();
    
    static {
        DATABASE_CONFIGS.put("mysql", new DatabaseConfig(
            "com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://{host}:{port}/{database}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai",
            3306
        ));
        
        DATABASE_CONFIGS.put("postgresql", new DatabaseConfig(
            "org.postgresql.Driver",
            "jdbc:postgresql://{host}:{port}/{database}",
            5432
        ));
        
        DATABASE_CONFIGS.put("sqlite", new DatabaseConfig(
            "org.sqlite.JDBC",
            "jdbc:sqlite:{database}",
            0
        ));
        
        DATABASE_CONFIGS.put("mongodb", new DatabaseConfig(
            "com.mongodb.client.MongoClient",
            "mongodb://{host}:{port}/{database}",
            27017
        ));
        
        DATABASE_CONFIGS.put("oracle", new DatabaseConfig(
            "oracle.jdbc.OracleDriver",
            "jdbc:oracle:thin:@{host}:{port}:{database}",
            1521
        ));
        
        DATABASE_CONFIGS.put("sqlserver", new DatabaseConfig(
            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "jdbc:sqlserver://{host}:{port};databaseName={database}",
            1433
        ));
        
        DATABASE_CONFIGS.put("mariadb", new DatabaseConfig(
            "org.mariadb.jdbc.Driver",
            "jdbc:mariadb://{host}:{port}/{database}",
            3306
        ));
        
        DATABASE_CONFIGS.put("h2", new DatabaseConfig(
            "org.h2.Driver",
            "jdbc:h2:{database}",
            0
        ));
    }
    
    public Connection createConnection(String dbType, String host, Integer port, 
                                       String database, String username, String password) throws SQLException {
        DatabaseConfig config = DATABASE_CONFIGS.get(dbType.toLowerCase());
        if (config == null) {
            throw new IllegalArgumentException("不支持的数据库类型: " + dbType);
        }
        
        try {
            Class.forName(config.getDriverClass());
        } catch (ClassNotFoundException e) {
            throw new SQLException("数据库驱动未找到: " + config.getDriverClass(), e);
        }
        
        String url = buildUrl(config.getUrlTemplate(), host, port, database);
        
        if (username == null || username.isEmpty()) {
            return DriverManager.getConnection(url);
        } else {
            return DriverManager.getConnection(url, username, password);
        }
    }
    
    public String buildUrl(String template, String host, Integer port, String database) {
        return template
            .replace("{host}", host != null ? host : "localhost")
            .replace("{port}", port != null ? String.valueOf(port) : "3306")
            .replace("{database}", database != null ? database : "test");
    }
    
    public Map<String, Object> testConnection(String dbType, String host, Integer port, 
                                              String database, String username, String password) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try (Connection connection = createConnection(dbType, host, port, database, username, password)) {
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            result.put("status", "connected");
            result.put("dbType", dbType.toUpperCase());
            result.put("responseTime", responseTime + "ms");
            result.put("databaseName", connection.getCatalog());
            result.put("databaseVersion", connection.getMetaData().getDatabaseProductName() + " " + 
                                         connection.getMetaData().getDatabaseProductVersion());
            result.put("driverName", connection.getMetaData().getDriverName());
            result.put("driverVersion", connection.getMetaData().getDriverVersion());
            
            log.info("数据库连接测试成功: {} - {}ms", dbType, responseTime);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            result.put("status", "disconnected");
            result.put("dbType", dbType.toUpperCase());
            result.put("responseTime", responseTime + "ms");
            result.put("error", e.getMessage());
            
            log.error("数据库连接测试失败: {} - {}", dbType, e.getMessage());
        }
        
        return result;
    }
    
    public static class DatabaseConfig {
        private final String driverClass;
        private final String urlTemplate;
        private final int defaultPort;
        
        public DatabaseConfig(String driverClass, String urlTemplate, int defaultPort) {
            this.driverClass = driverClass;
            this.urlTemplate = urlTemplate;
            this.defaultPort = defaultPort;
        }
        
        public String getDriverClass() {
            return driverClass;
        }
        
        public String getUrlTemplate() {
            return urlTemplate;
        }
        
        public int getDefaultPort() {
            return defaultPort;
        }
    }
}
