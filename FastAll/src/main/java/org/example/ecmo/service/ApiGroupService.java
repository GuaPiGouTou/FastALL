package org.example.ecmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ecmo.entity.ApiGroup;
import java.util.List;
import java.util.Map;

public interface ApiGroupService extends IService<ApiGroup> {

    List<ApiGroup> findByParentId(Long parentId);

    List<ApiGroup> findAllActive();

    ApiGroup findByCode(String groupCode);

    List<ApiGroup> buildTree();

    /**
     * 与数据中心 / API 生成器前端约定一致：带 children 嵌套的树（id、groupName、groupCode 等）
     */
    List<Map<String, Object>> getGroupTreeNested();
}
