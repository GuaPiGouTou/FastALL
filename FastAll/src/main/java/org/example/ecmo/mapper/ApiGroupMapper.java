package org.example.ecmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.ecmo.entity.ApiGroup;
import java.util.List;

@Mapper
public interface ApiGroupMapper extends BaseMapper<ApiGroup> {

    @Select("SELECT * FROM api_group WHERE parent_id = #{parentId} ORDER BY sort_order ASC")
    List<ApiGroup> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM api_group WHERE status = 1 ORDER BY sort_order ASC")
    List<ApiGroup> findAllActive();

    @Select("SELECT * FROM api_group WHERE group_code = #{groupCode}")
    ApiGroup findByCode(@Param("groupCode") String groupCode);
}
