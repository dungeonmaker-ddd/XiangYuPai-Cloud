package com.xypai.sms.common.exception;

/**
 * Exception: 业务异常基类
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Exception: 模板不存在异常
     */
    public static class TemplateNotFoundException extends BusinessException {
        public TemplateNotFoundException(String templateCode) {
            super("TEMPLATE_NOT_FOUND", "模板不存在: " + templateCode);
        }
    }

    /**
     * Exception: 模板已存在异常
     */
    public static class TemplateAlreadyExistsException extends BusinessException {
        public TemplateAlreadyExistsException(String templateCode) {
            super("TEMPLATE_ALREADY_EXISTS", "模板已存在: " + templateCode);
        }
    }

    /**
     * Exception: 模板不可用异常
     */
    public static class TemplateNotAvailableException extends BusinessException {
        public TemplateNotAvailableException(String templateCode, String status) {
            super("TEMPLATE_NOT_AVAILABLE",
                    String.format("模板不可用: %s, 状态: %s", templateCode, status));
        }
    }

    /**
     * Exception: 短信发送异常
     */
    public static class SmsSendException extends BusinessException {
        public SmsSendException(String message) {
            super("SMS_SEND_ERROR", message);
        }

        public SmsSendException(String message, Throwable cause) {
            super("SMS_SEND_ERROR", message, cause);
        }
    }

    /**
     * Exception: 渠道不可用异常
     */
    public static class ChannelUnavailableException extends BusinessException {
        public ChannelUnavailableException(String channel) {
            super("CHANNEL_UNAVAILABLE", "渠道不可用: " + channel);
        }
    }

    /**
     * Exception: 频率限制异常
     */
    public static class RateLimitException extends BusinessException {
        public RateLimitException(String message) {
            super("RATE_LIMIT_EXCEEDED", message);
        }
    }
}
