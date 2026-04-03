package org.example.ecmo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ecmo.entity.ApiGroup;
import org.example.ecmo.mapper.ApiGroupMapper;
import org.example.ecmo.service.ApiGroupService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApiGroupServiceImpl extends ServiceImpl<ApiGroupMapper, ApiGroup> implements ApiGroupService {

    @Override
    public List<ApiGroup> findByParentId(Long parentId) {
        return baseMapper.findByParentId(parentId);
    }

    @Override
    public List<ApiGroup> findAllActive() {
        return baseMapper.findAllActive();
    }

    @Override
    public ApiGroup findByCode(String groupCode) {
        return baseMapper.findByCode(groupCode);
    }

    @Override
    public List<ApiGroup> buildTree() {
        List<ApiGroup> allGroups = findAllActive();
        
        Map<Long, List<ApiGroup>> childrenMap = allGroups.stream()
                .filter(g -> g.getParentId() != null && g.getParentId() > 0)
                .collect(Collectors.groupingBy(ApiGroup::getParentId));

        List<ApiGroup> rootGroups = allGroups.stream()
                .filter(g -> g.getParentId() == null || g.getParentId() == 0)
                .collect(Collectors.toList());

        buildTreeRecursive(rootGroups, childrenMap);
        
        return rootGroups;
    }

    private void buildTreeRecursive(List<ApiGroup> groups, Map<Long, List<ApiGroup>> childrenMap) {
        if (groups == null) return;
        
        for (ApiGroup group : groups) {
            List<ApiGroup> children = childrenMap.get(group.getId());
            if (children != null && !children.isEmpty()) {
                buildTreeRecursive(children, childrenMap);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getGroupTreeNested() {
        List<ApiGroup> allGroups = findAllActive();
        Map<Long, Map<String, Object>> groupMap = new HashMap<>();
        List<Map<String, Object>> rootGroups = new ArrayList<>();

        for (ApiGroup group : allGroups) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", group.getId());
            node.put("groupName", group.getGroupName());
            node.put("groupCode", group.getGroupCode());
            node.put("parentId", group.getParentId());
            node.put("sortOrder", group.getSortOrder());
            node.put("icon", group.getIcon());
            node.put("description", group.getDescription());
            node.put("children", new ArrayList<Map<String, Object>>());
            groupMap.put(group.getId(), node);
        }

        for (ApiGroup group : allGroups) {
            Map<String, Object> node = groupMap.get(group.getId());
            if (group.getParentId() == null || group.getParentId() == 0) {
                rootGroups.add(node);
            } else {
                Map<String, Object> parentNode = groupMap.get(group.getParentId());
                if (parentNode != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parentNode.get("children");
                    children.add(node);
                } else {
                    rootGroups.add(node);
                }
            }
        }

        return rootGroups;
    }
}
