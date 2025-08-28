package com.xypai.user.domain.aggregate;

import com.xypai.user.domain.shared.DomainEvent;
import com.xypai.user.domain.shared.UserCreatedEvent;
import com.xypai.user.domain.shared.UserUpdatedEvent;
import com.xypai.user.domain.valueobject.UserId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 👤 用户聚合根 - DDD核心聚合
 * <p>
 * 负责用户基础信息管理和业务规则验证
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class UserAggregate {

    // ========================================
    // 聚合根标识
    // ========================================

    private final UserId userId;

    // ========================================
    // 基础信息
    // ========================================
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    private String mobile;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthDate;
    private Integer status;

    // ========================================
    // 详细资料
    // ========================================
    private String clientType;
    private String realName;
    private String email;
    private String wechat;
    private String occupation;
    private String location;
    private String bio;
    private String interests;
    private BigDecimal height;

    // ========================================
    // 系统字段
    // ========================================
    private BigDecimal weight;
    private Integer notificationPush;
    private Integer privacyLevel;
    private String language;
    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;

    // ========================================
    // 领域事件
    // ========================================
    private LocalDateTime updateTime;

    // ========================================
    // 构造器 - 私有，通过工厂方法创建
    // ========================================

    private UserAggregate(UserId userId) {
        this.userId = Objects.requireNonNull(userId, "用户ID不能为空");
    }

    // ========================================
    // 工厂方法
    // ========================================

    /**
     * 🔨 创建新用户聚合根
     */
    public static UserAggregate createUser(
            String mobile,
            String nickname,
            String clientType
    ) {
        // 验证必填字段
        Objects.requireNonNull(mobile, "手机号不能为空");
        Objects.requireNonNull(nickname, "昵称不能为空");
        Objects.requireNonNull(clientType, "客户端类型不能为空");

        // 验证业务规则
        validateMobile(mobile);
        validateNickname(nickname);
        validateClientType(clientType);

        // 创建聚合根（此时还没有ID，由基础设施层生成）
        var aggregate = new UserAggregate(null);
        aggregate.mobile = mobile;
        aggregate.nickname = nickname;
        aggregate.clientType = clientType;
        aggregate.status = 1; // 默认正常状态
        aggregate.gender = 0; // 默认未知
        aggregate.notificationPush = 1;
        aggregate.privacyLevel = 1;
        aggregate.language = "zh-CN";
        aggregate.registerTime = LocalDateTime.now();
        aggregate.updateTime = LocalDateTime.now();

        return aggregate;
    }

    /**
     * 🧪 为测试创建新用户聚合根（带临时ID）
     */
    public static UserAggregate createUserForTest(
            String mobile,
            String nickname,
            String clientType
    ) {
        // 验证必填字段
        Objects.requireNonNull(mobile, "手机号不能为空");
        Objects.requireNonNull(nickname, "昵称不能为空");
        Objects.requireNonNull(clientType, "客户端类型不能为空");

        // 验证业务规则
        validateMobile(mobile);
        validateNickname(nickname);
        validateClientType(clientType);

        // 创建聚合根（使用临时ID用于测试）
        var aggregate = new UserAggregate(UserId.of(1L));
        aggregate.mobile = mobile;
        aggregate.nickname = nickname;
        aggregate.clientType = clientType;
        aggregate.status = 1; // 默认正常状态
        aggregate.gender = 0; // 默认未知
        aggregate.notificationPush = 1;
        aggregate.privacyLevel = 1;
        aggregate.language = "zh-CN";
        aggregate.registerTime = LocalDateTime.now();
        aggregate.updateTime = LocalDateTime.now();

        // 标记为已创建并生成事件
        aggregate.markAsCreated();

        return aggregate;
    }

    /**
     * 🔄 从现有数据重建聚合根
     */
    public static UserAggregate fromExisting(
            Long userId,
            String mobile,
            String username,
            String nickname,
            String avatar,
            Integer gender,
            LocalDate birthDate,
            Integer status,
            String clientType,
            LocalDateTime registerTime,
            LocalDateTime lastLoginTime
    ) {
        var aggregate = new UserAggregate(UserId.of(userId));
        aggregate.mobile = mobile;
        aggregate.username = username;
        aggregate.nickname = nickname;
        aggregate.avatar = avatar;
        aggregate.gender = gender != null ? gender : 0;
        aggregate.birthDate = birthDate;
        aggregate.status = status != null ? status : 1;
        aggregate.clientType = clientType != null ? clientType : "app";
        aggregate.registerTime = registerTime;
        aggregate.lastLoginTime = lastLoginTime;
        aggregate.updateTime = LocalDateTime.now();

        return aggregate;
    }

    // ========================================
    // 业务方法
    // ========================================

    private static void validateMobile(String mobile) {
        if (!mobile.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
    }

    private static void validateNickname(String nickname) {
        if (nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        if (nickname.length() > 30) {
            throw new IllegalArgumentException("昵称长度不能超过30字符");
        }
    }

    private static void validateUsername(String username) {
        if (username != null && username.length() > 30) {
            throw new IllegalArgumentException("用户名长度不能超过30字符");
        }
    }

    private static void validateClientType(String clientType) {
        if (!List.of("web", "app", "mini").contains(clientType)) {
            throw new IllegalArgumentException("客户端类型只能是 web、app 或 mini");
        }
    }

    // ========================================
    // 业务规则验证
    // ========================================

    private static void validateGender(Integer gender) {
        if (gender < 0 || gender > 2) {
            throw new IllegalArgumentException("性别值只能是 0(未知)、1(男) 或 2(女)");
        }
    }

    private static void validateBirthDate(LocalDate birthDate) {
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("生日不能是未来日期");
        }
    }

    /**
     * 📝 更新用户信息
     */
    public void updateProfile(
            String username,
            String nickname,
            String avatar,
            Integer gender,
            LocalDate birthDate
    ) {
        if (username != null && !username.equals(this.username)) {
            validateUsername(username);
            var oldValue = this.username;
            this.username = username;
            addDomainEvent(UserUpdatedEvent.create(
                    this.userId, "username", oldValue, username
            ));
        }

        if (nickname != null && !nickname.equals(this.nickname)) {
            validateNickname(nickname);
            var oldValue = this.nickname;
            this.nickname = nickname;
            addDomainEvent(UserUpdatedEvent.create(
                    this.userId, "nickname", oldValue, nickname
            ));
        }

        if (avatar != null && !avatar.equals(this.avatar)) {
            this.avatar = avatar;
        }

        if (gender != null && !gender.equals(this.gender)) {
            validateGender(gender);
            this.gender = gender;
        }

        if (birthDate != null && !birthDate.equals(this.birthDate)) {
            validateBirthDate(birthDate);
            this.birthDate = birthDate;
        }

        this.updateTime = LocalDateTime.now();
    }

    /**
     * 🔓 启用用户
     */
    public void enable() {
        if (this.status != 1) {
            this.status = 1;
            this.updateTime = LocalDateTime.now();
            addDomainEvent(UserUpdatedEvent.create(
                    this.userId, "status", "0", "1"
            ));
        }
    }

    /**
     * 🔒 禁用用户
     */
    public void disable() {
        if (this.status != 0) {
            this.status = 0;
            this.updateTime = LocalDateTime.now();
            addDomainEvent(UserUpdatedEvent.create(
                    this.userId, "status", "1", "0"
            ));
        }
    }

    /**
     * 🔄 更新最后登录时间
     */
    public void updateLastLogin() {
        this.lastLoginTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
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

    // ========================================
    // 领域事件管理
    // ========================================

    private void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public void markAsCreated() {
        if (this.userId != null) {
            addDomainEvent(UserCreatedEvent.create(
                    this.userId, this.mobile, this.nickname, this.clientType
            ));
        }
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
}
