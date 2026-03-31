package org.example.ecmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.ecmo.entity.ApiDefinition;
import java.util.List;
import java.util.Map;

@Mapper
public interface ApiDefinitionMapper extends BaseMapper<ApiDefinition> {

    @Select("SELECT * FROM api_definition WHERE api_path = #{apiPath} AND api_method = #{apiMethod} AND status = 'published' AND deleted = 0")
    ApiDefinition findPublishedByPathAndMethod(@Param("apiPath") String apiPath, @Param("apiMethod") String apiMethod);

    @Select("SELECT * FROM api_definition WHERE api_code = #{apiCode} AND deleted = 0")
    ApiDefinition findByCode(@Param("apiCode") String apiCode);

    @Select("SELECT COUNT(*) FROM api_definition WHERE deleted = 0")
    int getTotalCount();

    @Select("SELECT COUNT(*) FROM api_definition WHERE status = 'published' AND deleted = 0")
    int getPublishedCount();

    @Select("SELECT COUNT(*) FROM api_definition WHERE status = 'draft' AND deleted = 0")
    int getDraftCount();

    @Select("SELECT group_name, COUNT(*) as count FROM api_definition WHERE deleted = 0 GROUP BY group_name")
    List<Map<String, Object>> getCountByGroup();

    @Select("SELECT api_method, COUNT(*) as count FROM api_definition WHERE deleted = 0 GROUP BY api_method")
    List<Map<String, Object>> getCountByMethod();

    @Select("SELECT environment, COUNT(*) as count FROM api_definition WHERE deleted = 0 GROUP BY environment")
    List<Map<String, Object>> getCountByEnvironment();

    @Update("UPDATE api_definition SET call_count = call_count + 1 WHERE id = #{id}")
    void incrementCallCount(@Param("id") Long id);

    @Update("UPDATE api_definition SET error_count = error_count + 1 WHERE id = #{id}")
    void incrementErrorCount(@Param("id") Long id);

    @Select("SELECT * FROM api_definition WHERE group_id = #{groupId} AND deleted = 0 ORDER BY sort_order ASC")
    List<ApiDefinition> findByGroupId(@Param("groupId") Long groupId);

    @Select("SELECT * FROM api_definition WHERE tags LIKE CONCAT('%', #{tag}, '%') AND deleted = 0")
    List<ApiDefinition> findByTag(@Param("tag") String tag);
}
