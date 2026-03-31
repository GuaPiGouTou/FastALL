package org.example.ecmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ecmo.mapper.CrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CrudService {

    @Autowired
    private CrudMapper crudMapper;

    public int getTableCount() {
        return crudMapper.getTableCount();
    }

    public long getTotalRecords() {
        return crudMapper.getTotalRecords();
    }

    public String getStorageUsed() {
        return crudMapper.getStorageUsed();
    }

    public int getWeeklyNewRecords() {
        return crudMapper.getWeeklyNewRecords();
    }

    public List<Map<String, Object>> getDataRanking() {
        return crudMapper.getDataRanking();
    }

    public void createTable(String tableName, String moduleName, String description, List<Map<String, Object>> fields) {
        crudMapper.createTable(tableName, moduleName, description, fields);
    }

    /**
     * 获取表结构定义
     * @param tableName 表名
     * @return 字段配置列表
     */
    public List<Map<String, Object>> getTableSchema(String tableName) {
        return crudMapper.getTableSchema(tableName);
    }

    /**
     * 获取数据列表
     * @param tableName 表名
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param sortField 排序字段
     * @param sortOrder 排序顺序
     * @return 数据列表和总数
     */
    public Map<String, Object> getTableData(String tableName, int page, int size, String keyword, String sortField, String sortOrder) {
        // 构建 WHERE 条件
        String whereSql = buildWhereSql(keyword);
        // 构建 ORDER BY 条件
        String orderSql = buildOrderSql(sortField, sortOrder);
        // 构建 LIMIT 条件
        String limitSql = buildLimitSql(page, size);

        // 获取数据列表
        List<Map<String, Object>> list = crudMapper.getTableData(tableName, whereSql, orderSql, limitSql);
        // 获取数据总数
        long total = crudMapper.getTableDataCount(tableName, whereSql);

        // 构建返回结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    /**
     * 保存数据
     * @param tableName 表名
     * @param data 数据
     * @return 保存结果
     */
    public boolean saveData(String tableName, Map<String, Object> data) {
        // 移除 id 字段，用于判断是新增还是修改
        Long id = (Long) data.remove("id");
        if (id == null || id == 0) {
            // 新增数据
            return crudMapper.saveData(tableName, data) > 0;
        } else {
            // 修改数据
            return crudMapper.updateData(tableName, data, id) > 0;
        }
    }

    /**
     * 删除数据
     * @param tableName 表名
     * @param id 主键
     * @return 删除结果
     */
    public boolean deleteData(String tableName, Long id) {
        return crudMapper.deleteData(tableName, id) > 0;
    }

    /**
     * 新增字段配置
     * @param tableName 表名
     * @param prop 字段名
     * @param label 字段标签
     * @param uiType UI类型
     * @param options 选项配置
     * @param isShowInList 是否在列表中显示
     * @param sortOrder 排序
     * @return 操作结果
     */
    public boolean addFieldConfig(String tableName, String prop, String label, String uiType, String options, Integer isShowInList, Integer sortOrder) {
        return crudMapper.addFieldConfig(tableName, prop, label, uiType, options, isShowInList, sortOrder) > 0;
    }

    /**
     * 修改字段配置
     * @param tableName 表名
     * @param prop 字段名
     * @param label 字段标签
     * @param uiType UI类型
     * @param options 选项配置
     * @param isShowInList 是否在列表中显示
     * @param sortOrder 排序
     * @return 操作结果
     */
    public boolean updateFieldConfig(String tableName, String prop, String label, String uiType, String options, Integer isShowInList, Integer sortOrder) {
        return crudMapper.updateFieldConfig(tableName, prop, label, uiType, options, isShowInList, sortOrder) > 0;
    }

    /**
     * 构建 WHERE 条件
     * @param keyword 搜索关键词
     * @return WHERE 条件
     */
    private String buildWhereSql(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return "";
        }
        return "WHERE name LIKE '%" + keyword + "%' OR description LIKE '%" + keyword + "%'";
    }

    /**
     * 构建 ORDER BY 条件
     * @param sortField 排序字段
     * @param sortOrder 排序顺序
     * @return ORDER BY 条件
     */
    private String buildOrderSql(String sortField, String sortOrder) {
        if (sortField == null || sortField.isEmpty()) {
            return "ORDER BY created_time DESC";
        }
        return "ORDER BY " + sortField + " " + ("desc".equals(sortOrder) ? "DESC" : "ASC");
    }

    /**
     * 构建 LIMIT 条件
     * @param page 页码
     * @param size 每页大小
     * @return LIMIT 条件
     */
    private String buildLimitSql(int page, int size) {
        int offset = (page - 1) * size;
        return "LIMIT " + offset + ", " + size;
    }
}