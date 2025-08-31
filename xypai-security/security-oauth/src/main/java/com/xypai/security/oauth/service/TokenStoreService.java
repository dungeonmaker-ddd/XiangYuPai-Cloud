package com.xypai.security.oauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xypai.security.oauth.auth.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 🗄️ Redis Token存储服务
 * <p>
 * XV02:11 Token存储管理
 * 使用Redis实现分布式Token存储，支持集群部署
 *
 * @author xypai
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenStoreService {

    // Redis Key前缀
    private static final String ACCESS_TOKEN_PREFIX = "auth:access_token:";
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh_token:";
    private static final String USER_TOKEN_PREFIX = "auth:user_tokens:";
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 存储访问令牌
     *
     * @param accessToken   访问令牌
     * @param userInfo      用户信息
     * @param expireSeconds 过期时间（秒）
     */
    public void storeAccessToken(String accessToken, AuthResponse.UserInfo userInfo, long expireSeconds) {
        try {
            String key = ACCESS_TOKEN_PREFIX + accessToken;
            String userInfoJson = objectMapper.writeValueAsString(userInfo);

            redisTemplate.opsForValue().set(key, userInfoJson, expireSeconds, TimeUnit.SECONDS);

            // 为用户建立Token索引，支持查询用户的所有Token
            String userTokenKey = USER_TOKEN_PREFIX + userInfo.id();
            redisTemplate.opsForSet().add(userTokenKey, accessToken);
            redisTemplate.expire(userTokenKey, expireSeconds, TimeUnit.SECONDS);

            log.debug("存储访问令牌成功: token={}, userId={}, expireSeconds={}",
                    maskToken(accessToken), userInfo.id(), expireSeconds);

        } catch (JsonProcessingException e) {
            log.error("序列化用户信息失败: userId={}, error={}", userInfo.id(), e.getMessage());
            throw new RuntimeException("Token存储失败", e);
        }
    }

    /**
     * 存储刷新令牌
     *
     * @param refreshToken  刷新令牌
     * @param accessToken   关联的访问令牌
     * @param expireSeconds 过期时间（秒）
     */
    public void storeRefreshToken(String refreshToken, String accessToken, long expireSeconds) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, accessToken, expireSeconds, TimeUnit.SECONDS);

        log.debug("存储刷新令牌成功: refreshToken={}, accessToken={}",
                maskToken(refreshToken), maskToken(accessToken));
    }

    /**
     * 获取访问令牌对应的用户信息
     *
     * @param accessToken 访问令牌
     * @return 用户信息
     */
    public Optional<AuthResponse.UserInfo> getAccessToken(String accessToken) {
        try {
            String key = ACCESS_TOKEN_PREFIX + accessToken;
            String userInfoJson = redisTemplate.opsForValue().get(key);

            if (userInfoJson == null) {
                log.debug("访问令牌不存在或已过期: token={}", maskToken(accessToken));
                return Optional.empty();
            }

            AuthResponse.UserInfo userInfo = objectMapper.readValue(userInfoJson, AuthResponse.UserInfo.class);
            log.debug("获取访问令牌成功: token={}, userId={}", maskToken(accessToken), userInfo.id());

            return Optional.of(userInfo);

        } catch (JsonProcessingException e) {
            log.error("反序列化用户信息失败: token={}, error={}", maskToken(accessToken), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 获取刷新令牌对应的访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 访问令牌
     */
    public Optional<String> getRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        String accessToken = redisTemplate.opsForValue().get(key);

        if (accessToken == null) {
            log.debug("刷新令牌不存在或已过期: refreshToken={}", maskToken(refreshToken));
            return Optional.empty();
        }

        log.debug("获取刷新令牌成功: refreshToken={}, accessToken={}",
                maskToken(refreshToken), maskToken(accessToken));

        return Optional.of(accessToken);
    }

    /**
     * 删除访问令牌
     *
     * @param accessToken 访问令牌
     * @return 被删除的用户信息
     */
    public Optional<AuthResponse.UserInfo> removeAccessToken(String accessToken) {
        String key = ACCESS_TOKEN_PREFIX + accessToken;

        // 先获取用户信息
        Optional<AuthResponse.UserInfo> userInfoOpt = getAccessToken(accessToken);

        // 删除Token
        Boolean deleted = redisTemplate.delete(key);

        if (Boolean.TRUE.equals(deleted) && userInfoOpt.isPresent()) {
            AuthResponse.UserInfo userInfo = userInfoOpt.get();

            // 从用户Token索引中移除
            String userTokenKey = USER_TOKEN_PREFIX + userInfo.id();
            redisTemplate.opsForSet().remove(userTokenKey, accessToken);

            log.info("删除访问令牌成功: token={}, userId={}", maskToken(accessToken), userInfo.id());
            return userInfoOpt;
        }

        log.debug("访问令牌不存在，无需删除: token={}", maskToken(accessToken));
        return Optional.empty();
    }

    /**
     * 删除刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 是否删除成功
     */
    public boolean removeRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        Boolean deleted = redisTemplate.delete(key);

        log.debug("删除刷新令牌: refreshToken={}, deleted={}", maskToken(refreshToken), deleted);
        return Boolean.TRUE.equals(deleted);
    }

    /**
     * 删除用户的所有Token（用于强制登出）
     *
     * @param userId 用户ID
     * @return 删除的Token数量
     */
    public int removeAllUserTokens(Long userId) {
        String userTokenKey = USER_TOKEN_PREFIX + userId;

        // 获取用户的所有Token
        var tokens = redisTemplate.opsForSet().members(userTokenKey);
        if (tokens == null || tokens.isEmpty()) {
            log.debug("用户没有活跃Token: userId={}", userId);
            return 0;
        }

        int deletedCount = 0;
        for (String token : tokens) {
            if (removeAccessToken(token).isPresent()) {
                deletedCount++;
            }
        }

        // 清空用户Token索引
        redisTemplate.delete(userTokenKey);

        log.info("删除用户所有Token: userId={}, deletedCount={}", userId, deletedCount);
        return deletedCount;
    }

    /**
     * 检查Token是否存在
     *
     * @param accessToken 访问令牌
     * @return 是否存在
     */
    public boolean exists(String accessToken) {
        String key = ACCESS_TOKEN_PREFIX + accessToken;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 延长Token过期时间
     *
     * @param accessToken   访问令牌
     * @param expireSeconds 新的过期时间（秒）
     * @return 是否延长成功
     */
    public boolean extendToken(String accessToken, long expireSeconds) {
        String key = ACCESS_TOKEN_PREFIX + accessToken;
        Boolean extended = redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(extended)) {
            log.debug("延长Token过期时间: token={}, expireSeconds={}", maskToken(accessToken), expireSeconds);
        }

        return Boolean.TRUE.equals(extended);
    }

    /**
     * 获取Token剩余过期时间
     *
     * @param accessToken 访问令牌
     * @return 剩余秒数，-1表示永不过期，-2表示不存在
     */
    public long getTokenTTL(String accessToken) {
        String key = ACCESS_TOKEN_PREFIX + accessToken;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 脱敏Token用于日志输出
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 8) + "***" + token.substring(token.length() - 4);
    }
}
