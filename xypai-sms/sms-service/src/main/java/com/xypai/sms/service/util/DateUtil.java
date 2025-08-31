package com.xypai.sms.service.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Util: 日期时间工具类
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public final class DateUtil {

    // 常用日期时间格式
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String ISO_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATETIME_PATTERN);
    private DateUtil() {
    }

    /**
     * Util: 获取当前日期
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Util: 获取当前日期时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Util: 格式化日期
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    /**
     * Util: 格式化日期时间
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    /**
     * Util: 格式化时间戳
     */
    public static String formatTimestamp(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIMESTAMP_FORMATTER) : null;
    }

    /**
     * Util: 格式化ISO日期时间
     */
    public static String formatIsoDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_DATETIME_FORMATTER) : null;
    }

    /**
     * Util: 解析日期
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("无效的日期格式: " + dateStr + "，请使用格式: " + DATE_PATTERN, e);
        }
    }

    /**
     * Util: 解析日期时间
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTimeStr.trim(), DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("无效的日期时间格式: " + dateTimeStr + "，请使用格式: " + DATETIME_PATTERN, e);
        }
    }

    /**
     * Util: 智能解析日期时间（支持多种格式）
     */
    public static LocalDateTime parseSmartDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        String cleaned = dateTimeStr.trim();

        // 尝试不同的格式
        DateTimeFormatter[] formatters = {
                DATETIME_FORMATTER,
                TIMESTAMP_FORMATTER,
                ISO_DATETIME_FORMATTER,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(cleaned, formatter);
            } catch (DateTimeParseException ignored) {
                // 继续尝试下一个格式
            }
        }

        throw new IllegalArgumentException("无法解析日期时间: " + dateTimeStr);
    }

    /**
     * Util: 计算日期差（天数）
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Util: 计算时间差（小时）
     */
    public static long hoursBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(startTime, endTime);
    }

    /**
     * Util: 计算时间差（分钟）
     */
    public static long minutesBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(startTime, endTime);
    }

    /**
     * Util: 计算时间差（秒）
     */
    public static long secondsBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(startTime, endTime);
    }

    /**
     * Util: 获取日期范围的开始时间（当天0点）
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    /**
     * Util: 获取日期范围的结束时间（当天23:59:59）
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59, 999999999) : null;
    }

    /**
     * Util: 检查日期是否在范围内
     */
    public static boolean isInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null) {
            return false;
        }

        if (startDate != null && date.isBefore(startDate)) {
            return false;
        }

        if (endDate != null && date.isAfter(endDate)) {
            return false;
        }

        return true;
    }

    /**
     * Util: 检查日期时间是否在范围内
     */
    public static boolean isInRange(LocalDateTime dateTime, LocalDateTime startTime, LocalDateTime endTime) {
        if (dateTime == null) {
            return false;
        }

        if (startTime != null && dateTime.isBefore(startTime)) {
            return false;
        }

        if (endTime != null && dateTime.isAfter(endTime)) {
            return false;
        }

        return true;
    }

    /**
     * Util: 获取本周开始日期（周一）
     */
    public static LocalDate startOfWeek() {
        return today().with(java.time.DayOfWeek.MONDAY);
    }

    /**
     * Util: 获取本月开始日期
     */
    public static LocalDate startOfMonth() {
        return today().withDayOfMonth(1);
    }

    /**
     * Util: 获取本年开始日期
     */
    public static LocalDate startOfYear() {
        return today().withDayOfYear(1);
    }

    /**
     * Util: 获取昨天
     */
    public static LocalDate yesterday() {
        return today().minusDays(1);
    }

    /**
     * Util: 获取明天
     */
    public static LocalDate tomorrow() {
        return today().plusDays(1);
    }

    /**
     * Util: 检查是否为今天
     */
    public static boolean isToday(LocalDate date) {
        return today().equals(date);
    }

    /**
     * Util: 检查是否为过去的日期
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(today());
    }

    /**
     * Util: 检查是否为未来的日期
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(today());
    }
}
