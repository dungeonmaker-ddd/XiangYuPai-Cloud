package com.xypai.user.domain.user;

import com.xypai.user.domain.shared.DomainEvent;
import com.xypai.user.domain.shared.UserCreatedEvent;
import com.xypai.user.domain.shared.UserUpdatedEvent;
import com.xypai.user.domain.user.entity.User;
import com.xypai.user.domain.user.valueobject.UserId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 👤 用户聚合根 - DDD核心聚合
 * <p>
 * 负责用户基础信息管理和业务规则验证
 * 聚合根模式：委托给User实体执行具体操作
 *
 * @author XyPai
 * @since 2025-01-02
 */
public class UserAggregate {

    // ========================================
    // 聚合根核心
    // ========================================

    private final List<DomainEvent> domainEvents = new ArrayList<>();
    private User user;  // 改为非final，允许替换（主要用于设置ID）

    // ========================================
    // 构造器 - 私有，通过工厂方法创建
    // ========================================

    private UserAggregate(User user) {
        this.user = Objects.requireNonNull(user, "用户实体不能为空");
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

        // 创建用户实体（此时还没有ID，由基础设施层生成）
        var user = User.create(UserId.empty(), mobile, nickname, clientType);
        var aggregate = new UserAggregate(user);

        // 标记创建事件（等有ID后再发布）
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

        // 创建用户实体（使用临时ID用于测试）
        var user = User.create(UserId.of(1L), mobile, nickname, clientType);
        var aggregate = new UserAggregate(user);

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
        var user = User.create(
                UserId.of(userId),
                mobile,
                nickname,
                clientType != null ? clientType : "app"
        );

        // 设置其他字段
        if (username != null) {
            user.updateBasicInfo(username, null, avatar);
        }
        if (gender != null || birthDate != null) {
            user.updatePersonalInfo(gender, birthDate);
        }
        if (status != null && status == 0) {
            user.disable();
        }

        return new UserAggregate(user);
    }

    // ========================================
    // 业务方法 - 委托给User实体
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

    private static void validateClientType(String clientType) {
        if (!List.of("web", "app", "mini").contains(clientType)) {
            throw new IllegalArgumentException("客户端类型只能是 web、app 或 mini");
        }
    }

    /**
     * 📝 更新用户基础信息
     */
    public void updateProfile(
            String username,
            String nickname,
            String avatar,
            Integer gender,
            LocalDate birthDate
    ) {
        var oldNickname = user.getNickname();
        var oldUsername = user.getUsername();

        // 委托给实体执行
        user.updateBasicInfo(username, nickname, avatar);
        user.updatePersonalInfo(gender, birthDate);

        // 发布变更事件
        if (!Objects.equals(oldNickname, user.getNickname())) {
            addDomainEvent(UserUpdatedEvent.create(
                    user.getUserId(), "nickname", oldNickname, user.getNickname()
            ));
        }

        if (!Objects.equals(oldUsername, user.getUsername())) {
            addDomainEvent(UserUpdatedEvent.create(
                    user.getUserId(), "username", oldUsername, user.getUsername()
            ));
        }
    }

    // ========================================
    // 业务规则验证 - 静态方法
    // ========================================

    /**
     * 🔓 启用用户
     */
    public void enable() {
        if (!user.isActive()) {
            user.enable();
            addDomainEvent(UserUpdatedEvent.create(
                    user.getUserId(), "status", "0", "1"
            ));
        }
    }

    /**
     * 🔒 禁用用户
     */
    public void disable() {
        if (user.isActive()) {
            user.disable();
            addDomainEvent(UserUpdatedEvent.create(
                    user.getUserId(), "status", "1", "0"
            ));
        }
    }

    /**
     * 🔄 更新最后登录时间
     */
    public void updateLastLogin() {
        user.updateLastLogin();
    }

    // ========================================
    // 查询方法 - 委托给User实体
    // ========================================

    /**
     * 检查用户是否正常
     */
    public boolean isActive() {
        return user.isActive();
    }

    /**
     * 检查用户是否已禁用
     */
    public boolean isDisabled() {
        return user.isDisabled();
    }

    /**
     * 检查是否有用户名
     */
    public boolean hasUsername() {
        return user.hasUsername();
    }

    /**
     * 检查是否成年人
     */
    public boolean isAdult() {
        return user.isAdult();
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
        if (user.getUserId() != null) {
            addDomainEvent(UserCreatedEvent.create(
                    user.getUserId(), user.getMobile(), user.getNickname(), user.getClientType()
            ));
        }
    }

    public UserId getUserId() {
        return user.getUserId();
    }

    // ========================================
    // Getters - 委托给User实体
    // ========================================

    /**
     * 🆔 设置用户ID（由基础设施层调用）
     * 用于新用户创建后设置生成的ID
     */
    public void setUserId(UserId userId) {
        if (user.getUserId() != null) {
            throw new IllegalStateException("用户ID已设置，不能重复设置");
        }

        // 创建新的User实例并替换
        this.user = user.withUserId(userId);

        // 现在可以发布创建事件了
        markAsCreated();
    }

    public String getMobile() {
        return user.getMobile();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getNickname() {
        return user.getNickname();
    }

    public String getAvatar() {
        return user.getAvatar();
    }

    public Integer getGender() {
        return user.getGender();
    }

    public LocalDate getBirthDate() {
        return user.getBirthDate();
    }

    public Integer getStatus() {
        return user.getStatus();
    }

    public String getClientType() {
        return user.getClientType();
    }

    public String getRealName() {
        return user.getRealName();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getWechat() {
        return user.getWechat();
    }

    public String getOccupation() {
        return user.getOccupation();
    }

    public String getLocation() {
        return user.getLocation();
    }

    public String getBio() {
        return user.getBio();
    }

    public String getInterests() {
        return user.getInterests();
    }

    public LocalDateTime getRegisterTime() {
        return user.getRegisterTime();
    }

    public LocalDateTime getLastLoginTime() {
        return user.getLastLoginTime();
    }

    public LocalDateTime getUpdateTime() {
        return user.getUpdateTime();
    }

    // ========================================
    // 获取内部实体（仅供Repository使用）
    // ========================================

    /**
     * 🔒 获取内部用户实体（仅供基础设施层使用）
     */
    public User getUser() {
        return user;
    }
}