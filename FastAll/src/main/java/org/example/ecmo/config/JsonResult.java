package org.example.ecmo.config;
public class JsonResult<T> {
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private T data;
    private int code;
    private String msg;


    /**
     * 若没有数据返回，默认状态码为0，提示信息为：操作成功！
     */
    public JsonResult() {
        this.code = 0;
        this.msg = "操作成功！";
    }

    /**
     * 若没有数据返回，可以人为指定状态码和提示信息
     * @param code
     * @param msg
     */
    public JsonResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 有数据返回时，状态码为0，默认提示信息为：操作成功！
     * @param data
     */
    public JsonResult(T data) {
        this.data = data;
        this.code = 0;
        this.msg = "操作成功！";
    }

    /**
     * 有数据返回，状态码为0，人为指定提示信息
     * @param data
     * @param msg
     */
    public JsonResult(T data, String msg) {
        this.data = data;
        this.code = 0;
        this.msg = msg;
    }
    /**
     * 有数据返回，状态码为0，人为指定提示信息
     * @param data
     * @param msg
     */
    public JsonResult(int code,T data, String msg) {
        this.data = data;
        this.code = code;
        this.msg = msg;
    }

    /**
     * 静态方法：返回成功响应
     * @param data 响应数据
     * @param <T> 数据类型
     * @return JsonResult
     */
    public static <T> JsonResult<T> success(T data) {
        return new JsonResult<>(data);
    }

    /**
     * 静态方法：返回成功响应
     * @param msg 提示信息
     * @param <T> 数据类型
     * @return JsonResult
     */
    public static <T> JsonResult<T> success(String msg) {
        return new JsonResult<>(0, msg);
    }

    /**
     * 静态方法：返回错误响应
     * @param msg 错误信息
     * @param <T> 数据类型
     * @return JsonResult
     */
    public static <T> JsonResult<T> error(String msg) {
        return new JsonResult<>(1, msg);
    }

}
