package org.example.ecmo.exception;

import org.example.ecmo.config.JsonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.multipart.MultipartException;

import java.sql.SQLException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public JsonResult handleException(Exception e) {
        log.error("系统异常: ", e);
        return new JsonResult(500, null, "系统错误: " + e.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    public JsonResult handleSQLException(SQLException e) {
        log.error("数据库异常: ", e);
        String message = "数据库操作失败";
        
        if (e.getMessage().contains("Duplicate entry")) {
            message = "数据已存在，请勿重复添加";
        } else if (e.getMessage().contains("doesn't exist")) {
            message = "表不存在";
        } else if (e.getMessage().contains("Access denied")) {
            message = "数据库访问权限不足";
        } else if (e.getMessage().contains("Connection refused")) {
            message = "数据库连接失败，请检查数据库服务是否启动";
        }
        
        return new JsonResult(500, null, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public JsonResult handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数异常: ", e);
        return new JsonResult(400, null, "参数错误: " + e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public JsonResult handleNullPointerException(NullPointerException e) {
        log.error("空指针异常: ", e);
        return new JsonResult(500, null, "数据为空，请检查输入");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public JsonResult handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("文件大小超限: ", e);
        return new JsonResult(400, null, "文件大小超过限制，请上传小于10MB的文件");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public JsonResult handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        log.error("缺少请求部分: ", e);
        return new JsonResult(400, null, "缺少文件参数: " + e.getRequestPartName());
    }

    @ExceptionHandler(MultipartException.class)
    public JsonResult handleMultipartException(MultipartException e) {
        log.error("文件上传异常: ", e);
        return new JsonResult(400, null, "文件上传失败: " + e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public JsonResult handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return new JsonResult(500, null, "操作失败: " + e.getMessage());
    }
}
