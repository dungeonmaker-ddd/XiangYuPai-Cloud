package com.xypai.auth.service.impl;

import com.xypai.auth.domain.dto.LoginDTO;
import com.xypai.auth.domain.dto.SmsLoginDTO;
import com.xypai.auth.domain.dto.SmsCodeDTO;
import com.xypai.auth.domain.vo.LoginResultVO;
import com.xypai.auth.feign.UserServiceFeign;
import com.xypai.auth.feign.dto.AuthUserDTO;
import com.xypai.auth.feign.dto.AutoRegisterDTO;
import com.xypai.auth.feign.dto.UserValidateDTO;
import com.xypai.auth.service.IAuthService;
import com.xypai.auth.utils.JwtUtils;
import com.xypai.common.core.domain.R;
import com.xypai.common.core.exception.ServiceException;
import com.xypai.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserServiceFeign userServiceFeign;
    private final JwtUtils jwtUtils;
    private final RedisService redisService;

    private static final String SMS_CODE_PREFIX = "auth:sms:";
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";
    private static final int SMS_CODE_EXPIRE_MINUTES = 5;
    private static final int SMS_SEND_INTERVAL_SECONDS = 60;

    @Override
    public LoginResultVO loginWithPassword(LoginDTO loginDTO) {
        log.info("密码登录请求: username={}", loginDTO.getUsername());

        try {
            // 1. 验证用户密码
            AuthUserDTO user = authenticateUserWithPassword(loginDTO);
            if (user == null) {
                throw new ServiceException("用户名或密码错误");
            }

            // 2. 检查用户状态
            if (!user.isNormal()) {
                throw new ServiceException("用户账户异常，请联系管理员");
            }

            // 3. 生成令牌并返回结果
            return generateLoginResult(user, loginDTO.getClientType(), loginDTO.getDeviceId());

        } catch (ServiceException e) {
            log.warn("密码登录失败: username={}, error={}", loginDTO.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("密码登录异常: username={}, error={}", loginDTO.getUsername(), e.getMessage(), e);
            throw new ServiceException("登录失败，请稍后重试");
        }
    }

    @Override
    public LoginResultVO loginWithSms(SmsLoginDTO smsLoginDTO) {
        log.info("短信登录请求: mobile={}", smsLoginDTO.getMobile());

        try {
            // 1. 验证并消费短信验证码
            if (!verifyAndConsumeSmsCode(smsLoginDTO.getMobile(), smsLoginDTO.getSmsCode())) {
                throw new ServiceException("验证码错误或已过期");
            }

            // 2. 获取用户信息，如果不存在则自动注册
            AuthUserDTO user = authenticateUserWithMobile(smsLoginDTO.getMobile());
            if (user == null) {
                // 尝试自动注册用户
                user = autoRegisterUserForSmsLogin(smsLoginDTO);
                if (user == null) {
                    throw new ServiceException("自动注册失败，请联系管理员");
                }
                log.info("短信登录自动注册成功: mobile={}, userId={}", smsLoginDTO.getMobile(), user.getId());
            }

            // 3. 检查用户状态
            if (!user.isNormal()) {
                throw new ServiceException("用户账户异常，请联系管理员");
            }

            // 4. 生成令牌并返回结果
            return generateLoginResult(user, smsLoginDTO.getClientType(), smsLoginDTO.getDeviceId());

        } catch (ServiceException e) {
            log.warn("短信登录失败: mobile={}, error={}", smsLoginDTO.getMobile(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("短信登录异常: mobile={}, error={}", smsLoginDTO.getMobile(), e.getMessage(), e);
            throw new ServiceException("登录失败，请稍后重试");
        }
    }

    @Override
    public LoginResultVO refreshToken(String refreshToken) {
        log.info("刷新令牌请求");

        try {
            // 1. 验证刷新令牌
            if (!jwtUtils.validateToken(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
                throw new ServiceException("无效的刷新令牌");
            }

            // 2. 从令牌中提取用户信息
            AuthUserDTO user = jwtUtils.extractUserInfo(refreshToken);
            
            // 3. 重新验证用户状态
            R<AuthUserDTO> userResult = userServiceFeign.getUserByUsername(user.getUsername());
            if (!R.isSuccess(userResult) || userResult.getData() == null) {
                throw new ServiceException("用户不存在");
            }

            AuthUserDTO currentUser = userResult.getData();
            if (!currentUser.isNormal()) {
                throw new ServiceException("用户账户异常");
            }

            // 4. 使用统一的令牌生成方法
            return generateLoginResult(currentUser, "web", null);

        } catch (ServiceException e) {
            log.warn("令牌刷新失败: error={}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("令牌刷新异常: error={}", e.getMessage(), e);
            throw new ServiceException("令牌刷新失败");
        }
    }

    @Override
    public boolean logout(String accessToken) {
        log.info("用户登出请求");

        try {
            // 1. 验证令牌
            if (!jwtUtils.validateToken(accessToken)) {
                return false;
            }

            // 2. 将令牌加入黑名单
            String username = jwtUtils.getUsernameFromToken(accessToken);
            long remainingTime = jwtUtils.getTokenRemainingTime(accessToken);
            
            if (remainingTime > 0) {
                redisService.setCacheObject(
                    TOKEN_BLACKLIST_PREFIX + accessToken, 
                    username, 
                    remainingTime, 
                    TimeUnit.SECONDS
                );
            }

            log.info("用户登出成功: username={}", username);
            return true;

        } catch (Exception e) {
            log.error("用户登出异常: error={}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> verifyToken(String accessToken) {
        try {
            // 1. 检查令牌是否在黑名单中
            if (redisService.hasKey(TOKEN_BLACKLIST_PREFIX + accessToken)) {
                return null;
            }

            // 2. 验证令牌
            if (!jwtUtils.validateToken(accessToken) || !jwtUtils.isAccessToken(accessToken)) {
                return null;
            }

            // 3. 获取令牌信息
            Map<String, Object> claims = jwtUtils.getAllClaimsFromToken(accessToken);
            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("user_id", claims.get("user_id"));
            result.put("username", claims.get("username"));
            result.put("roles", claims.get("roles"));
            result.put("permissions", claims.get("permissions"));
            result.put("remaining_time", jwtUtils.getTokenRemainingTime(accessToken));

            return result;

        } catch (Exception e) {
            log.debug("令牌验证失败: error={}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean sendSmsCode(SmsCodeDTO smsCodeDTO) {
        log.info("发送短信验证码: mobile={}, type={}", smsCodeDTO.getMobile(), smsCodeDTO.getType());

        try {
            String key = SMS_CODE_PREFIX + smsCodeDTO.getMobile();
            
            // 1. 检查发送频率
            if (redisService.hasKey(key + ":send_time")) {
                throw new ServiceException("发送过于频繁，请稍后重试");
            }

            // 2. 生成验证码
            String code = generateSmsCode();

            // 3. 模拟发送短信 (实际应调用短信服务)
            boolean sent = mockSendSms(smsCodeDTO.getMobile(), code);
            if (!sent) {
                throw new ServiceException("短信发送失败");
            }

            // 4. 存储验证码
            redisService.setCacheObject(key, code, (long) SMS_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            redisService.setCacheObject(key + ":send_time", System.currentTimeMillis(), (long) SMS_SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

            log.info("短信验证码发送成功: mobile={}", smsCodeDTO.getMobile());
            return true;

        } catch (ServiceException e) {
            log.warn("短信验证码发送失败: mobile={}, error={}", smsCodeDTO.getMobile(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("短信验证码发送异常: mobile={}, error={}", smsCodeDTO.getMobile(), e.getMessage(), e);
            throw new ServiceException("短信发送失败");
        }
    }

    @Override
    public boolean verifySmsCode(String mobile, String code) {
        try {
            String key = SMS_CODE_PREFIX + mobile;
            String storedCode = redisService.getCacheObject(key);
            
            if (storedCode == null) {
                return false;
            }

            return storedCode.equals(code);

        } catch (Exception e) {
            log.error("验证码验证异常: mobile={}, error={}", mobile, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证并消费短信验证码
     */
    private boolean verifyAndConsumeSmsCode(String mobile, String code) {
        try {
            String key = SMS_CODE_PREFIX + mobile;
            String storedCode = redisService.getCacheObject(key);
            
            if (storedCode == null) {
                return false;
            }

            boolean valid = storedCode.equals(code);
            if (valid) {
                // 验证成功后删除验证码
                redisService.deleteObject(key);
            }

            return valid;

        } catch (Exception e) {
            log.error("验证码验证异常: mobile={}, error={}", mobile, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 密码验证用户
     */
    private AuthUserDTO authenticateUserWithPassword(LoginDTO loginDTO) {
        try {
            // 1. 获取用户信息
            R<AuthUserDTO> userResult = userServiceFeign.getUserByUsername(loginDTO.getUsername());
            if (!R.isSuccess(userResult) || userResult.getData() == null) {
                return null;
            }

            AuthUserDTO user = userResult.getData();

            // 2. 验证密码
            UserValidateDTO validateDTO = UserValidateDTO.builder()
                    .username(loginDTO.getUsername())
                    .password(loginDTO.getPassword())
                    .build();

            R<Boolean> validateResult = userServiceFeign.validatePassword(validateDTO);
            if (!R.isSuccess(validateResult) || !Boolean.TRUE.equals(validateResult.getData())) {
                return null;
            }

            return user;

        } catch (Exception e) {
            log.error("密码验证异常: username={}, error={}", loginDTO.getUsername(), e.getMessage());
            return null;
        }
    }

    /**
     * 手机号验证用户
     */
    private AuthUserDTO authenticateUserWithMobile(String mobile) {
        try {
            // 获取用户信息（手机号登录）
            R<AuthUserDTO> userResult = userServiceFeign.getUserByMobile(mobile);
            if (!R.isSuccess(userResult) || userResult.getData() == null) {
                return null;
            }

            return userResult.getData();

        } catch (Exception e) {
            log.error("手机号验证异常: mobile={}, error={}", mobile, e.getMessage());
            return null;
        }
    }

    /**
     * 生成登录结果
     */
    private LoginResultVO generateLoginResult(AuthUserDTO user, String clientType, String deviceId) {
        // 1. 生成令牌
        String accessToken = jwtUtils.generateAccessToken(user, clientType, deviceId);
        String refreshToken = jwtUtils.generateRefreshToken(user, clientType, deviceId);

        // 2. 更新用户最后登录时间
        updateLastLoginTime(user.getId());

        // 3. 构建返回结果
        LoginResultVO.UserInfo userInfo = buildUserInfo(user);
        LoginResultVO result = LoginResultVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24小时
                .userInfo(userInfo)
                .build();

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return result;
    }

    /**
     * 更新最后登录时间
     */
    private void updateLastLoginTime(Long userId) {
        try {
            userServiceFeign.updateLastLoginTime(userId);
        } catch (Exception e) {
            log.warn("更新用户登录时间失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 构建用户信息
     */
    private LoginResultVO.UserInfo buildUserInfo(AuthUserDTO user) {
        return LoginResultVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .mobile(user.getMaskedMobile())
                .status(user.getStatus())
                .roles(user.getRoles() != null ? user.getRoles() : Set.of("USER"))
                .permissions(user.getPermissions() != null ? user.getPermissions() : Set.of("user:read"))
                .lastLoginTime(LocalDateTime.now())
                .build();
    }

    /**
     * 生成6位数字验证码
     */
    private String generateSmsCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    /**
     * 模拟发送短信
     */
    private boolean mockSendSms(String mobile, String code) {
        // 实际应调用短信服务
        log.info("模拟发送短信: mobile={}, code={}", mobile, code);
        return true;
    }

    /**
     * 短信登录自动注册用户
     */
    private AuthUserDTO autoRegisterUserForSmsLogin(SmsLoginDTO smsLoginDTO) {
        try {
            AutoRegisterDTO autoRegisterDTO = AutoRegisterDTO.builder()
                    .mobile(smsLoginDTO.getMobile())
                    .source("sms_login")
                    .clientType(smsLoginDTO.getClientType())
                    .deviceId(smsLoginDTO.getDeviceId())
                    .build();

            R<AuthUserDTO> result = userServiceFeign.autoRegisterUser(autoRegisterDTO);
            if (R.isSuccess(result) && result.getData() != null) {
                log.info("自动注册用户成功: mobile={}", smsLoginDTO.getMobile());
                return result.getData();
            } else {
                log.error("自动注册用户失败: mobile={}, msg={}", smsLoginDTO.getMobile(), result.getMsg());
                return null;
            }

        } catch (Exception e) {
            log.error("自动注册用户异常: mobile={}, error={}", smsLoginDTO.getMobile(), e.getMessage(), e);
            return null;
        }
    }
}
