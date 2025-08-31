package com.xypai.sms.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception: 全局异常处理器
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Exception: 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("Exception: 业务异常 - {}: {}", e.getErrorCode(), e.getMessage());

        HttpStatus status = switch (e.getErrorCode()) {
            case "TEMPLATE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "TEMPLATE_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            case "RATE_LIMIT_EXCEEDED" -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.BAD_REQUEST;
        };

        ErrorResponse error = ErrorResponse.of(e.getErrorCode(), e.getMessage());
        return ResponseEntity.status(status).body(error);
    }

    /**
     * Exception: 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e) {
        log.warn("Exception: 参数验证失败 - {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.of("VALIDATION_ERROR", "参数验证失败", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Exception: 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Exception: 非法参数 - {}", e.getMessage());

        ErrorResponse error = ErrorResponse.of("ILLEGAL_ARGUMENT", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Exception: 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Exception: 系统异常 - {}", e.getMessage(), e);

        ErrorResponse error = ErrorResponse.of("INTERNAL_ERROR", "系统内部错误，请稍后重试");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Exception: 错误响应记录
     */
    public record ErrorResponse(
            String code,
            String message,
            Map<String, Object> details,
            LocalDateTime timestamp
    ) {

        public static ErrorResponse of(String code, String message) {
            return new ErrorResponse(code, message, null, LocalDateTime.now());
        }

        public static ErrorResponse of(String code, String message, Map<String, ?> details) {
            Map<String, Object> detailsMap = new HashMap<>();
            if (details != null) {
                detailsMap.putAll(details);
            }
            return new ErrorResponse(code, message, detailsMap, LocalDateTime.now());
        }
    }
}
