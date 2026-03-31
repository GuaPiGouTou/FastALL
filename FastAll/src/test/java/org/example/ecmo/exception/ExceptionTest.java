package org.example.ecmo.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void testBusinessExceptionWithMessage() {
        BusinessException exception = new BusinessException("测试异常");
        
        assertEquals("测试异常", exception.getMessage());
        assertEquals(500, exception.getCode());
    }

    @Test
    void testBusinessExceptionWithCodeAndMessage() {
        BusinessException exception = new BusinessException(400, "参数错误");
        
        assertEquals("参数错误", exception.getMessage());
        assertEquals(400, exception.getCode());
    }

    @Test
    void testAuthException() {
        AuthException exception = new AuthException("认证失败");
        
        assertEquals("认证失败", exception.getMessage());
        assertEquals(401, exception.getCode());
    }

    @Test
    void testValidationException() {
        ValidationException exception = new ValidationException("参数校验失败");
        
        assertEquals("参数校验失败", exception.getMessage());
        assertEquals(400, exception.getCode());
    }

    @Test
    void testFileStorageException() {
        FileStorageException exception = new FileStorageException("文件存储失败");
        
        assertEquals("文件存储失败", exception.getMessage());
        assertEquals(500, exception.getCode());
    }

    @Test
    void testFileStorageExceptionWithCause() {
        Exception cause = new RuntimeException("原始异常");
        FileStorageException exception = new FileStorageException("文件存储失败", cause);
        
        assertEquals("文件存储失败", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
