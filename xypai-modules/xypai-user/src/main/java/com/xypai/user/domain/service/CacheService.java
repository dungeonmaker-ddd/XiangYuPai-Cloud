package com.xypai.user.domain.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 缓存服务接口 - 领域层
 *
 * @author XyPai
 * @since 2025-01-02
 */
public interface CacheService {

    /**
     * 设置缓存值
     */
    <T> void set(String key, T value, Duration ttl);

    /**
     * 获取缓存值
     */
    <T> Optional<T> get(String key, Class<T> clazz);

    /**
     * 删除缓存
     */
    void delete(String key);

    /**
     * 批量删除缓存（支持通配符）
     */
    void deletePattern(String pattern);

    /**
     * 检查缓存是否存在
     */
    boolean exists(String key);

    /**
     * 设置集合缓存
     */
    <T> void setSet(String key, Set<T> value, Duration ttl);

    /**
     * 获取集合缓存
     */
    <T> Set<T> getSet(String key, Class<T> clazz);

    /**
     * 向集合添加元素
     */
    <T> void addToSet(String key, T value);

    /**
     * 从集合移除元素
     */
    <T> void removeFromSet(String key, T value);

    /**
     * 检查集合中是否包含元素
     */
    <T> boolean containsInSet(String key, T value);

    /**
     * 设置列表缓存
     */
    <T> void setList(String key, List<T> value, Duration ttl);

    /**
     * 获取列表缓存
     */
    <T> List<T> getList(String key, Class<T> clazz);

    /**
     * 向列表头部添加元素
     */
    <T> void addToListHead(String key, T value);

    /**
     * 向列表尾部添加元素
     */
    <T> void addToListTail(String key, T value);

    /**
     * 获取列表范围
     */
    <T> List<T> getListRange(String key, int start, int end, Class<T> clazz);

    /**
     * 递增计数器
     */
    long increment(String key);

    /**
     * 递增计数器（指定步长）
     */
    long increment(String key, long delta);

    /**
     * 递减计数器
     */
    long decrement(String key);

    /**
     * 设置过期时间
     */
    void expire(String key, Duration ttl);
}
