package org.example.ecmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.ecmo.entity.ApiVersion;
import java.util.List;

@Mapper
public interface ApiVersionMapper extends BaseMapper<ApiVersion> {

    @Select("SELECT * FROM api_version WHERE api_id = #{apiId} ORDER BY create_time DESC")
    List<ApiVersion> findByApiId(@Param("apiId") Long apiId);

    @Select("SELECT * FROM api_version WHERE api_id = #{apiId} AND is_current = 1")
    ApiVersion findCurrentVersion(@Param("apiId") Long apiId);

    @Select("SELECT * FROM api_version WHERE api_id = #{apiId} AND version_no = #{versionNo}")
    ApiVersion findByVersionNo(@Param("apiId") Long apiId, @Param("versionNo") String versionNo);

    @Update("UPDATE api_version SET is_current = 0 WHERE api_id = #{apiId}")
    void clearCurrentVersion(@Param("apiId") Long apiId);

    @Select("SELECT COUNT(*) FROM api_version WHERE api_id = #{apiId}")
    int countByApiId(@Param("apiId") Long apiId);
}
