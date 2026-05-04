package com.example.dhcpms.common;

import lombok.Data;

/**
 * 全局统一返回结果类
 * 所有接口返回数据都通过该类封装，确保响应格式统一
 */
@Data
public class Result<T> {
    // 响应码：200成功，401未授权，403权限不足，500系统异常
    private Integer code;
    // 响应消息（前端展示/日志记录用）
    private String msg;
    // 响应数据（接口核心返回内容）
    private T data;

    // 私有构造方法，禁止外部直接实例化，统一通过静态方法创建
    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ====================== 成功响应 ======================

    /**
     * 成功返回（带数据，默认消息）
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功返回（自定义消息，不带数据）
     * @param msg 自定义成功消息
     * @param <T> 数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> success(String msg) {
        return new Result<>(200, msg, null);
    }

    /**
     * 成功返回（自定义消息+数据）
     * @param msg 自定义成功消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    // ====================== 失败响应 ======================

    /**
     * 失败返回（默认500码，自定义消息，不带数据）
     * @param msg 自定义失败消息
     * @param <T> 数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    /**
     * 失败返回（自定义码+消息，不带数据）
     * @param code 自定义错误码
     * @param msg 自定义失败消息
     * @param <T> 数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    // ====================== 权限相关响应 ======================

    /**
     * 权限不足返回（403码，自定义消息）
     * @param msg 权限不足提示消息
     * @param <T> 数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> forbidden(String msg) {
        return new Result<>(403, msg, null);
    }

    /**
     * 未授权返回（401码，自定义消息）
     * @param msg 未授权提示消息
     * @param <T> 数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(401, msg, null);
    }
}