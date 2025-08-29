package com.xypai.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 🎯 统一响应结果 - MVP版本
 * <p>
 * 设计原则：
 * - 简单统一
 * - 前端友好
 * - 易于调试
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;

    /**
     * 响应数据
     */
    private final T data;

    /**
     * 响应时间
     */
    private final LocalDateTime timestamp;

    /**
     * 请求ID（用于追踪）
     */
    private final String requestId;

    private Result(Integer code, String message, T data, String requestId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.requestId = requestId;
    }

    // ========================================
    // 成功响应
    // ========================================

    /**
     * ✅ 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null, null);
    }

    /**
     * ✅ 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, null);
    }

    /**
     * ✅ 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data, null);
    }

    // ========================================
    // 失败响应
    // ========================================

    /**
     * ❌ 失败响应
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null, null);
    }

    /**
     * ❌ 失败响应（带错误码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null, null);
    }

    /**
     * ❌ 失败响应（带请求ID）
     */
    public static <T> Result<T> error(Integer code, String message, String requestId) {
        return new Result<>(code, message, null, requestId);
    }

    // ========================================
    // 业务异常响应
    // ========================================

    /**
     * ⚠️ 参数错误
     */
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message, null, null);
    }

    /**
     * 🔒 未授权
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null, null);
    }

    /**
     * 🚫 禁止访问
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null, null);
    }

    /**
     * 🔍 未找到
     */
    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message, null, null);
    }

    // ========================================
    // 便捷方法
    // ========================================

    /**
     * 🔍 是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }

    /**
     * 🔍 是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}
