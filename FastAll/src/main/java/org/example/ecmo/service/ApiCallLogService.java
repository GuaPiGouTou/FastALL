package org.example.ecmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ecmo.entity.ApiCallLog;
import java.util.List;
import java.util.Map;

public interface ApiCallLogService extends IService<ApiCallLog> {

    void saveLog(ApiCallLog log);

    int getTodayCallCount();

    int getWeeklyCallCount();

    List<Map<String, Object>> getTopCalledApis();

    List<Map<String, Object>> getCallTrend();
}
