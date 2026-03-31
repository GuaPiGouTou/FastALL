package org.example.ecmo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ecmo.entity.DataTableGroup;
import org.example.ecmo.mapper.DataTableGroupMapper;
import org.example.ecmo.service.DataTableGroupService;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataTableGroupServiceImpl extends ServiceImpl<DataTableGroupMapper, DataTableGroup> implements DataTableGroupService {

    @Override
    public List<DataTableGroup> findAllActive() {
        LambdaQueryWrapper<DataTableGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataTableGroup::getStatus, 1)
               .orderByAsc(DataTableGroup::getSortOrder)
               .orderByAsc(DataTableGroup::getId);
        return list(wrapper);
    }

    @Override
    public List<Map<String, Object>> getGroupTree() {
        List<DataTableGroup> allGroups = findAllActive();
        
        Map<Long, Map<String, Object>> groupMap = new HashMap<>();
        List<Map<String, Object>> rootGroups = new ArrayList<>();
        
        for (DataTableGroup group : allGroups) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", group.getId());
            node.put("groupName", group.getGroupName());
            node.put("groupCode", group.getGroupCode());
            node.put("parentId", group.getParentId());
            node.put("sortOrder", group.getSortOrder());
            node.put("children", new ArrayList<>());
            groupMap.put(group.getId(), node);
        }
        
        for (DataTableGroup group : allGroups) {
            Map<String, Object> node = groupMap.get(group.getId());
            if (group.getParentId() == null || group.getParentId() == 0) {
                rootGroups.add(node);
            } else {
                Map<String, Object> parentNode = groupMap.get(group.getParentId());
                if (parentNode != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parentNode.get("children");
                    children.add(node);
                }
            }
        }
        
        return rootGroups;
    }

    @Override
    public DataTableGroup findByCode(String groupCode) {
        LambdaQueryWrapper<DataTableGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataTableGroup::getGroupCode, groupCode)
               .eq(DataTableGroup::getStatus, 1);
        return getOne(wrapper);
    }
}
