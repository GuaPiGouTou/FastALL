package org.example.ecmo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ecmo.entity.ApiCallLog;
import org.example.ecmo.mapper.ApiCallLogMapper;
import org.example.ecmo.service.ApiCallLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class ApiCallLogServiceImpl extends ServiceImpl<ApiCallLogMapper, ApiCallLog> implements ApiCallLogService {

    @Autowired
    private ApiCallLogMapper apiCallLogMapper;

    @Override
    public void saveLog(ApiCallLog log) {
        save(log);
    }

    @Override
    public int getTodayCallCount() {
        return apiCallLogMapper.getTodayCallCount();
    }

    @Override
    public int getWeeklyCallCount() {
        return apiCallLogMapper.getWeeklyCallCount();
    }

    @Override
    public List<Map<String, Object>> getTopCalledApis() {
        return apiCallLogMapper.getTopCalledApis();
    }

    @Override
    public List<Map<String, Object>> getCallTrend() {
        return apiCallLogMapper.getCallTrend();
    }
}
