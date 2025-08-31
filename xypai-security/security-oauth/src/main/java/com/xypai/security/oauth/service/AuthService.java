package com.xypai.security.oauth.service;

import com.xypai.security.oauth.auth.dto.request.AuthRequest;
import com.xypai.security.oauth.auth.dto.response.AuthResponse;

import java.util.Map;
import java.util.Optional;

/**
 * 🔐 认证服务接口
 * <p>
 * XV02:04 认证核心业务接口定义
 * 遵循单一职责原则，专注认证逻辑
 *
 * @author xypai
 * @since 1.0.0
 */
public interface AuthService {
    
    /**
     * 🔐 用户认证登录
     *
     * @param authRequest 认证请求
     * @return 认证响应
     */
    Optional<AuthResponse> authenticate(AuthRequest authRequest);
    
    /**
     * 🔄 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @param clientType   客户端类型
     * @return 新的认证响应
     */
    Optional<AuthResponse> refreshToken(String refreshToken, String clientType);
    
    /**
     * ✅ 验证访问令牌
     *
     * @param accessToken 访问令牌
     * @return 令牌信息
     */
    Optional<Map<String, Object>> verifyToken(String accessToken);
    
    /**
     * 👤 获取用户信息
     *
     * @param accessToken 访问令牌
     * @return 用户信息
     */
    Optional<AuthResponse.UserInfo> getUserInfo(String accessToken);
    
    /**
     * 🚪 用户登出
     *
     * @param accessToken 访问令牌
     * @return 是否成功
     */
    boolean logout(String accessToken);
}
