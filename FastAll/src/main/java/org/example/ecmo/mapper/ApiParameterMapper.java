package org.example.ecmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.ecmo.entity.ApiParameter;
import java.util.List;

@Mapper
public interface ApiParameterMapper extends BaseMapper<ApiParameter> {

    @Select("SELECT * FROM api_parameter WHERE api_id = #{apiId} ORDER BY sort_order ASC")
    List<ApiParameter> findByApiId(@Param("apiId") Long apiId);

    @Select("SELECT * FROM api_parameter WHERE api_id = #{apiId} AND param_category = #{category} ORDER BY sort_order ASC")
    List<ApiParameter> findByApiIdAndCategory(@Param("apiId") Long apiId, @Param("category") String category);

    @Select("SELECT * FROM api_parameter WHERE api_id = #{apiId} AND param_position = #{position} ORDER BY sort_order ASC")
    List<ApiParameter> findByApiIdAndPosition(@Param("apiId") Long apiId, @Param("position") String position);

    @Select("SELECT * FROM api_parameter WHERE api_version_id = #{versionId} ORDER BY sort_order ASC")
    List<ApiParameter> findByVersionId(@Param("versionId") Long versionId);

    @Select("SELECT * FROM api_parameter WHERE parent_id = #{parentId} ORDER BY sort_order ASC")
    List<ApiParameter> findByParentId(@Param("parentId") Long parentId);
}
