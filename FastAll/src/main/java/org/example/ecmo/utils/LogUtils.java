package org.example.ecmo.utils;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import org.example.ecmo.entity.SysLoginLog;
import org.example.ecmo.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
    public class LogUtils {
    public static  void setRests(HttpServletRequest request, SysLoginLog loginLog) {
        String uaString = request.getHeader("User-Agent");
        loginLog.setUserAgent(uaString);
        UserAgent ua = UserAgentUtil.parse(uaString);
        loginLog.setBrowser(ua.getBrowser().toString() + " " + ua.getVersion());
        loginLog.setOs(ua.getOs().toString());

        String deviceType = "PC";
        if (ua.isMobile()) deviceType = "Mobile";
        loginLog.setDeviceType(deviceType);
    }
}
