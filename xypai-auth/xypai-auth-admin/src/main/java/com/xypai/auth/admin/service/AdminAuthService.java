package com.xypai.auth.admin.service;

import com.xypai.auth.common.dto.LoginRequest;
import com.xypai.auth.common.vo.LoginResponse;
import com.xypai.common.core.domain.R;
import com.xypai.common.core.utils.JwtUtils;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.core.utils.ip.IpUtils;
import com.xypai.common.security.auth.AuthUtil;
import com.xypai.common.security.service.TokenService;
import com.xypai.common.security.utils.SecurityUtils;
import com.xypai.system.api.model.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🏛️ 管理端认证服务
 * <p>
 * 独立的管理端认证功能，支持：
 * - 严格的管理员登录验证
 * - IP白名单检查
 * - 会话管理和监控
 * - 权限验证
 * - 安全审计
 *
 * @author xypai
 * @version 4.0.0
 */
@Service
public class AdminAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthService.class);

    private final TokenService tokenService;
    private final AdminLoginService adminLoginService;

    // 在线管理员缓存
    private final Map<String, AdminSession> onlineAdmins = new ConcurrentHashMap<>();

    public AdminAuthService(TokenService tokenService,
                            AdminLoginService adminLoginService) {
        this.tokenService = tokenService;
        this.adminLoginService = adminLoginService;
    }

    /**
     * 处理管理端登录
     */
    public ResponseEntity<R<LoginResponse>> processAdminLogin(LoginRequest request,
                                                              HttpServletRequest httpRequest) {
        try {
            String clientIp = IpUtils.getIpAddr(httpRequest);
            logger.info("🏛️ 收到管理端登录请求 - 用户: {}, IP: {}", request.username(), clientIp);

            // 强制设置客户端类型为web（管理端）
            LoginRequest adminRequest = LoginRequest.web(request.username(), request.password());

            // 执行严格的管理端认证
            LoginUser adminInfo = adminLoginService.adminLogin(adminRequest, clientIp);

            // 创建管理端token
            String token = createAdminToken(adminInfo);
            LoginResponse response = buildAdminLoginResponse(token, adminInfo);

            // 记录在线管理员
            recordOnlineAdmin(adminInfo, token, clientIp);

            logger.info("✅ 管理端登录成功 - 用户: {}, IP: {}", request.username(), clientIp);
            return ResponseEntity.ok(R.ok(response));

        } catch (SecurityException e) {
            logger.warn("🚫 管理端认证失败 - 用户: {}, 错误: {}", request.username(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(R.fail("认证失败：" + e.getMessage()));
        } catch (Exception e) {
            logger.error("💥 管理端登录异常 - 用户: {}", request.username(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(R.fail("系统异常，请稍后重试"));
        }
    }

    /**
     * 处理管理端退出登录
     */
    public ResponseEntity<R<Void>> processAdminLogout(HttpServletRequest request) {
        String token = SecurityUtils.getToken(request);

        if (StringUtils.isNotEmpty(token)) {
            String username = JwtUtils.getUserName(token);

            // 移除在线管理员记录
            removeOnlineAdmin(token);

            // 注销token
            AuthUtil.logoutByToken(token);

            logger.info("🚪 管理员退出登录 - 用户: {}", username);
        }

        return ResponseEntity.ok(R.ok());
    }

    /**
     * 刷新管理端令牌
     */
    public ResponseEntity<R<LoginResponse>> refreshAdminToken(HttpServletRequest request) {
        LoginUser loginUser = tokenService.getLoginUser(request);

        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(R.fail("无效的管理员token"));
        }

        // 验证是否为管理员权限
        if (!isAdminUser(loginUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(R.fail("权限不足"));
        }

        tokenService.refreshToken(loginUser);
        String newToken = createAdminToken(loginUser);
        LoginResponse response = buildAdminLoginResponse(newToken, loginUser);

        // 更新在线管理员记录
        updateOnlineAdmin(SecurityUtils.getToken(request), newToken);

        return ResponseEntity.ok(R.ok(response));
    }

    /**
     * 获取当前管理员信息
     */
    public ResponseEntity<R<Object>> getCurrentAdminInfo(HttpServletRequest request) {
        try {
            LoginUser loginUser = tokenService.getLoginUser(request);

            if (loginUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(R.fail("管理员未登录"));
            }

            if (!isAdminUser(loginUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(R.fail("权限不足"));
            }

            Map<String, Object> adminInfo = buildAdminInfoMap(loginUser);
            return ResponseEntity.ok(R.ok(adminInfo));
        } catch (Exception e) {
            logger.error("获取管理员信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(R.fail("获取管理员信息失败"));
        }
    }

    /**
     * 验证管理员权限
     */
    public ResponseEntity<R<Object>> validateAdminPermission(String permission,
                                                             HttpServletRequest request) {
        try {
            LoginUser loginUser = tokenService.getLoginUser(request);

            if (loginUser == null || !isAdminUser(loginUser)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(R.fail("权限验证失败"));
            }

            // TODO: 实现具体的权限验证逻辑
            boolean hasPermission = checkAdminPermission(loginUser, permission);

            Map<String, Object> result = new HashMap<>();
            result.put("hasPermission", hasPermission);
            result.put("permission", permission);
            result.put("username", loginUser.getUsername());

            return ResponseEntity.ok(R.ok(result));
        } catch (Exception e) {
            logger.error("权限验证异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(R.fail("权限验证失败"));
        }
    }

    /**
     * 获取在线管理员列表
     */
    public ResponseEntity<R<Object>> getOnlineAdmins() {
        Map<String, Object> result = new HashMap<>();
        result.put("onlineCount", onlineAdmins.size());
        result.put("admins", onlineAdmins.values());
        return ResponseEntity.ok(R.ok(result));
    }

    /**
     * 强制管理员下线
     */
    public ResponseEntity<R<Void>> forceAdminLogout(Long adminId, HttpServletRequest request) {
        // TODO: 实现强制下线逻辑
        logger.info("🔨 强制下线管理员 - 管理员ID: {}", adminId);
        return ResponseEntity.ok(R.ok());
    }

    // ==================== 私有方法 ====================

    /**
     * 创建管理端token
     */
    private String createAdminToken(LoginUser userInfo) {
        Map<String, Object> tokenMap = tokenService.createToken(userInfo);
        return (String) tokenMap.get("access_token");
    }

    /**
     * 构建管理端登录响应
     */
    private LoginResponse buildAdminLoginResponse(String token, LoginUser userInfo) {
        return LoginResponse.of(
                token,
                "Bearer",
                7200L, // 管理端token有效期2小时
                userInfo.getUsername(),
                userInfo.getSysUser().getNickName()
        );
    }

    /**
     * 记录在线管理员
     */
    private void recordOnlineAdmin(LoginUser adminInfo, String token, String clientIp) {
        AdminSession session = new AdminSession(
                adminInfo.getUsername(),
                adminInfo.getSysUser().getNickName(),
                clientIp,
                System.currentTimeMillis()
        );
        onlineAdmins.put(token, session);
    }

    /**
     * 移除在线管理员记录
     */
    private void removeOnlineAdmin(String token) {
        onlineAdmins.remove(token);
    }

    /**
     * 更新在线管理员记录
     */
    private void updateOnlineAdmin(String oldToken, String newToken) {
        AdminSession session = onlineAdmins.remove(oldToken);
        if (session != null) {
            onlineAdmins.put(newToken, session);
        }
    }

    /**
     * 验证是否为管理员用户
     */
    private boolean isAdminUser(LoginUser loginUser) {
        // TODO: 实现管理员身份验证逻辑
        return true; // 临时实现
    }

    /**
     * 检查管理员权限
     */
    private boolean checkAdminPermission(LoginUser loginUser, String permission) {
        // TODO: 实现具体的权限检查逻辑
        return true; // 临时实现
    }

    /**
     * 构建管理员信息Map
     */
    private Map<String, Object> buildAdminInfoMap(LoginUser loginUser) {
        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("userId", loginUser.getSysUser().getUserId());
        adminInfo.put("username", loginUser.getUsername());
        adminInfo.put("nickname", loginUser.getSysUser().getNickName());
        adminInfo.put("email", loginUser.getSysUser().getEmail());
        adminInfo.put("loginTime", loginUser.getLoginTime());
        adminInfo.put("expireTime", loginUser.getExpireTime());
        adminInfo.put("clientType", "admin");
        adminInfo.put("permissions", getAdminPermissions(loginUser));
        return adminInfo;
    }

    /**
     * 获取管理员权限列表
     */
    private Object getAdminPermissions(LoginUser loginUser) {
        // TODO: 实现权限获取逻辑
        return new String[]{"system:admin", "user:manage", "role:manage"};
    }

    /**
     * 管理员会话信息
     */
    public static class AdminSession {
        private final String username;
        private final String nickname;
        private final String clientIp;
        private final long loginTime;

        public AdminSession(String username, String nickname, String clientIp, long loginTime) {
            this.username = username;
            this.nickname = nickname;
            this.clientIp = clientIp;
            this.loginTime = loginTime;
        }

        // Getters
        public String getUsername() {
            return username;
        }

        public String getNickname() {
            return nickname;
        }

        public String getClientIp() {
            return clientIp;
        }

        public long getLoginTime() {
            return loginTime;
        }
    }
}
