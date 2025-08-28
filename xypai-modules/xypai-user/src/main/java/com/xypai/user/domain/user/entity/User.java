package com.xypai.user.domain.user.entity;

import com.xypai.user.domain.user.valueobject.UserId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 👤 用户实体 - 纯领域对象
 * <p>
 * 不包含任何基础设施关注点，仅包含业务逻辑
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class User {

    // ========================================
    // 实体标识
    // ========================================

    private final UserId userId;

    // ========================================
    // 基础信息
    // ========================================

    private String mobile;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthDate;
    private Integer status;
    private String clientType;

    // ========================================
    // 详细资料
    // ========================================

    private String realName;
    private String email;
    private String wechat;
    private String occupation;
    private String location;
    private String bio;
    private String interests;
    private BigDecimal height;
    private BigDecimal weight;

    // ========================================
    // 系统设置
    // ========================================

    private Integer notificationPush;
    private Integer privacyLevel;
    private String language;

    // ========================================
    // 时间字段
    // ========================================

    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;
    private LocalDateTime updateTime;

    // ========================================
    // 构造器
    // ========================================

    public User(UserId userId) {
        this.userId = userId; // 允许为null，在创建新用户时由基础设施层设置
    }

    // ========================================
    // 工厂方法
    // ========================================

    /**
     * 🔨 创建新用户
     */
    public static User create(
            UserId userId,
            String mobile,
            String nickname,
            String clientType
    ) {
        var user = new User(userId);
        user.mobile = Objects.requireNonNull(mobile, "手机号不能为空");
        user.nickname = Objects.requireNonNull(nickname, "昵称不能为空");
        user.clientType = Objects.requireNonNull(clientType, "客户端类型不能为空");
        user.status = 1; // 正常状态
        user.gender = 0; // 未知
        user.notificationPush = 1;
        user.privacyLevel = 1;
        user.language = "zh-CN";
        user.registerTime = LocalDateTime.now();
        user.updateTime = LocalDateTime.now();

        return user;
    }

    // ========================================
    // 业务方法
    // ========================================

    /**
     * 📝 更新基础信息
     */
    public void updateBasicInfo(String username, String nickname, String avatar) {
        if (username != null && !username.trim().isEmpty()) {
            validateUsername(username);
            this.username = username;
        }

        if (nickname != null && !nickname.trim().isEmpty()) {
            validateNickname(nickname);
            this.nickname = nickname;
        }

        if (avatar != null) {
            this.avatar = avatar;
        }

        this.updateTime = LocalDateTime.now();
    }

    /**
     * 👤 更新个人信息
     */
    public void updatePersonalInfo(Integer gender, LocalDate birthDate) {
        if (gender != null) {
            validateGender(gender);
            this.gender = gender;
        }

        if (birthDate != null) {
            validateBirthDate(birthDate);
            this.birthDate = birthDate;
        }

        this.updateTime = LocalDateTime.now();
    }

    /**
     * 🔓 启用用户
     */
    public void enable() {
        this.status = 1;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 🔒 禁用用户
     */
    public void disable() {
        this.status = 0;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 🔄 更新最后登录时间
     */
    public void updateLastLogin() {
        this.lastLoginTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 🆔 设置用户ID（仅供基础设施层调用）
     * 用于新用户创建后设置生成的ID
     */
    public User withUserId(UserId userId) {
        if (this.userId != null) {
            throw new IllegalStateException("用户ID已设置，不能重复设置");
        }
        Objects.requireNonNull(userId, "用户ID不能为空");

        // 由于userId是final，我们需要创建一个新的User实例
        var newUser = new User(userId);
        newUser.mobile = this.mobile;
        newUser.username = this.username;
        newUser.nickname = this.nickname;
        newUser.avatar = this.avatar;
        newUser.gender = this.gender;
        newUser.birthDate = this.birthDate;
        newUser.status = this.status;
        newUser.clientType = this.clientType;
        newUser.realName = this.realName;
        newUser.email = this.email;
        newUser.wechat = this.wechat;
        newUser.occupation = this.occupation;
        newUser.location = this.location;
        newUser.bio = this.bio;
        newUser.interests = this.interests;
        newUser.height = this.height;
        newUser.weight = this.weight;
        newUser.notificationPush = this.notificationPush;
        newUser.privacyLevel = this.privacyLevel;
        newUser.language = this.language;
        newUser.registerTime = this.registerTime;
        newUser.lastLoginTime = this.lastLoginTime;
        newUser.updateTime = this.updateTime;

        return newUser;
    }

    // ========================================
    // 业务规则验证
    // ========================================

    private void validateUsername(String username) {
        if (username.length() > 30) {
            throw new IllegalArgumentException("用户名长度不能超过30字符");
        }
        if (!username.matches("^[a-zA-Z0-9_]{3,30}$")) {
            throw new IllegalArgumentException("用户名只能包含字母、数字和下划线，长度3-30字符");
        }
    }

    private void validateNickname(String nickname) {
        if (nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        if (nickname.length() > 30) {
            throw new IllegalArgumentException("昵称长度不能超过30字符");
        }
    }

    private void validateGender(Integer gender) {
        if (gender < 0 || gender > 2) {
            throw new IllegalArgumentException("性别值只能是 0(未知)、1(男) 或 2(女)");
        }
    }

    private void validateBirthDate(LocalDate birthDate) {
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("生日不能是未来日期");
        }
        var age = LocalDate.now().getYear() - birthDate.getYear();
        if (age > 150) {
            throw new IllegalArgumentException("年龄不能超过150岁");
        }
    }

    // ========================================
    // 查询方法
    // ========================================

    /**
     * 检查用户是否正常
     */
    public boolean isActive() {
        return status != null && status == 1;
    }

    /**
     * 检查用户是否已禁用
     */
    public boolean isDisabled() {
        return status != null && status == 0;
    }

    /**
     * 检查是否有用户名
     */
    public boolean hasUsername() {
        return username != null && !username.trim().isEmpty();
    }

    /**
     * 检查是否成年人
     */
    public boolean isAdult() {
        if (birthDate == null) return false;
        return LocalDate.now().getYear() - birthDate.getYear() >= 18;
    }

    /**
     * 检查是否有有效的用户ID
     */
    public boolean hasValidUserId() {
        return userId != null && userId.value() != null;
    }

    // ========================================
    // Getters（只读访问）
    // ========================================

    public UserId getUserId() {
        return userId;
    }

    public String getMobile() {
        return mobile;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public Integer getGender() {
        return gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Integer getStatus() {
        return status;
    }

    public String getClientType() {
        return clientType;
    }

    public String getRealName() {
        return realName;
    }

    public String getEmail() {
        return email;
    }

    public String getWechat() {
        return wechat;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getLocation() {
        return location;
    }

    public String getBio() {
        return bio;
    }

    public String getInterests() {
        return interests;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public Integer getNotificationPush() {
        return notificationPush;
    }

    public Integer getPrivacyLevel() {
        return privacyLevel;
    }

    public String getLanguage() {
        return language;
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    // ========================================
    // Object方法重写
    // ========================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", mobile='" + mobile + '\'' +
                ", nickname='" + nickname + '\'' +
                ", status=" + status +
                '}';
    }
}
