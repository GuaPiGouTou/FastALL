package org.example.ecmo.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.ecmo.entity.SysLoginLog;
import org.example.ecmo.entity.SysOperLog;

public interface LogService  {
    void recordLoginLog(SysLoginLog loginLog);

    // 参数：当前页，页大小，筛选条件（用户名/标题，状态）
    Page<SysLoginLog> findLoginLogPage(int current, int size, String username, String status);
    Page<SysOperLog> findOperLogPage(int current, int size, String title, String status);
}
