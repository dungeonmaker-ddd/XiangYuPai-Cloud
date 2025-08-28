package com.xypai.user.infrastructure.cache;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.xypai.user.domain.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis缓存服务实现
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public <T> void set(String key, T value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("缓存设置成功: key={}, ttl={}", key, ttl);
        } catch (Exception e) {
            log.error("缓存设置失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }

            if (clazz.isInstance(value)) {
                return Optional.of(clazz.cast(value));
            }

            // 尝试JSON转换
            T converted = objectMapper.convertValue(value, clazz);
            return Optional.of(converted);
        } catch (Exception e) {
            log.error("缓存获取失败: key={}, error={}", key, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("缓存删除成功: key={}", key);
        } catch (Exception e) {
            log.error("缓存删除失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("批量缓存删除成功: pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("批量缓存删除失败: pattern={}, error={}", pattern, e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("缓存存在性检查失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public <T> void setSet(String key, Set<T> value, Duration ttl) {
        try {
            // 先删除旧数据
            redisTemplate.delete(key);

            if (!value.isEmpty()) {
                redisTemplate.opsForSet().add(key, value.toArray());
                redisTemplate.expire(key, ttl);
            }
            log.debug("集合缓存设置成功: key={}, size={}, ttl={}", key, value.size(), ttl);
        } catch (Exception e) {
            log.error("集合缓存设置失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public <T> Set<T> getSet(String key, Class<T> clazz) {
        try {
            Set<Object> members = redisTemplate.opsForSet().members(key);
            if (members == null || members.isEmpty()) {
                return Set.of();
            }

            return members.stream()
                    .map(member -> objectMapper.convertValue(member, clazz))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("集合缓存获取失败: key={}, error={}", key, e.getMessage(), e);
            return Set.of();
        }
    }

    @Override
    public <T> void addToSet(String key, T value) {
        try {
            redisTemplate.opsForSet().add(key, value);
            log.debug("集合缓存添加成功: key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("集合缓存添加失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public <T> void removeFromSet(String key, T value) {
        try {
            redisTemplate.opsForSet().remove(key, value);
            log.debug("集合缓存移除成功: key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("集合缓存移除失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public <T> boolean containsInSet(String key, T value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("集合缓存包含性检查失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public <T> void setList(String key, List<T> value, Duration ttl) {
        try {
            // 先删除旧数据
            redisTemplate.delete(key);

            if (!value.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(key, value.toArray());
                redisTemplate.expire(key, ttl);
            }
            log.debug("列表缓存设置成功: key={}, size={}, ttl={}", key, value.size(), ttl);
        } catch (Exception e) {
            log.error("列表缓存设置失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public <T> List<T> getList(String key, Class<T> clazz) {
        try {
            List<Object> list = redisTemplate.opsForList().range(key, 0, -1);
            if (list == null || list.isEmpty()) {
                return List.of();
            }

            return list.stream()
                    .map(item -> objectMapper.convertValue(item, clazz))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("列表缓存获取失败: key={}, error={}", key, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public <T> void addToListHead(String key, T value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            log.debug("列表缓存头部添加成功: key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("列表缓存头部添加失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public <T> void addToListTail(String key, T value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            log.debug("列表缓存尾部添加成功: key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("列表缓存尾部添加失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public <T> List<T> getListRange(String key, int start, int end, Class<T> clazz) {
        try {
            List<Object> list = redisTemplate.opsForList().range(key, start, end);
            if (list == null || list.isEmpty()) {
                return List.of();
            }

            return list.stream()
                    .map(item -> objectMapper.convertValue(item, clazz))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("列表缓存范围获取失败: key={}, start={}, end={}, error={}", key, start, end, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public long increment(String key) {
        try {
            Long result = redisTemplate.opsForValue().increment(key);
            return result != null ? result : 0L;
        } catch (Exception e) {
            log.error("缓存递增失败: key={}, error={}", key, e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    public long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0L;
        } catch (Exception e) {
            log.error("缓存递增失败: key={}, delta={}, error={}", key, delta, e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    public long decrement(String key) {
        try {
            Long result = redisTemplate.opsForValue().decrement(key);
            return result != null ? result : 0L;
        } catch (Exception e) {
            log.error("缓存递减失败: key={}, error={}", key, e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    public void expire(String key, Duration ttl) {
        try {
            redisTemplate.expire(key, ttl);
            log.debug("缓存过期时间设置成功: key={}, ttl={}", key, ttl);
        } catch (Exception e) {
            log.error("缓存过期时间设置失败: key={}, ttl={}, error={}", key, ttl, e.getMessage(), e);
        }
    }
}
