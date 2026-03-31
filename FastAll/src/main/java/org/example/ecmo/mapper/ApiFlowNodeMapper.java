package org.example.ecmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.ecmo.entity.ApiFlowNode;
import java.util.List;

@Mapper
public interface ApiFlowNodeMapper extends BaseMapper<ApiFlowNode> {

    @Select("SELECT * FROM api_flow_node WHERE api_id = #{apiId} ORDER BY exec_order ASC")
    List<ApiFlowNode> findByApiId(@Param("apiId") Long apiId);

    @Select("SELECT * FROM api_flow_node WHERE flow_id = #{flowId} ORDER BY exec_order ASC")
    List<ApiFlowNode> findByFlowId(@Param("flowId") String flowId);

    @Select("SELECT * FROM api_flow_node WHERE api_id = #{apiId} AND node_id = #{nodeId}")
    ApiFlowNode findByNodeId(@Param("apiId") Long apiId, @Param("nodeId") String nodeId);
}
