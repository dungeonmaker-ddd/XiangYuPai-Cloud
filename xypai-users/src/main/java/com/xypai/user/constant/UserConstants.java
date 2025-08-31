package com.xypai.user.constant;

/**
 * 🏗️ 用户模块常量 - 企业架构实现
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public class UserConstants {

    // ================================
    // 🔑 缓存Key常量
    // ================================

    /**
     * 用户信息缓存前缀
     */
    public static final String USER_CACHE_KEY = "xypai:user:info:";

    /**
     * 用户编码缓存前缀
     */
    public static final String USER_CODE_CACHE_KEY = "xypai:user:code:";

    /**
     * 手机号缓存前缀
     */
    public static final String USER_MOBILE_CACHE_KEY = "xypai:user:mobile:";

    /**
     * 用户名缓存前缀
     */
    public static final String USER_USERNAME_CACHE_KEY = "xypai:user:username:";

    /**
     * 用户统计缓存前缀
     */
    public static final String USER_STATS_CACHE_KEY = "xypai:user:stats:";

    // ================================
    // 📝 业务常量
    // ================================

    /**
     * 用户编码前缀
     */
    public static final String USER_CODE_PREFIX = "XY";

    /**
     * 用户编码长度
     */
    public static final int USER_CODE_LENGTH = 16;

    /**
     * 默认用户昵称前缀
     */
    public static final String DEFAULT_NICKNAME_PREFIX = "XY用户";

    /**
     * 默认头像URL
     */
    public static final String DEFAULT_AVATAR_URL = "https://cdn.xypai.com/avatar/default.jpg";

    /**
     * 初始用户等级
     */
    public static final int INITIAL_USER_LEVEL = 1;

    /**
     * 初始用户积分
     */
    public static final int INITIAL_USER_POINTS = 100;

    /**
     * 初始用户余额
     */
    public static final long INITIAL_BALANCE = 0L;

    // ================================
    // 🔄 状态常量
    // ================================

    /**
     * 用户状态 - 正常
     */
    public static final int USER_STATUS_NORMAL = 1;

    /**
     * 用户状态 - 禁用
     */
    public static final int USER_STATUS_DISABLED = 0;

    /**
     * 用户类型 - 普通用户
     */
    public static final int USER_TYPE_NORMAL = 0;

    /**
     * 用户类型 - VIP用户
     */
    public static final int USER_TYPE_VIP = 1;

    /**
     * 实名认证 - 未认证
     */
    public static final int VERIFIED_NO = 0;

    /**
     * 实名认证 - 已认证
     */
    public static final int VERIFIED_YES = 1;

    // ================================
    // ⏰ 时间常量
    // ================================

    /**
     * 缓存过期时间(分钟) - 用户信息
     */
    public static final int USER_CACHE_EXPIRE_MINUTES = 30;

    /**
     * 缓存过期时间(分钟) - 统计数据
     */
    public static final int STATS_CACHE_EXPIRE_MINUTES = 60;

    /**
     * 活跃用户定义天数
     */
    public static final int ACTIVE_USER_DAYS = 30;

    /**
     * 新用户定义天数
     */
    public static final int NEW_USER_DAYS = 7;

    // ================================
    // 📏 限制常量
    // ================================

    /**
     * 批量操作最大数量
     */
    public static final int BATCH_OPERATION_MAX_SIZE = 1000;

    /**
     * 查询最大返回数量
     */
    public static final int QUERY_MAX_LIMIT = 10000;

    /**
     * 分页默认页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 分页最大页大小
     */
    public static final int MAX_PAGE_SIZE = 100;

    // ================================
    // 🔐 权限常量
    // ================================

    /**
     * 用户管理权限前缀
     */
    public static final String USER_PERMISSION_PREFIX = "user:";

    /**
     * 查询用户权限
     */
    public static final String USER_QUERY_PERMISSION = "user:query";

    /**
     * 添加用户权限
     */
    public static final String USER_ADD_PERMISSION = "user:add";

    /**
     * 编辑用户权限
     */
    public static final String USER_EDIT_PERMISSION = "user:edit";

    /**
     * 删除用户权限
     */
    public static final String USER_REMOVE_PERMISSION = "user:remove";

    /**
     * 用户状态管理权限
     */
    public static final String USER_STATUS_PERMISSION = "user:status";

    /**
     * 用户升级权限
     */
    public static final String USER_UPGRADE_PERMISSION = "user:upgrade";

    /**
     * 用户实名认证权限
     */
    public static final String USER_VERIFY_PERMISSION = "user:verify";
}
