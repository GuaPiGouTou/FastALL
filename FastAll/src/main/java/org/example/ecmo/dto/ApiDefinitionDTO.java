package org.example.ecmo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApiDefinitionDTO {

    private Long id;

    private String apiName;

    private String apiCode;

    private String apiPath;

    private String apiMethod;

    private String apiVersion;

    private Long groupId;

    private String groupName;

    private String tags;

    private String description;

    private Long datasourceId;

    private String datasourceType;

    private Long dataCenterGroupId;

    private String tableName;

    private String execMode;

    private String sqlTemplate;

    private Object flowConfig;

    private Integer responseWrapper;

    private String responseTemplate;

    private String authType;

    private Object authConfig;

    private Integer rateLimit;

    private String ipWhitelist;

    private String ipBlacklist;

    private Integer corsEnabled;

    private Integer mockEnabled;

    private String mockData;

    private String environment;

    private String status;

    private LocalDateTime publishTime;

    private String publishUser;

    private String changeLog;

    private String createUser;

    private String updateUser;

    private List<ApiParameterDTO> requestParams;

    private List<ApiParameterDTO> responseParams;

    private List<ApiFlowNodeDTO> flowNodes;

    @Data
    public static class ApiParameterDTO {
        private Long id;
        private String paramCategory;
        private String paramPosition;
        private String paramName;
        private String paramLabel;
        private String paramType;
        private String paramFormat;
        private Integer required;
        private String defaultValue;
        private Integer minLength;
        private Integer maxLength;
        private Double minValue;
        private Double maxValue;
        private String regexPattern;
        private String enumValues;
        private Long parentId;
        private Object objectSchema;
        private String sourceField;
        private String targetField;
        private String fieldTransform;
        private Integer sensitive;
        private String mockRule;
        private String mockValue;
        private String description;
        private String exampleValue;
        private Integer sortOrder;
    }

    @Data
    public static class ApiFlowNodeDTO {
        private Long id;
        private String flowId;
        private String nodeId;
        private String nodeName;
        private String nodeType;
        private Object nodeConfig;
        private String prevNodes;
        private String nextNodes;
        private Integer execOrder;
        private String conditionExpr;
        private Integer timeout;
        private Integer retryCount;
        private Integer positionX;
        private Integer positionY;
    }
}
