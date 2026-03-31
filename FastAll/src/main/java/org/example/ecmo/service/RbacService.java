package org.example.ecmo.service;

import org.example.ecmo.entity.SysRole;
import org.example.ecmo.entity.SysPermission;
import org.example.ecmo.entity.SysUserRole;
import org.example.ecmo.entity.SysRolePermission;
import org.example.ecmo.mapper.SysRoleMapper;
import org.example.ecmo.mapper.SysPermissionMapper;
import org.example.ecmo.mapper.SysUserRoleMapper;
import org.example.ecmo.mapper.SysRolePermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RbacService {

    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysPermissionMapper sysPermissionMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    // ==================== 角色管理 ====================

    /**
     * 获取所有角色列表
     * @return 角色列表
     */
    public List<SysRole> getAllRoles() {
        return sysRoleMapper.getAllRoles();
    }

    /**
     * 获取单个角色
     * @param id 角色ID
     * @return 角色信息
     */
    public SysRole getRoleById(Long id) {
        return sysRoleMapper.selectById(id);
    }

    /**
     * 创建角色
     * @param role 角色信息
     * @return 创建结果
     */
    public boolean createRole(SysRole role) {
        return sysRoleMapper.insert(role) > 0;
    }

    /**
     * 更新角色
     * @param role 角色信息
     * @return 更新结果
     */
    public boolean updateRole(SysRole role) {
        return sysRoleMapper.updateById(role) > 0;
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    @Transactional
    public boolean deleteRole(Long id) {
        // 删除角色关联的权限
        sysRolePermissionMapper.deleteByRoleId(id);
        // 删除用户与角色的关联
        sysUserRoleMapper.deleteByRoleId(id);
        // 删除角色
        return sysRoleMapper.deleteById(id) > 0;
    }

    // ==================== 角色权限分配 ====================

    /**
     * 获取角色的权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    public List<Long> getRolePermissionIds(Long roleId) {
        return sysPermissionMapper.getPermissionIdsByRoleId(roleId);
    }

    /**
     * 批量分配权限给角色
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 分配结果
     */
    @Transactional
    public boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 删除角色原有的权限
        sysRolePermissionMapper.deleteByRoleId(roleId);
        
        // 批量添加新的权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermission> rolePermissions = new ArrayList<>();
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissions.add(rolePermission);
            }
            sysRolePermissionMapper.batchInsert(rolePermissions);
        }
        
        return true;
    }

    /**
     * 添加单个权限到角色
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 添加结果
     */
    public boolean addPermissionToRole(Long roleId, Long permissionId) {
        SysRolePermission rolePermission = new SysRolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        return sysRolePermissionMapper.insert(rolePermission) > 0;
    }

    /**
     * 移除角色的单个权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 移除结果
     */
    public boolean removePermissionFromRole(Long roleId, Long permissionId) {
        return sysRolePermissionMapper.deleteByRoleIdAndPermissionId(roleId, permissionId) > 0;
    }

    // ==================== 用户角色分配 ====================

    /**
     * 获取用户的角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<SysRole> getUserRoles(Long userId) {
        return sysRoleMapper.getRolesByUserId(userId);
    }

    /**
     * 获取用户的角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    public List<Long> getUserRoleIds(Long userId) {
        return sysRoleMapper.getRoleIdsByUserId(userId);
    }

    /**
     * 批量分配角色给用户
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 分配结果
     */
    @Transactional
    public boolean assignRolesToUser(Long userId, List<Long> roleIds) {
        // 删除用户原有的角色
        sysUserRoleMapper.deleteByUserId(userId);
        
        // 批量添加新的角色
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> userRoles = new ArrayList<>();
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoles.add(userRole);
            }
            sysUserRoleMapper.batchInsert(userRoles);
        }
        
        return true;
    }

    /**
     * 添加单个角色到用户
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 添加结果
     */
    public boolean addRoleToUser(Long userId, Long roleId) {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return sysUserRoleMapper.insert(userRole) > 0;
    }

    /**
     * 移除用户的单个角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 移除结果
     */
    public boolean removeRoleFromUser(Long userId, Long roleId) {
        return sysUserRoleMapper.deleteByUserIdAndRoleId(userId, roleId) > 0;
    }

    // ==================== 用户权限查询 ====================

    /**
     * 获取用户的权限编码列表
     * @param userId 用户ID
     * @return 权限编码列表
     */
    public List<String> getUserPermissionCodes(Long userId) {
        return sysPermissionMapper.getPermissionCodesByUserId(userId);
    }

    /**
     * 检查用户是否拥有某权限
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    public boolean checkUserPermission(Long userId, String permissionCode) {
        List<String> permissionCodes = sysPermissionMapper.getPermissionCodesByUserId(userId);
        return permissionCodes != null && permissionCodes.contains(permissionCode);
    }

    // ==================== 权限管理 ====================

    /**
     * 获取所有权限列表
     * @return 权限列表
     */
    public List<SysPermission> getAllPermissions() {
        return sysPermissionMapper.getAllPermissions();
    }

    /**
     * 获取权限树结构
     * @return 权限树结构
     */
    public List<Map<String, Object>> getPermissionTree() {
        List<SysPermission> permissions = sysPermissionMapper.getAllPermissions();
        return buildPermissionTree(permissions);
    }

    /**
     * 按类型获取权限列表
     * @param type 资源类型
     * @return 权限列表
     */
    public List<SysPermission> getPermissionsByType(String type) {
        return sysPermissionMapper.getPermissionsByType(type);
    }

    /**
     * 构建权限树结构
     * @param permissions 权限列表
     * @return 权限树结构
     */
    private List<Map<String, Object>> buildPermissionTree(List<SysPermission> permissions) {
        // 按父ID分组
        Map<Long, List<SysPermission>> permissionMap = permissions.stream()
                .collect(Collectors.groupingBy(SysPermission::getParentId));
        
        // 构建树结构
        return buildTree(0L, permissionMap);
    }

    /**
     * 递归构建树结构
     * @param parentId 父级ID
     * @param permissionMap 权限映射
     * @return 树节点列表
     */
    private List<Map<String, Object>> buildTree(Long parentId, Map<Long, List<SysPermission>> permissionMap) {
        List<Map<String, Object>> treeNodes = new ArrayList<>();
        List<SysPermission> childPermissions = permissionMap.get(parentId);
        
        if (childPermissions != null) {
            for (SysPermission permission : childPermissions) {
                Map<String, Object> node = new java.util.HashMap<>();
                node.put("id", permission.getId());
                node.put("label", permission.getPermissionName());
                node.put("code", permission.getPermissionCode());
                node.put("type", permission.getResourceType());
                node.put("children", buildTree(permission.getId(), permissionMap));
                treeNodes.add(node);
            }
        }
        
        return treeNodes;
    }
}