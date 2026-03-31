package org.example.ecmo.controller;

import org.example.ecmo.annotation.AuditLog;
import org.example.ecmo.config.JsonResult;
import org.example.ecmo.entity.SysRole;
import org.example.ecmo.entity.SysPermission;
import org.example.ecmo.service.RbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rbac")
public class RbacController {

    @Autowired
    private RbacService rbacService;

    // ==================== 角色管理 ====================

    /**
     * 获取所有角色列表
     * @return 角色列表
     */
    @AuditLog(title = "查询角色列表", businessType = 0)
    @GetMapping("/roles")
    public JsonResult getAllRoles() {
        List<SysRole> roles = rbacService.getAllRoles();
        return JsonResult.success(roles);
    }

    /**
     * 获取单个角色
     * @param id 角色ID
     * @return 角色信息
     */
    @AuditLog(title = "查询角色详情", businessType = 0)
    @GetMapping("/roles/{id}")
    public JsonResult getRoleById(@PathVariable Long id) {
        SysRole role = rbacService.getRoleById(id);
        return JsonResult.success(role);
    }

    /**
     * 创建角色
     * @param role 角色信息
     * @return 创建结果
     */
    @AuditLog(title = "创建角色", businessType = 0)
    @PostMapping("/roles")
    public JsonResult createRole(@RequestBody SysRole role) {
        boolean result = rbacService.createRole(role);
        return result ? JsonResult.success("创建成功") : JsonResult.error("创建失败");
    }

    /**
     * 更新角色
     * @param id 角色ID
     * @param role 角色信息
     * @return 更新结果
     */
    @AuditLog(title = "更新角色", businessType = 0)
    @PutMapping("/roles/{id}")
    public JsonResult updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        boolean result = rbacService.updateRole(role);
        return result ? JsonResult.success("更新成功") : JsonResult.error("更新失败");
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    @AuditLog(title = "删除角色", businessType = 0)
    @DeleteMapping("/roles/{id}")
    public JsonResult deleteRole(@PathVariable Long id) {
        boolean result = rbacService.deleteRole(id);
        return result ? JsonResult.success("删除成功") : JsonResult.error("删除失败");
    }

    // ==================== 角色权限分配 ====================

    /**
     * 获取角色的权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @AuditLog(title = "查询角色权限", businessType = 0)
    @GetMapping("/roles/{roleId}/permissions")
    public JsonResult getRolePermissionIds(@PathVariable Long roleId) {
        List<Long> permissionIds = rbacService.getRolePermissionIds(roleId);
        return JsonResult.success(permissionIds);
    }

    /**
     * 批量分配权限给角色
     * @param roleId 角色ID
     * @param params 权限ID列表
     * @return 分配结果
     */
    @AuditLog(title = "分配角色权限", businessType = 0)
    @PostMapping("/roles/{roleId}/permissions")
    public JsonResult assignPermissionsToRole(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> params) {
        List<Long> permissionIds = params.get("permissionIds");
        boolean result = rbacService.assignPermissionsToRole(roleId, permissionIds);
        return result ? JsonResult.success("权限分配成功") : JsonResult.error("权限分配失败");
    }

    /**
     * 添加单个权限到角色
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 添加结果
     */
    @AuditLog(title = "添加角色权限", businessType = 0)
    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    public JsonResult addPermissionToRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        boolean result = rbacService.addPermissionToRole(roleId, permissionId);
        return result ? JsonResult.success("添加权限成功") : JsonResult.error("添加权限失败");
    }

    /**
     * 移除角色的单个权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 移除结果
     */
    @AuditLog(title = "移除角色权限", businessType = 0)
    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    public JsonResult removePermissionFromRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        boolean result = rbacService.removePermissionFromRole(roleId, permissionId);
        return result ? JsonResult.success("移除权限成功") : JsonResult.error("移除权限失败");
    }

    // ==================== 用户角色分配 ====================

    /**
     * 获取用户的角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    @AuditLog(title = "查询用户角色", businessType = 0)
    @GetMapping("/users/{userId}/roles")
    public JsonResult getUserRoles(@PathVariable Long userId) {
        List<SysRole> roles = rbacService.getUserRoles(userId);
        return JsonResult.success(roles);
    }

    /**
     * 获取用户的角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @AuditLog(title = "查询用户角色ID", businessType = 0)
    @GetMapping("/users/{userId}/role-ids")
    public JsonResult getUserRoleIds(@PathVariable Long userId) {
        List<Long> roleIds = rbacService.getUserRoleIds(userId);
        return JsonResult.success(roleIds);
    }

    /**
     * 批量分配角色给用户
     * @param userId 用户ID
     * @param params 角色ID列表
     * @return 分配结果
     */
    @AuditLog(title = "分配用户角色", businessType = 0)
    @PostMapping("/users/{userId}/roles")
    public JsonResult assignRolesToUser(@PathVariable Long userId, @RequestBody Map<String, List<Long>> params) {
        List<Long> roleIds = params.get("roleIds");
        boolean result = rbacService.assignRolesToUser(userId, roleIds);
        return result ? JsonResult.success("角色分配成功") : JsonResult.error("角色分配失败");
    }

    /**
     * 添加单个角色到用户
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 添加结果
     */
    @AuditLog(title = "添加用户角色", businessType = 0)
    @PostMapping("/users/{userId}/roles/{roleId}")
    public JsonResult addRoleToUser(@PathVariable Long userId, @PathVariable Long roleId) {
        boolean result = rbacService.addRoleToUser(userId, roleId);
        return result ? JsonResult.success("添加角色成功") : JsonResult.error("添加角色失败");
    }

    /**
     * 移除用户的单个角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 移除结果
     */
    @AuditLog(title = "移除用户角色", businessType = 0)
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public JsonResult removeRoleFromUser(@PathVariable Long userId, @PathVariable Long roleId) {
        boolean result = rbacService.removeRoleFromUser(userId, roleId);
        return result ? JsonResult.success("移除角色成功") : JsonResult.error("移除角色失败");
    }

    // ==================== 用户权限查询 ====================

    /**
     * 获取用户的权限编码列表
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @AuditLog(title = "查询用户权限", businessType = 0)
    @GetMapping("/users/{userId}/permissions")
    public JsonResult getUserPermissionCodes(@PathVariable Long userId) {
        List<String> permissionCodes = rbacService.getUserPermissionCodes(userId);
        return JsonResult.success(permissionCodes);
    }

    /**
     * 检查用户是否拥有某权限
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    @AuditLog(title = "检查用户权限", businessType = 0)
    @GetMapping("/users/{userId}/permissions/check")
    public JsonResult checkUserPermission(@PathVariable Long userId, @RequestParam String permissionCode) {
        boolean hasPermission = rbacService.checkUserPermission(userId, permissionCode);
        return JsonResult.success(hasPermission);
    }

    // ==================== 权限管理 ====================

    /**
     * 获取所有权限列表
     * @return 权限列表
     */
    @AuditLog(title = "查询权限列表", businessType = 0)
    @GetMapping("/permissions")
    public JsonResult getAllPermissions() {
        List<SysPermission> permissions = rbacService.getAllPermissions();
        return JsonResult.success(permissions);
    }

    /**
     * 获取权限树结构
     * @return 权限树结构
     */
    @AuditLog(title = "查询权限树", businessType = 0)
    @GetMapping("/permissions/tree")
    public JsonResult getPermissionTree() {
        List<Map<String, Object>> permissionTree = rbacService.getPermissionTree();
        return JsonResult.success(permissionTree);
    }

    /**
     * 按类型获取权限列表
     * @param type 资源类型
     * @return 权限列表
     */
    @AuditLog(title = "按类型查询权限", businessType = 0)
    @GetMapping("/permissions/type/{type}")
    public JsonResult getPermissionsByType(@PathVariable String type) {
        List<SysPermission> permissions = rbacService.getPermissionsByType(type);
        return JsonResult.success(permissions);
    }
}