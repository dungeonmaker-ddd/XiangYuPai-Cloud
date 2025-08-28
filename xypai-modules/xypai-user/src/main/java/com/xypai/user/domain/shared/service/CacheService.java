package com.xypai.user.domain.shared.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 🔧 缓存服务接口
 * <p>
 * 提供统一的缓存操作抽象，支持多种数据类型的缓存操作
 *
 * @author XyPai
 * @since 2025-08-28
 */
public interface CacheService {

    // ================== 字符串操作 ==================

    /**
     * 设置字符串值
     *
     * @param key        缓存键
     * @param value      缓存值
     * @param expireTime 过期时间
     */
    void setString(String key, String value, Duration expireTime);

    /**
     * 获取字符串值
     *
     * @param key 缓存键
     * @return 缓存值
     */
    Optional<String> getString(String key);

    // ================== 对象操作 ==================

    /**
     * 设置对象值
     *
     * @param key        缓存键
     * @param value      缓存值
     * @param expireTime 过期时间
     */
    <T> void setObject(String key, T value, Duration expireTime);

    /**
     * 获取对象值
     *
     * @param key   缓存键
     * @param clazz 对象类型
     * @return 缓存值
     */
    <T> Optional<T> getObject(String key, Class<T> clazz);

    // ================== 集合操作 ==================

    /**
     * 添加集合成员
     *
     * @param key     缓存键
     * @param members 成员列表
     */
    void addToSet(String key, String... members);

    /**
     * 判断是否为集合成员
     *
     * @param key    缓存键
     * @param member 成员
     * @return 是否存在
     */
    boolean isSetMember(String key, String member);

    /**
     * 获取集合所有成员
     *
     * @param key 缓存键
     * @return 成员集合
     */
    Set<String> getSetMembers(String key);

    /**
     * 获取集合大小
     *
     * @param key 缓存键
     * @return 集合大小
     */
    long getSetSize(String key);

    // ================== 列表操作 ==================

    /**
     * 添加到列表右端
     *
     * @param key    缓存键
     * @param values 值列表
     */
    void rightPushToList(String key, String... values);

    /**
     * 获取列表范围
     *
     * @param key   缓存键
     * @param start 开始位置
     * @param end   结束位置
     * @return 值列表
     */
    List<String> getListRange(String key, long start, long end);

    /**
     * 获取列表大小
     *
     * @param key 缓存键
     * @return 列表大小
     */
    long getListSize(String key);

    // ================== 哈希操作 ==================

    /**
     * 设置哈希字段
     *
     * @param key   缓存键
     * @param field 字段名
     * @param value 字段值
     */
    void setHashField(String key, String field, String value);

    /**
     * 批量设置哈希字段
     *
     * @param key    缓存键
     * @param fields 字段映射
     */
    void setHashFields(String key, Map<String, String> fields);

    /**
     * 获取哈希字段
     *
     * @param key   缓存键
     * @param field 字段名
     * @return 字段值
     */
    Optional<String> getHashField(String key, String field);

    /**
     * 获取所有哈希字段
     *
     * @param key 缓存键
     * @return 字段映射
     */
    Map<String, String> getAllHashFields(String key);

    // ================== 计数器操作 ==================

    /**
     * 递增计数器
     *
     * @param key   缓存键
     * @param delta 增量
     * @return 递增后的值
     */
    long increment(String key, long delta);

    /**
     * 递增计数器并设置过期时间
     *
     * @param key        缓存键
     * @param delta      增量
     * @param expireTime 过期时间
     * @return 递增后的值
     */
    long incrementAndExpire(String key, long delta, Duration expireTime);

    // ================== 通用操作 ==================

    /**
     * 判断键是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 删除键
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean delete(String key);

    /**
     * 设置过期时间
     *
     * @param key        缓存键
     * @param expireTime 过期时间
     * @return 是否设置成功
     */
    boolean expire(String key, Duration expireTime);

    /**
     * 获取剩余过期时间
     *
     * @param key 缓存键
     * @return 剩余时间
     */
    Duration getExpire(String key);
}
