package org.example.ecmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.ecmo.entity.ApiDatasource;
import java.util.List;

@Mapper
public interface ApiDatasourceMapper extends BaseMapper<ApiDatasource> {

    @Select("SELECT * FROM api_datasource WHERE ds_code = #{dsCode} AND environment = #{environment}")
    ApiDatasource findByCodeAndEnv(@Param("dsCode") String dsCode, @Param("environment") String environment);

    @Select("SELECT * FROM api_datasource WHERE environment = #{environment} AND status = 1")
    List<ApiDatasource> findByEnvironment(@Param("environment") String environment);

    @Select("SELECT * FROM api_datasource WHERE ds_type = #{dsType} AND status = 1")
    List<ApiDatasource> findByType(@Param("dsType") String dsType);
}
