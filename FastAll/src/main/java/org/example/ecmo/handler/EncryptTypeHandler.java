package org.example.ecmo.handler;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.util.StringUtils;

import java.sql.*;

public class EncryptTypeHandler extends BaseTypeHandler<String> {

    // AES 密钥必须是 16 位
    private static final String AES_KEY = "ecmo_aes_key_123";
    private static final AES aes = SecureUtil.aes(AES_KEY.getBytes());

    /**
     * 写入数据库前：明文 → 加密成十六进制密文
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        String s = aes.encryptHex(parameter);
        ps.setString(i, s);

    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }

    /**
     * 从数据库读出后：密文 → 解密成明文
     * 需要兼容老数据（老数据是明文，AES 无法解密会抛异常）
     */
    private String decrypt(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        try {
          return  aes.decryptStr( value);
        } catch (Exception e) {

            return value;
        }
    }
}
