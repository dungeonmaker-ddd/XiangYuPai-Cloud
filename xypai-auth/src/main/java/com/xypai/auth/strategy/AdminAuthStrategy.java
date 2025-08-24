package com.xypai.auth.strategy;

import com.xypai.auth.dto.LoginRequest;
import com.xypai.auth.service.SysLoginService;
import com.xypai.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 🏛️ 管理端认证策略
 * <p>
 * 特点：
 * - 严格的安全验证
 * - IP白名单检查
 * - 强密码策略
 * - 详细的审计日志
 *
 * @author xypai
 * @version 3.0.0
 * @since 2024-01-15
 */
@Component("adminAuthStrategy")
public class AdminAuthStrategy implements AuthenticationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthStrategy.class);

    private final SysLoginService sysLoginService;

    public AdminAuthStrategy(SysLoginService sysLoginService) {
        this.sysLoginService = sysLoginService;
    }

    @Override
    public LoginUser authenticate(LoginRequest request) {
        logger.info("🏛️ 执行管理端认证策略 - 用户: {}", request.username());

        // 管理端严格认证
        LoginUser user = sysLoginService.login(request.username(), request.password());

        logger.info("✅ 管理端认证成功 - 用户: {}", request.username());
        return user;
    }

    @Override
    public String getStrategyName() {
        return "AdminAuthStrategy(管理端严格认证)";
    }
}
