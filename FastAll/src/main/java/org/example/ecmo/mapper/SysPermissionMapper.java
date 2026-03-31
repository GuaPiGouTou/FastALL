package org.example.ecmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.ecmo.entity.SysPermission;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 获取所有权限列表
     * @return 权限列表
     */
    @Select("SELECT id, permission_name, permission_code, resource_type, resource_url, parent_id, icon, sort_order, status, create_time FROM sys_permission WHERE status = 1 ORDER BY sort_order ASC")
    List<SysPermission> getAllPermissions();

    /**
     * 根据角色ID获取权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.id, p.permission_name, p.permission_code, p.resource_type, p.resource_url, p.parent_id, p.icon, p.sort_order, p.status, p.create_time FROM sys_permission p LEFT JOIN sys_role_permission rp ON p.id = rp.permission_id WHERE rp.role_id = #{roleId} AND p.status = 1")
    List<SysPermission> getPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID获取权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> getPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID获取权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT p.id, p.permission_name, p.permission_code, p.resource_type, p.resource_url, p.parent_id, p.icon, p.sort_order, p.status, p.create_time FROM sys_permission p LEFT JOIN sys_role_permission rp ON p.id = rp.permission_id LEFT JOIN sys_user_role ur ON rp.role_id = ur.role_id WHERE ur.user_id = #{userId} AND p.status = 1")
    List<SysPermission> getPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID获取权限编码列表
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p LEFT JOIN sys_role_permission rp ON p.id = rp.permission_id LEFT JOIN sys_user_role ur ON rp.role_id = ur.role_id WHERE ur.user_id = #{userId} AND p.status = 1")
    List<String> getPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 按类型获取权限列表
     * @param type 资源类型
     * @return 权限列表
     */
    @Select("SELECT id, permission_name, permission_code, resource_type, resource_url, parent_id, icon, sort_order, status, create_time FROM sys_permission WHERE resource_type = #{type} AND status = 1 ORDER BY sort_order ASC")
    List<SysPermission> getPermissionsByType(@Param("type") String type);
}