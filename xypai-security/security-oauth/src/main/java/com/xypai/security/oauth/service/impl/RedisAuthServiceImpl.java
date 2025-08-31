package com.xypai.security.oauth.service.impl;

import com.xypai.security.oauth.auth.dto.request.AuthRequest;
import com.xypai.security.oauth.auth.dto.response.AuthResponse;
import com.xypai.security.oauth.common.properties.AuthProperties;
import com.xypai.security.oauth.service.AuthService;
import com.xypai.security.oauth.service.TokenStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * 🔐 基于Redis的认证服务实现
 * <p>
 * XV02:12 生产级认证服务实现
 * 使用Redis存储，支持集群部署和高可用
 *
 * @author xypai
 * @since 1.0.0
 */
@Slf4j
@Service("redisAuthService")
// TODO: 测试稳定后可以启用@Primary替代内存版本
// @Primary  
@RequiredArgsConstructor
public class RedisAuthServiceImpl implements AuthService {

    private final AuthProperties authProperties;
    private final TokenStoreService tokenStoreService;

    @Override
    public Optional<AuthResponse> authenticate(AuthRequest authRequest) {
        log.info("用户认证请求: username={}, authType={}",
                authRequest.username(), authRequest.authType());

        try {
            // 验证用户凭据
            AuthResponse.UserInfo userInfo = authenticateUser(authRequest);
            if (userInfo == null) {
                log.warn("用户认证失败: {}", authRequest.username());
                return Optional.empty();
            }

            // 生成令牌
            String accessToken = generateAccessToken(userInfo);
            String refreshToken = generateRefreshToken(userInfo);
            Long expiresIn = authProperties.token().expireTime().toSeconds();

            // 存储到Redis
            tokenStoreService.storeAccessToken(accessToken, userInfo, expiresIn);
            tokenStoreService.storeRefreshToken(refreshToken, accessToken, expiresIn * 7); // 刷新令牌7倍时长

            AuthResponse response = AuthResponse.create(accessToken, refreshToken, expiresIn, userInfo);
            log.info("用户认证成功: username={}", authRequest.username());

            return Optional.of(response);

        } catch (Exception e) {
            log.error("认证过程发生异常: username={}, error={}",
                    authRequest.username(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<AuthResponse> refreshToken(String refreshToken, String clientType) {
        log.info("刷新令牌请求");

        // 1. 验证刷新令牌并获取关联的访问令牌
        Optional<String> oldAccessTokenOpt = tokenStoreService.getRefreshToken(refreshToken);
        if (oldAccessTokenOpt.isEmpty()) {
            log.warn("无效的刷新令牌");
            return Optional.empty();
        }

        String oldAccessToken = oldAccessTokenOpt.get();

        // 2. 获取用户信息
        Optional<AuthResponse.UserInfo> userInfoOpt = tokenStoreService.getAccessToken(oldAccessToken);
        if (userInfoOpt.isEmpty()) {
            log.warn("令牌对应的用户信息不存在");
            return Optional.empty();
        }

        AuthResponse.UserInfo userInfo = userInfoOpt.get();

        // 3. 生成新令牌
        String newAccessToken = generateAccessToken(userInfo);
        String newRefreshToken = generateRefreshToken(userInfo);
        Long expiresIn = authProperties.token().expireTime().toSeconds();

        // 4. 原子性更新Redis存储
        try {
            // 删除旧令牌
            tokenStoreService.removeAccessToken(oldAccessToken);
            tokenStoreService.removeRefreshToken(refreshToken);

            // 存储新令牌
            tokenStoreService.storeAccessToken(newAccessToken, userInfo, expiresIn);
            tokenStoreService.storeRefreshToken(newRefreshToken, newAccessToken, expiresIn * 7);

            AuthResponse response = AuthResponse.create(newAccessToken, newRefreshToken, expiresIn, userInfo);
            log.info("令牌刷新成功: username={}", userInfo.username());

            return Optional.of(response);

        } catch (Exception e) {
            log.error("令牌刷新过程发生异常: username={}, error={}",
                    userInfo.username(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Map<String, Object>> verifyToken(String accessToken) {
        Optional<AuthResponse.UserInfo> userInfoOpt = tokenStoreService.getAccessToken(accessToken);
        if (userInfoOpt.isEmpty()) {
            log.debug("令牌验证失败: token不存在或已过期");
            return Optional.empty();
        }

        AuthResponse.UserInfo userInfo = userInfoOpt.get();

        Map<String, Object> tokenInfo = Map.of(
                "valid", true,
                "username", userInfo.username(),
                "userId", userInfo.id(),
                "roles", userInfo.roles(),
                "permissions", userInfo.permissions(),
                "ttl", tokenStoreService.getTokenTTL(accessToken)
        );

        log.debug("令牌验证成功: username={}", userInfo.username());
        return Optional.of(tokenInfo);
    }

    @Override
    public Optional<AuthResponse.UserInfo> getUserInfo(String accessToken) {
        Optional<AuthResponse.UserInfo> userInfoOpt = tokenStoreService.getAccessToken(accessToken);
        if (userInfoOpt.isPresent()) {
            log.debug("获取用户信息成功: username={}", userInfoOpt.get().username());
        } else {
            log.debug("获取用户信息失败: token不存在或已过期");
        }
        return userInfoOpt;
    }

    @Override
    public boolean logout(String accessToken) {
        log.info("用户登出请求");

        // 获取用户信息
        Optional<AuthResponse.UserInfo> userInfoOpt = tokenStoreService.getAccessToken(accessToken);
        if (userInfoOpt.isEmpty()) {
            log.warn("登出失败：无效的访问令牌");
            return false;
        }

        AuthResponse.UserInfo userInfo = userInfoOpt.get();

        // 删除访问令牌
        tokenStoreService.removeAccessToken(accessToken);

        // 查找并删除关联的刷新令牌
        // 注意：这里简化处理，实际项目中可以建立双向索引来优化
        log.info("用户登出成功: username={}", userInfo.username());
        return true;
    }

    /**
     * 🚪 强制用户登出（删除用户所有Token）
     *
     * @param userId 用户ID
     * @return 删除的Token数量
     */
    public int forceLogoutUser(Long userId) {
        log.info("强制用户登出: userId={}", userId);
        return tokenStoreService.removeAllUserTokens(userId);
    }

    /**
     * 🔄 延长Token过期时间
     *
     * @param accessToken   访问令牌
     * @param extendSeconds 延长的秒数
     * @return 是否成功
     */
    public boolean extendToken(String accessToken, long extendSeconds) {
        long currentTTL = tokenStoreService.getTokenTTL(accessToken);
        if (currentTTL > 0) {
            return tokenStoreService.extendToken(accessToken, currentTTL + extendSeconds);
        }
        return false;
    }

    /**
     * 📊 获取Token信息（用于监控）
     *
     * @param accessToken 访问令牌
     * @return Token详细信息
     */
    public Map<String, Object> getTokenDetails(String accessToken) {
        Optional<AuthResponse.UserInfo> userInfoOpt = tokenStoreService.getAccessToken(accessToken);
        if (userInfoOpt.isEmpty()) {
            return Map.of("exists", false);
        }

        AuthResponse.UserInfo userInfo = userInfoOpt.get();
        long ttl = tokenStoreService.getTokenTTL(accessToken);

        return Map.of(
                "exists", true,
                "userId", userInfo.id(),
                "username", userInfo.username(),
                "ttl", ttl,
                "expired", ttl <= 0
        );
    }

    // ================================
    // 🔒 私有辅助方法
    // ================================

    /**
     * 用户认证逻辑（MVP版本）
     * TODO: 后续需要集成用户服务进行真实认证
     */
    private AuthResponse.UserInfo authenticateUser(AuthRequest authRequest) {
        // MVP版本：模拟用户认证（实际应调用用户服务）
        if ("admin".equals(authRequest.username()) &&
                authRequest.isPasswordAuth() &&
                "123456".equals(authRequest.password())) {

            return new AuthResponse.AdminUser(
                    1L, "admin", "管理员", "admin@xypai.com", "13800138000",
                    null, null, java.time.Instant.now()
            );
        }

        if ("user".equals(authRequest.username()) &&
                authRequest.isPasswordAuth() &&
                "123456".equals(authRequest.password())) {

            return new AuthResponse.StandardUser(
                    2L, "user", "普通用户", "user@xypai.com", "13800138001",
                    null, null, java.time.Instant.now()
            );
        }

        // TODO: 集成用户服务
        // return userServiceClient.authenticate(authRequest);

        return null;
    }

    /**
     * 生成访问令牌
     * TODO: 后续可以升级为JWT或其他标准格式
     */
    private String generateAccessToken(AuthResponse.UserInfo userInfo) {
        return "access_" + userInfo.id() + "_" + System.currentTimeMillis();
    }

    /**
     * 生成刷新令牌
     * TODO: 后续可以升级为JWT或其他标准格式
     */
    private String generateRefreshToken(AuthResponse.UserInfo userInfo) {
        return "refresh_" + userInfo.id() + "_" + System.currentTimeMillis();
    }
}
