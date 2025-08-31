package com.xypai.security.oauth.service.impl;

import com.xypai.security.oauth.auth.dto.request.AuthRequest;
import com.xypai.security.oauth.auth.dto.response.AuthResponse;
import com.xypai.security.oauth.common.properties.AuthProperties;
import com.xypai.security.oauth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🔐 认证服务实现类
 * <p>
 * XV02:05 MVP版本的认证服务实现
 * 使用内存存储，后续可扩展为数据库+Redis
 *
 * @author xypai
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthProperties authProperties;
    
    // MVP版本：使用内存存储（生产环境应使用Redis + 数据库）
    private final Map<String, AuthResponse.UserInfo> tokenStore = new ConcurrentHashMap<>();
    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();
    
    @Override
    public Optional<AuthResponse> authenticate(AuthRequest authRequest) {
        log.info("用户认证请求: username={}, authType={}",
                authRequest.username(), authRequest.authType());
        
        try {
            // MVP版本：简化认证逻辑
            AuthResponse.UserInfo userInfo = authenticateUser(authRequest);
            if (userInfo == null) {
                log.warn("用户认证失败: {}", authRequest.username());
                return Optional.empty();
            }

            // 生成令牌 - 使用默认过期时间
            String accessToken = generateAccessToken(userInfo);
            String refreshToken = generateRefreshToken(userInfo);
            Long expiresIn = authProperties.token().appExpireTime().toSeconds(); // 使用App端默认时间
            
            // 存储令牌
            tokenStore.put(accessToken, userInfo);
            refreshTokenStore.put(refreshToken, accessToken);

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
        log.info("刷新令牌请求: clientType={}", clientType);
        
        String oldAccessToken = refreshTokenStore.get(refreshToken);
        if (oldAccessToken == null) {
            log.warn("无效的刷新令牌");
            return Optional.empty();
        }
        
        AuthResponse.UserInfo userInfo = tokenStore.get(oldAccessToken);
        if (userInfo == null) {
            log.warn("令牌对应的用户信息不存在");
            return Optional.empty();
        }

        // 生成新令牌 - 使用默认过期时间
        String newAccessToken = generateAccessToken(userInfo);
        String newRefreshToken = generateRefreshToken(userInfo);
        Long expiresIn = authProperties.token().appExpireTime().toSeconds();
        
        // 更新存储
        tokenStore.remove(oldAccessToken);
        refreshTokenStore.remove(refreshToken);
        tokenStore.put(newAccessToken, userInfo);
        refreshTokenStore.put(newRefreshToken, newAccessToken);

        AuthResponse response = AuthResponse.create(newAccessToken, newRefreshToken, expiresIn, userInfo);
        log.info("令牌刷新成功: username={}", userInfo.username());
        
        return Optional.of(response);
    }
    
    @Override
    public Optional<Map<String, Object>> verifyToken(String accessToken) {
        AuthResponse.UserInfo userInfo = tokenStore.get(accessToken);
        if (userInfo == null) {
            return Optional.empty();
        }
        
        Map<String, Object> tokenInfo = Map.of(
            "valid", true,
            "username", userInfo.username(),
            "roles", userInfo.roles(),
            "permissions", userInfo.permissions()
        );
        
        return Optional.of(tokenInfo);
    }
    
    @Override
    public Optional<AuthResponse.UserInfo> getUserInfo(String accessToken) {
        return Optional.ofNullable(tokenStore.get(accessToken));
    }
    
    @Override
    public boolean logout(String accessToken) {
        log.info("用户登出请求");
        
        AuthResponse.UserInfo userInfo = tokenStore.remove(accessToken);
        if (userInfo != null) {
            // 移除关联的刷新令牌
            refreshTokenStore.entrySet().removeIf(entry -> accessToken.equals(entry.getValue()));
            log.info("用户登出成功: username={}", userInfo.username());
            return true;
        }
        
        log.warn("登出失败：无效的访问令牌");
        return false;
    }
    
    /**
     * MVP版本：简化的用户认证逻辑
     */
    private AuthResponse.UserInfo authenticateUser(AuthRequest authRequest) {
        // MVP版本：模拟用户认证（实际应查询数据库）
        if ("admin".equals(authRequest.username()) && 
            authRequest.isPasswordAuth() && 
            "123456".equals(authRequest.password())) {

            return new AuthResponse.AdminUser(
                    1L, "admin", "管理员", "admin@xypai.com", "13800138000",
                    Set.of("ADMIN", "USER"), Set.of("user:read", "user:write", "system:config"),
                Instant.now()
            );
        }
        
        if ("user".equals(authRequest.username()) && 
            authRequest.isPasswordAuth() && 
            "123456".equals(authRequest.password())) {

            return new AuthResponse.StandardUser(
                    2L, "user", "普通用户", "user@xypai.com", "13800138001",
                    Set.of("USER"), Set.of("user:read"),
                Instant.now()
            );
        }
        
        return null;
    }
    
    /**
     * 生成访问令牌（MVP版本：使用简单算法）
     */
    private String generateAccessToken(AuthResponse.UserInfo userInfo) {
        return "access_" + userInfo.id() + "_" + System.currentTimeMillis();
    }
    
    /**
     * 生成刷新令牌（MVP版本：使用简单算法）
     */
    private String generateRefreshToken(AuthResponse.UserInfo userInfo) {
        return "refresh_" + userInfo.id() + "_" + System.currentTimeMillis();
    }
}
