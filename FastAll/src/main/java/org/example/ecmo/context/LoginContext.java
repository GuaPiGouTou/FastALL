package org.example.ecmo.context;

import cn.hutool.crypto.symmetric.AES;
import lombok.Data;
import org.example.ecmo.entity.SysLoginLog;
import org.example.ecmo.entity.SysUser;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Data
public class LoginContext {
    private String username;
    private String password;
    private String captchaVerification;
    private String ipAddress;
    private SysLoginLog loginLog;
    private SysUser user;
    private String errorMessage;
    private HttpServletRequest request;
    private boolean shouldRecordFailedAttempt = false;

    private static final String AES_KEY = "ecmo_aes_key_123";
    private static final AES aes = new AES(AES_KEY.getBytes());

    public LoginContext(Map<String, String> params, HttpServletRequest request, String ipAddress) {
        this.username = params.get("username");
        String encryptedPassword = params.get("password");
        if (encryptedPassword != null && !encryptedPassword.isEmpty()) {
            try {
                this.password = aes.decryptStr(encryptedPassword);
            } catch (Exception e) {
                this.password = encryptedPassword;
            }
        }
        this.captchaVerification = params.get("captchaVerification");
        this.ipAddress = ipAddress;
        this.request = request;
        this.loginLog = new SysLoginLog();
        this.loginLog.setStatus(1);
        this.loginLog.setUsername(username);
        this.loginLog.setIpAddress(ipAddress);
    }

    public void setErrorMessage(String message) {
        this.errorMessage = message;
        this.loginLog.setMsg(message);
    }
}
