package org.example.ecmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ecmo.entity.ApiGroup;
import java.util.List;

public interface ApiGroupService extends IService<ApiGroup> {

    List<ApiGroup> findByParentId(Long parentId);

    List<ApiGroup> findAllActive();

    ApiGroup findByCode(String groupCode);

    List<ApiGroup> buildTree();
}
