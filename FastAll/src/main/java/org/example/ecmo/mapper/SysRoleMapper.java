package org.example.ecmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.ecmo.entity.SysRole;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 获取所有角色列表
     * @return 角色列表
     */
    @Select("SELECT id, role_name, role_code, description, status, create_time FROM sys_role WHERE status = 1 ORDER BY create_time DESC")
    List<SysRole> getAllRoles();

    /**
     * 根据用户ID获取角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.id, r.role_name, r.role_code, r.description, r.status, r.create_time FROM sys_role r LEFT JOIN sys_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId} AND r.status = 1")
    List<SysRole> getRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID获取角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> getRoleIdsByUserId(@Param("userId") Long userId);
}