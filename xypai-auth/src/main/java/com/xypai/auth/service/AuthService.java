package com.xypai.auth.service;

import com.xypai.auth.config.TokenConfig;
import com.xypai.auth.dto.LoginRequest;
import com.xypai.auth.dto.SmsLoginRequest;
import com.xypai.auth.strategy.AuthStrategyFactory;
import com.xypai.auth.strategy.AuthenticationStrategy;
import com.xypai.auth.vo.LoginResponse;
import com.xypai.auth.vo.SmsCodeResponse;
import com.xypai.common.core.domain.R;
import com.xypai.common.core.utils.JwtUtils;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.security.auth.AuthUtil;
import com.xypai.common.security.service.TokenService;
import com.xypai.common.security.utils.SecurityUtils;
import com.xypai.system.api.domain.SysUser;
import com.xypai.system.api.model.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 🎯 认证服务核心类
 * <p>
 * 处理所有认证相关的业务逻辑
 *
 * @author xypai
 * @version 4.1.0
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final TokenService tokenService;
    private final AuthStrategyFactory strategyFactory;
    private final TokenConfig tokenConfig;

    public AuthService(TokenService tokenService,
                       AuthStrategyFactory strategyFactory,
                       TokenConfig tokenConfig) {
        this.tokenService = tokenService;
        this.strategyFactory = strategyFactory;
        this.tokenConfig = tokenConfig;
    }

    /**
     * 处理统一登录
     */
    public ResponseEntity<R<LoginResponse>> processLogin(LoginRequest request) {
        try {
            logger.info("🎯 收到统一登录请求 - 用户: {}, 客户端: {}",
                    request.username(), request.clientType());

            // 执行认证策略
            AuthenticationStrategy strategy = strategyFactory.getStrategy(request.clientType());
            LoginUser userInfo = strategy.authenticate(request);

            // 创建token并构建响应
            String token = createTokenForUser(userInfo);
            LoginResponse response = buildLoginResponse(token, userInfo, request.clientType());

            logger.info("✅ 统一登录成功 - 用户: {}, 客户端: {}",
                    request.username(), request.clientType());

            return ResponseEntity.ok(R.ok(response));

        } catch (IllegalArgumentException e) {
            logger.warn("❌ 登录参数错误 - 用户: {}, 错误: {}", request.username(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(R.fail("参数错误：" + e.getMessage()));
        } catch (SecurityException e) {
            logger.warn("🚫 登录认证失败 - 用户: {}, 错误: {}", request.username(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(R.fail("认证失败：" + e.getMessage()));
        } catch (Exception e) {
            logger.error("💥 登录系统异常 - 用户: {}", request.username(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(R.fail("系统异常，请稍后重试"));
        }
    }

    /**
     * 处理短信登录
     */
    public ResponseEntity<R<LoginResponse>> processSmsLogin(SmsLoginRequest request) {
        try {
            logger.info("📱 收到短信登录请求 - 手机号: {}, 客户端: {}",
                    request.mobile(), request.clientType());

            // 短信登录仅支持移动端
            if (!"app".equals(request.clientType()) && !"mini".equals(request.clientType())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(R.fail("短信登录仅支持 app 或 mini 客户端类型"));
            }

            // 使用app策略处理短信登录
            AuthenticationStrategy strategy = strategyFactory.getStrategy("app");
            LoginUser userInfo = strategy.authenticateBySms(request);

            String token = createTokenForUser(userInfo);
            LoginResponse response = buildLoginResponse(token, userInfo, request.clientType());

            logger.info("✅ 短信登录成功 - 手机号: {}, 客户端: {}",
                    request.mobile(), request.clientType());
            return ResponseEntity.ok(R.ok(response));

        } catch (Exception e) {
            logger.error("💥 短信登录异常 - 手机号: {}", request.mobile(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(R.fail("登录失败：" + e.getMessage()));
        }
    }

    /**
     * 发送短信验证码
     */
    public ResponseEntity<R<SmsCodeResponse>> sendSmsCode(String mobile, String clientType) {
        try {
            logger.info("📱 收到发送短信验证码请求 - 手机号: {}, 客户端: {}", mobile, clientType);

            AuthenticationStrategy strategy = strategyFactory.getStrategy("app");
            SmsCodeResponse response = strategy.sendSmsCode(mobile);

            logger.info("📤 短信验证码发送成功 - 手机号: {}", mobile);
            return ResponseEntity.ok(R.ok(response));

        } catch (Exception e) {
            logger.error("💥 发送短信验证码异常 - 手机号: {}", mobile, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(R.fail("发送失败，请稍后重试"));
        }
    }

    /**
     * 处理退出登录
     */
    public ResponseEntity<R<Void>> processLogout(HttpServletRequest request) {
        String token = SecurityUtils.getToken(request);

        if (StringUtils.isNotEmpty(token)) {
            String username = JwtUtils.getUserName(token);
            AuthUtil.logoutByToken(token);
            logger.info("🚪 用户退出登录 - 用户: {}", username);
        }

        return ResponseEntity.ok(R.ok());
    }

    /**
     * 刷新令牌
     */
    public ResponseEntity<R<LoginResponse>> refreshToken(HttpServletRequest request) {
        LoginUser loginUser = tokenService.getLoginUser(request);

        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(R.fail("无效的token"));
        }

        tokenService.refreshToken(loginUser);
        String clientType = getClientTypeFromToken(SecurityUtils.getToken(request));
        String newToken = createTokenForUser(loginUser);
        LoginResponse response = buildLoginResponse(newToken, loginUser, clientType);

        return ResponseEntity.ok(R.ok(response));
    }

    /**
     * 获取当前用户信息
     */
    public ResponseEntity<R<Object>> getCurrentUserInfo(HttpServletRequest request) {
        try {
            LoginUser loginUser = tokenService.getLoginUser(request);

            if (loginUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(R.fail("用户未登录"));
            }

            String clientType = getClientTypeFromToken(SecurityUtils.getToken(request));
            SysUser user = loginUser.getSysUser();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("username", user.getUserName());
            userInfo.put("nickname", StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : user.getUserName());
            userInfo.put("email", user.getEmail());
            userInfo.put("phoneNumber", user.getPhonenumber());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("status", user.getStatus());
            userInfo.put("loginTime", loginUser.getLoginTime());
            userInfo.put("expireTime", loginUser.getExpireTime());
            userInfo.put("clientType", clientType);

            return ResponseEntity.ok(R.ok(userInfo));
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(R.fail("获取用户信息失败"));
        }
    }

    /**
     * 验证token有效性
     */
    public ResponseEntity<R<Object>> validateToken(HttpServletRequest request) {
        try {
            LoginUser loginUser = tokenService.getLoginUser(request);

            if (loginUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(R.fail("token无效"));
            }

            String clientType = getClientTypeFromToken(SecurityUtils.getToken(request));

            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("username", loginUser.getUsername());
            result.put("clientType", clientType);
            result.put("expireTime", loginUser.getExpireTime());
            result.put("remainingTime", loginUser.getExpireTime() - System.currentTimeMillis());

            return ResponseEntity.ok(R.ok(result));
        } catch (Exception e) {
            logger.error("token验证失败", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(R.fail("token验证失败"));
        }
    }

    /**
     * 为用户创建token（分离副作用）
     */
    private String createTokenForUser(LoginUser userInfo) {
        Map<String, Object> tokenMap = tokenService.createToken(userInfo);
        return (String) tokenMap.get("access_token");
    }

    /**
     * 构建登录响应（纯函数）
     */
    private LoginResponse buildLoginResponse(String token, LoginUser userInfo, String clientType) {
        SysUser user = userInfo.getSysUser();
        Long expiresIn = tokenConfig.getExpireTime(clientType);

        return LoginResponse.of(
                token,
                "Bearer",
                expiresIn,
                user.getUserName(),
                StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : user.getUserName()
        );
    }

    /**
     * 从token中获取客户端类型（临时实现）
     */
    private String getClientTypeFromToken(String token) {
        // TODO: 从JWT中解析clientType
        return "web";
    }
}
