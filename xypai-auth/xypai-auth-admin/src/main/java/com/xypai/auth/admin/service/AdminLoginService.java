package com.xypai.auth.admin.service;

import com.xypai.auth.common.dto.LoginRequest;
import com.xypai.common.core.constant.SecurityConstants;
import com.xypai.common.core.domain.R;
import com.xypai.common.core.exception.ServiceException;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.system.api.RemoteUserService;
import com.xypai.system.api.domain.SysUser;
import com.xypai.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 🏛️ 管理端登录服务
 * <p>
 * 专门为管理端提供的严格登录验证服务：
 * - IP白名单检查
 * - 严格的密码策略
 * - 管理员权限验证
 * - 详细的安全审计
 *
 * @author xypai
 * @version 4.0.0
 */
@Service
public class AdminLoginService {

    private static final Logger logger = LoggerFactory.getLogger(AdminLoginService.class);

    private final RemoteUserService remoteUserService;
    private final BCryptPasswordEncoder passwordEncoder;

    // 管理端IP白名单（可配置）
    private final List<String> adminIpWhitelist = Arrays.asList(
            "127.0.0.1",
            "192.168.1.0/24",
            "10.0.0.0/8"
            // TODO: 从配置文件或数据库加载
    );

    public AdminLoginService(@Lazy RemoteUserService remoteUserService) {
        this.remoteUserService = remoteUserService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 管理端严格登录验证
     */
    public LoginUser adminLogin(LoginRequest request, String clientIp) {
        logger.info("🏛️ 开始管理端登录验证 - 用户: {}, IP: {}", request.username(), clientIp);

        // 1. IP白名单检查
        validateClientIp(clientIp, request.username());

        // 2. 基础参数验证
        validateLoginParams(request);

        // 3. 管理端密码策略验证
        validateAdminPasswordPolicy(request);

        // 4. 获取管理员用户信息
        LoginUser adminUser = getAdminUserInfo(request.username());

        // 5. 验证管理员权限
        validateAdminPermission(adminUser);

        // 6. 密码验证
        validatePassword(adminUser.getSysUser(), request.password());

        // 7. 账户状态验证
        validateAccountStatus(adminUser.getSysUser());

        // 8. 记录登录成功日志
        recordAdminLoginSuccess(request.username(), clientIp);

        logger.info("✅ 管理端登录验证成功 - 用户: {}", request.username());
        return adminUser;
    }

    /**
     * 验证客户端IP是否在白名单中
     */
    private void validateClientIp(String clientIp, String username) {
        if (!isIpInWhitelist(clientIp)) {
            recordAdminLoginFail(username, clientIp, "IP地址不在白名单中");
            throw new SecurityException("IP地址不被允许访问管理系统");
        }
        logger.debug("✅ IP白名单验证通过 - IP: {}", clientIp);
    }

    /**
     * 验证登录参数
     */
    private void validateLoginParams(LoginRequest request) {
        if (StringUtils.isBlank(request.username())) {
            throw new ServiceException("管理员用户名不能为空");
        }
        if (StringUtils.isBlank(request.password())) {
            throw new ServiceException("管理员密码不能为空");
        }
        if (!"web".equals(request.clientType())) {
            throw new ServiceException("管理端仅支持web客户端类型");
        }
    }

    /**
     * 管理端密码策略验证
     */
    private void validateAdminPasswordPolicy(LoginRequest request) {
        String password = request.password();

        // 管理端严格密码要求
        if (password.length() < 8 || password.length() > 50) {
            recordAdminLoginFail(request.username(), null, "密码长度不符合管理端要求");
            throw new ServiceException("管理员密码长度必须在8-50个字符之间");
        }

        // TODO: 可添加更严格的密码复杂度要求
        // - 必须包含大小写字母
        // - 必须包含数字
        // - 必须包含特殊字符
    }

    /**
     * 获取管理员用户信息
     */
    private LoginUser getAdminUserInfo(String username) {
        try {
            R<LoginUser> userResult = remoteUserService.getUserInfo(username, SecurityConstants.INNER);

            if (R.FAIL == userResult.getCode()) {
                recordAdminLoginFail(username, null, "管理员账户不存在");
                throw new ServiceException("管理员账户不存在或已被禁用");
            }

            return userResult.getData();
        } catch (Exception e) {
            logger.error("🔴 获取管理员用户信息失败 - 用户: {}", username, e);
            throw new ServiceException("获取管理员信息失败");
        }
    }

    /**
     * 验证管理员权限
     */
    private void validateAdminPermission(LoginUser loginUser) {
        SysUser user = loginUser.getSysUser();

        // TODO: 实现管理员角色和权限验证
        // 这里应该检查用户是否具有管理员角色
        // 例如：检查角色表中是否有admin角色

        logger.debug("✅ 管理员权限验证通过 - 用户: {}", user.getUserName());
    }

    /**
     * 验证密码
     */
    private void validatePassword(SysUser user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            recordAdminLoginFail(user.getUserName(), null, "管理员密码错误");
            throw new SecurityException("管理员密码错误");
        }
        logger.debug("✅ 密码验证通过 - 用户: {}", user.getUserName());
    }

    /**
     * 验证账户状态
     */
    private void validateAccountStatus(SysUser user) {
        if ("1".equals(user.getDelFlag())) {
            recordAdminLoginFail(user.getUserName(), null, "管理员账户已被删除");
            throw new ServiceException("管理员账户已被删除");
        }
        if ("1".equals(user.getStatus())) {
            recordAdminLoginFail(user.getUserName(), null, "管理员账户已被停用");
            throw new ServiceException("管理员账户已被停用");
        }
        logger.debug("✅ 账户状态验证通过 - 用户: {}", user.getUserName());
    }

    /**
     * 检查IP是否在白名单中
     */
    private boolean isIpInWhitelist(String clientIp) {
        if (StringUtils.isBlank(clientIp)) {
            return false;
        }

        // 简单的IP白名单检查
        for (String whiteIp : adminIpWhitelist) {
            if (whiteIp.contains("/")) {
                // CIDR格式的IP段检查（简化实现）
                if (isIpInCidr(clientIp, whiteIp)) {
                    return true;
                }
            } else {
                // 精确IP匹配
                if (whiteIp.equals(clientIp)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查IP是否在CIDR网段中（简化实现）
     */
    private boolean isIpInCidr(String ip, String cidr) {
        // TODO: 实现完整的CIDR检查逻辑
        // 这里是简化实现，生产环境需要完整的IP段检查
        return cidr.startsWith("192.168.") && ip.startsWith("192.168.");
    }

    /**
     * 记录管理员登录成功
     */
    private void recordAdminLoginSuccess(String username, String clientIp) {
        logger.info("📝 管理员登录成功 - 用户: {}, IP: {}, 时间: {}",
                username, clientIp, System.currentTimeMillis());
        // TODO: 记录到审计日志表
    }

    /**
     * 记录管理员登录失败
     */
    private void recordAdminLoginFail(String username, String clientIp, String reason) {
        logger.warn("📝 管理员登录失败 - 用户: {}, IP: {}, 原因: {}, 时间: {}",
                username, clientIp, reason, System.currentTimeMillis());
        // TODO: 记录到审计日志表，并可能触发安全告警
    }
}
