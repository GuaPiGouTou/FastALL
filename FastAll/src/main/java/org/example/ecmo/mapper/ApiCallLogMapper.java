package org.example.ecmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.ecmo.entity.ApiCallLog;
import java.util.List;
import java.util.Map;

@Mapper
public interface ApiCallLogMapper extends BaseMapper<ApiCallLog> {

    @Select("SELECT COUNT(*) FROM api_call_log WHERE DATE(create_time) = CURDATE()")
    int getTodayCallCount();

    @Select("SELECT COUNT(*) FROM api_call_log WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
    int getWeeklyCallCount();

    @Select("SELECT api_path, COUNT(*) as count FROM api_call_log GROUP BY api_path ORDER BY count DESC LIMIT 10")
    List<Map<String, Object>> getTopCalledApis();

    @Select("SELECT DATE(create_time) as date, COUNT(*) as count FROM api_call_log WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) GROUP BY DATE(create_time)")
    List<Map<String, Object>> getCallTrend();
}
