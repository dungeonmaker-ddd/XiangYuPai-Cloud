package com.xypai.user.interfaces.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 详细用户响应（增强单表版本）
 * 支持Builder模式，方便构建和测试
 *
 * @author XyPai
 */
@Builder
public record DetailedUserResponse(
        // 基础信息
        Long userId,
        String mobile,
        String username,
        String nickname,
        String avatar,
        Integer gender,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        Integer status,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime registerTime,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime lastLoginTime,

        String clientType,

        // 详细资料（来自增强的单表）
        String realName,
        String email,
        String wechat,
        String occupation,
        String location,
        String bio,
        String interests,
        BigDecimal height,
        BigDecimal weight,
        Integer notificationPush,
        Integer privacyLevel,
        String language,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createTime,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updateTime
) {

    /**
     * 获取性别描述
     */
    public String getGenderDescription() {
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
    }

    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        return status == 1 ? "正常" : "禁用";
    }

    /**
     * 脱敏手机号（中间4位显示为*）
     */
    public String getMaskedMobile() {
        if (mobile == null || mobile.length() != 11) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    /**
     * 获取年龄（精确计算）
     */
    public Integer getAge() {
        if (birthDate == null) return null;

        LocalDate now = LocalDate.now();
        int age = now.getYear() - birthDate.getYear();

        // 如果今年的生日还没到，年龄减1
        if (now.getMonthValue() < birthDate.getMonthValue() ||
                (now.getMonthValue() == birthDate.getMonthValue() && now.getDayOfMonth() < birthDate.getDayOfMonth())) {
            age--;
        }

        return age;
    }

    /**
     * 获取BMI值
     */
    public BigDecimal getBMI() {
        if (height == null || weight == null ||
                height.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        // BMI = 体重(kg) / (身高(m))^2
        BigDecimal heightInMeter = height.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return weight.divide(heightInMeter.multiply(heightInMeter), 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取BMI状态描述
     */
    public String getBMIStatus() {
        BigDecimal bmi = getBMI();
        if (bmi == null) return "未设置";

        if (bmi.compareTo(new BigDecimal("18.5")) < 0) {
            return "偏瘦";
        } else if (bmi.compareTo(new BigDecimal("24")) < 0) {
            return "正常";
        } else if (bmi.compareTo(new BigDecimal("28")) < 0) {
            return "偏胖";
        } else {
            return "肥胖";
        }
    }

    /**
     * 获取兴趣爱好列表
     */
    public String[] getInterestList() {
        if (interests == null || interests.trim().isEmpty()) {
            return new String[0];
        }
        return interests.split(",");
    }

    /**
     * 检查资料完整度（百分比）
     */
    public Integer getProfileCompleteness() {
        int totalFields = 12; // 主要字段总数
        int filledFields = 0;

        if (nickname != null && !nickname.trim().isEmpty()) filledFields++;
        if (avatar != null && !avatar.trim().isEmpty()) filledFields++;
        if (gender != null && gender > 0) filledFields++;
        if (birthDate != null) filledFields++;
        if (realName != null && !realName.trim().isEmpty()) filledFields++;
        if (email != null && !email.trim().isEmpty()) filledFields++;
        if (occupation != null && !occupation.trim().isEmpty()) filledFields++;
        if (location != null && !location.trim().isEmpty()) filledFields++;
        if (wechat != null && !wechat.trim().isEmpty()) filledFields++;
        if (bio != null && !bio.trim().isEmpty()) filledFields++;
        if (height != null) filledFields++;
        if (weight != null) filledFields++;

        return (filledFields * 100) / totalFields;
    }
}
