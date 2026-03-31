package org.example.ecmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface CrudMapper extends BaseMapper<Map<String, Object>> {

    int getTableCount();

    long getTotalRecords();

    String getStorageUsed();

    int getWeeklyNewRecords();

    List<Map<String, Object>> getDataRanking();

    void createTable(@Param("tableName") String tableName, @Param("moduleName") String moduleName, @Param("description") String description, @Param("fields") List<Map<String, Object>> fields);

    /**
     * 获取表结构定义
     * @param tableName 表名
     * @return 字段配置列表
     */
    @Select("SELECT id, table_name, prop, label, ui_type, options, is_show_in_list, sort_order FROM sys_field_config WHERE table_name = #{tableName} ORDER BY sort_order ASC")
    List<Map<String, Object>> getTableSchema(@Param("tableName") String tableName);

    /**
     * 获取数据列表
     * @param tableName 表名
     * @param whereSql WHERE 条件
     * @param orderSql ORDER BY 条件
     * @param limitSql LIMIT 条件
     * @return 数据列表
     */
    @Select("SELECT * FROM ${tableName} ${whereSql} ${orderSql} ${limitSql}")
    List<Map<String, Object>> getTableData(@Param("tableName") String tableName, @Param("whereSql") String whereSql, @Param("orderSql") String orderSql, @Param("limitSql") String limitSql);

    /**
     * 获取数据总数
     * @param tableName 表名
     * @param whereSql WHERE 条件
     * @return 数据总数
     */
    @Select("SELECT COUNT(*) FROM ${tableName} ${whereSql}")
    long getTableDataCount(@Param("tableName") String tableName, @Param("whereSql") String whereSql);

    /**
     * 保存数据
     * @param tableName 表名
     * @param data 数据
     * @return 影响行数
     */
    int saveData(@Param("tableName") String tableName, @Param("data") Map<String, Object> data);

    /**
     * 更新数据
     * @param tableName 表名
     * @param data 数据
     * @param id 主键
     * @return 影响行数
     */
    int updateData(@Param("tableName") String tableName, @Param("data") Map<String, Object> data, @Param("id") Long id);

    /**
     * 删除数据
     * @param tableName 表名
     * @param id 主键
     * @return 影响行数
     */
    @Update("DELETE FROM ${tableName} WHERE id = #{id}")
    int deleteData(@Param("tableName") String tableName, @Param("id") Long id);

    /**
     * 新增字段配置
     * @param tableName 表名
     * @param prop 字段名
     * @param label 字段标签
     * @param uiType UI类型
     * @param options 选项配置
     * @param isShowInList 是否在列表中显示
     * @param sortOrder 排序
     * @return 影响行数
     */
    int addFieldConfig(@Param("tableName") String tableName, @Param("prop") String prop, @Param("label") String label, @Param("uiType") String uiType, @Param("options") String options, @Param("isShowInList") Integer isShowInList, @Param("sortOrder") Integer sortOrder);

    /**
     * 修改字段配置
     * @param tableName 表名
     * @param prop 字段名
     * @param label 字段标签
     * @param uiType UI类型
     * @param options 选项配置
     * @param isShowInList 是否在列表中显示
     * @param sortOrder 排序
     * @return 影响行数
     */
    int updateFieldConfig(@Param("tableName") String tableName, @Param("prop") String prop, @Param("label") String label, @Param("uiType") String uiType, @Param("options") String options, @Param("isShowInList") Integer isShowInList, @Param("sortOrder") Integer sortOrder);
}