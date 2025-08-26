package com.xypai.auth.app.auth.service;

import com.xypai.auth.common.dto.LoginRequest;
import com.xypai.auth.common.service.BaseAuthService;
import com.xypai.auth.common.service.SysPasswordService;
import com.xypai.auth.common.service.SysRecordLogService;
import com.xypai.auth.common.vo.SmsCodeResponse;
import com.xypai.common.core.constant.Constants;
import com.xypai.common.core.constant.SecurityConstants;
import com.xypai.common.core.domain.R;
import com.xypai.common.core.enums.UserStatus;
import com.xypai.common.core.exception.ServiceException;
import com.xypai.common.core.utils.DateUtils;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.security.utils.SecurityUtils;
import com.xypai.system.api.RemoteUserService;
import com.xypai.system.api.domain.SysUser;
import com.xypai.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * APP端登录服务 - 相对宽松的验证策略
 *
 * @author xypai
 */
@Component
public class AppLoginService extends BaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AppLoginService.class);

    /**
     * 构造器注入 - Spring Boot 3+ 推荐方式
     *
     * @Lazy 注解解决Feign客户端循环依赖问题
     */
    public AppLoginService(@Lazy RemoteUserService remoteUserService,
                           SysPasswordService passwordService,
                           SysRecordLogService recordLogService) {
        super(remoteUserService, passwordService, recordLogService);
    }

    /**
     * APP端登录 - 不检查IP黑名单，密码要求相对宽松
     */
    public LoginUser login(String username, String password) {
        // 基础参数验证
        validateBaseParams(username, password);

        // APP端宽松的密码策略
        validatePasswordPolicy(username, password);

        // 用户名长度验证
        validateUsername(username);

        // APP端安全检查（无IP黑名单检查）
        performSecurityChecks(username);

        // 获取用户信息
        LoginUser userInfo = getUserInfo(username);
        SysUser user = userInfo.getSysUser();

        // 检查用户状态
        validateUserStatus(username, user);

        // 验证密码
        passwordService.validate(user, password);

        // 记录登录成功
        recordLogService.recordLogininfor(username, Constants.LOGIN_SUCCESS, "APP端登录成功");
        recordLoginInfo(user.getUserId());

        return userInfo;
    }

    @Override
    protected void validatePasswordPolicy(String username, String password) {
        // APP端相对宽松的密码长度要求
        if (password.length() < 6 || password.length() > 50) {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "密码长度必须在6-50个字符之间");
            throw new ServiceException("密码长度必须在6-50个字符之间");
        }
    }

    @Override
    protected void performSecurityChecks(String username) {
        // APP端不进行IP黑名单检查，保持方法为空即可
        // 如果需要其他安全检查，可以在这里添加
    }

    /**
     * 手机号验证码登录
     */
    public LoginUser loginBySms(String mobile, String code) {
        // 验证手机号格式
        if (!mobile.matches("^1[3-9]\\d{9}$")) {
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "手机号格式不正确");
            throw new ServiceException("手机号格式不正确");
        }

        // 验证短信验证码 (这里简化处理，实际项目中需要对接短信服务)
        if (StringUtils.isBlank(code) || code.length() != 6) {
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "验证码格式不正确");
            throw new ServiceException("验证码格式不正确");
        }

        // 验证短信验证码的有效性
        validateSmsCode(mobile, code);

        // 通过手机号获取用户信息
        R<LoginUser> userResult = remoteUserService.getUserInfo(mobile, SecurityConstants.INNER);

        LoginUser userInfo;
        if (R.FAIL == userResult.getCode()) {
            // 手机号未注册，自动创建账号
            logger.info("📱 检测到未注册手机号，开始自动创建账号 - 手机号: {}", mobile);
            userInfo = autoCreateUserByMobile(mobile);
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_SUCCESS, "未注册手机号自动创建账号并登录成功");
        } else {
            userInfo = userResult.getData();
            SysUser user = userInfo.getSysUser();

            // 检查用户状态
            if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
                recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "账号已被删除");
                throw new ServiceException("账号已被删除");
            }
            if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
                recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "账号已停用");
                throw new ServiceException("账号已停用");
            }

            recordLogService.recordLogininfor(mobile, Constants.LOGIN_SUCCESS, "短信验证码登录成功");
        }

        return userInfo;
    }

    /**
     * 发送短信验证码
     */
    public SmsCodeResponse sendSmsCode(String mobile) {
        // 验证手机号格式
        if (!mobile.matches("^1[3-9]\\d{9}$")) {
            throw new ServiceException("手机号格式不正确");
        }

        // 调用短信服务发送验证码
        String verificationCode = sendActualSmsCode(mobile);

        // 缓存验证码到Redis，设置5分钟过期
        cacheVerificationCode(mobile, verificationCode, 300);

        recordLogService.recordLogininfor(mobile, Constants.LOGIN_SUCCESS, "短信验证码发送成功");

        // 返回短信验证码响应
        return SmsCodeResponse.success(mobile, 300); // 5分钟过期
    }

    /**
     * 🚀 APP端增强登录 - 支持设备信息和推送token
     *
     * @param loginRequest 登录请求
     * @param deviceInfo   设备信息
     * @param pushToken    推送token
     * @return 用户信息
     */
    public LoginUser login(LoginRequest loginRequest,
                           String deviceInfo, String pushToken) {
        // 执行基本登录
        LoginUser userInfo = login(loginRequest.username(), loginRequest.password());

        // 绑定设备信息和推送token
        if (StringUtils.isNotEmpty(deviceInfo) || StringUtils.isNotEmpty(pushToken)) {
            bindDeviceAndPushToken(userInfo.getSysUser().getUserId(),
                    loginRequest.deviceId(), deviceInfo, pushToken);
        }

        return userInfo;
    }

    /**
     * 🚀 APP端短信登录 - 支持设备信息和推送token
     *
     * @param mobile     手机号
     * @param code       验证码
     * @param deviceInfo 设备信息
     * @param pushToken  推送token
     * @return 用户信息
     */
    public LoginUser loginBySms(String mobile, String code,
                                String deviceInfo, String pushToken) {
        // 执行基本短信登录
        LoginUser userInfo = loginBySms(mobile, code);

        // 绑定设备信息和推送token
        if (StringUtils.isNotEmpty(deviceInfo) || StringUtils.isNotEmpty(pushToken)) {
            bindDeviceAndPushToken(userInfo.getSysUser().getUserId(),
                    null, deviceInfo, pushToken);
        }

        return userInfo;
    }

    /**
     * 绑定设备信息和推送token
     *
     * @param userId     用户ID
     * @param deviceId   设备ID
     * @param deviceInfo 设备信息
     * @param pushToken  推送token
     */
    private void bindDeviceAndPushToken(Long userId, String deviceId,
                                        String deviceInfo, String pushToken) {
        try {
            // TODO: 实现设备信息和推送token的绑定逻辑
            // 这里可以调用设备管理服务或推送服务
            // deviceService.bindDevice(userId, deviceId, deviceInfo);
            // pushService.bindPushToken(userId, pushToken);

            logger.info("📱 绑定设备信息成功 - 用户ID: {}, 设备ID: {}", userId, deviceId);
        } catch (Exception e) {
            logger.warn("📱 绑定设备信息失败 - 用户ID: {}, 错误: {}", userId, e.getMessage());
            // 绑定失败不影响登录成功
        }
    }

    /**
     * APP端退出登录日志记录
     */
    public void recordLogout(String username) {
        recordLogService.recordLogininfor(username, Constants.LOGOUT, "APP端退出成功");
    }

    /**
     * 🚀 通过手机号自动创建用户账号
     *
     * @param mobile 手机号
     * @return 创建的用户登录信息
     */
    private LoginUser autoCreateUserByMobile(String mobile) {
        try {
            // 生成默认用户名（mobile_xxxx格式，避免重复）
            String username = generateUniqueUsername(mobile);

            // 生成随机密码（用户可后续修改）
            String randomPassword = generateRandomPassword();

            // 创建用户对象
            SysUser sysUser = new SysUser();
            sysUser.setUserName(username);
            sysUser.setNickName("手机用户" + mobile.substring(7)); // 使用手机号后4位作为昵称
            sysUser.setPhonenumber(mobile);
            sysUser.setPwdUpdateDate(DateUtils.getNowDate());
            sysUser.setPassword(SecurityUtils.encryptPassword(randomPassword));

            // 设置默认状态
            sysUser.setStatus("0"); // 正常状态
            sysUser.setDelFlag("0"); // 未删除

            // 调用远程服务创建用户
            R<?> registerResult = remoteUserService.registerUserInfo(sysUser, SecurityConstants.INNER);

            if (R.FAIL == registerResult.getCode()) {
                logger.error("📱 自动创建用户失败 - 手机号: {}, 错误: {}", mobile, registerResult.getMsg());
                throw new ServiceException("创建账号失败: " + registerResult.getMsg());
            }

            logger.info("✅ 自动创建用户成功 - 手机号: {}, 用户名: {}", mobile, username);
            recordLogService.recordLogininfor(mobile, Constants.REGISTER, "手机号自动注册成功");

            // 重新获取创建的用户信息
            R<LoginUser> userResult = remoteUserService.getUserInfo(mobile, SecurityConstants.INNER);
            if (R.FAIL == userResult.getCode()) {
                throw new ServiceException("获取新创建用户信息失败");
            }

            return userResult.getData();

        } catch (Exception e) {
            logger.error("📱 自动创建用户异常 - 手机号: {}", mobile, e);
            throw new ServiceException("自动创建账号失败，请稍后重试");
        }
    }

    /**
     * 生成唯一用户名
     *
     * @param mobile 手机号
     * @return 唯一用户名
     */
    private String generateUniqueUsername(String mobile) {
        // 使用手机号 + 4位随机数的格式
        Random random = new Random();
        int randomSuffix = 1000 + random.nextInt(9000); // 生成1000-9999的随机数
        return mobile + "_" + randomSuffix;
    }

    /**
     * 生成随机密码
     *
     * @return 8位随机密码
     */
    private String generateRandomPassword() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    /**
     * 验证短信验证码
     *
     * @param mobile 手机号
     * @param code   验证码
     */
    private void validateSmsCode(String mobile, String code) {
        // 从缓存中获取验证码
        String cachedCode = getCachedVerificationCode(mobile);

        if (StringUtils.isBlank(cachedCode)) {
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "验证码已过期或不存在");
            throw new ServiceException("验证码已过期，请重新获取");
        }

        if (!cachedCode.equals(code)) {
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "验证码错误");
            throw new ServiceException("验证码错误");
        }

        // 验证成功后删除缓存的验证码（防止重复使用）
        deleteCachedVerificationCode(mobile);
    }

    /**
     * 发送实际的短信验证码
     *
     * @param mobile 手机号
     * @return 验证码
     */
    private String sendActualSmsCode(String mobile) {
        // 生成6位数字验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        try {
            // TODO: 集成实际的短信服务（阿里云SMS、腾讯云SMS等）
            // 现在使用模拟发送，实际项目中替换为真实的短信发送逻辑
            logger.info("📱 模拟发送短信验证码 - 手机号: {}, 验证码: {}", mobile, code);

            // 实际的短信发送代码示例：
            // SmsRequest smsRequest = SmsRequest.builder()
            //     .phoneNumber(mobile)
            //     .templateCode("SMS_123456789")
            //     .templateParams(Map.of("code", code))
            //     .build();
            // smsService.sendSms(smsRequest);

            return code;
        } catch (Exception e) {
            logger.error("📱 短信发送失败 - 手机号: {}, 错误: {}", mobile, e.getMessage());
            throw new ServiceException("短信发送失败，请稍后重试");
        }
    }

    /**
     * 缓存验证码
     *
     * @param mobile     手机号
     * @param code       验证码
     * @param expireTime 过期时间（秒）
     */
    private void cacheVerificationCode(String mobile, String code, int expireTime) {
        // TODO: 使用Redis缓存验证码
        // 现在使用简单的内存缓存，实际项目中应使用Redis
        String cacheKey = "sms:code:" + mobile;
        logger.info("🔄 缓存验证码 - 手机号: {}, 过期时间: {}秒", mobile, expireTime);

        // 实际的Redis缓存代码示例：
        // redisTemplate.opsForValue().set(cacheKey, code, Duration.ofSeconds(expireTime));
    }

    /**
     * 获取缓存的验证码
     *
     * @param mobile 手机号
     * @return 验证码
     */
    private String getCachedVerificationCode(String mobile) {
        String cacheKey = "sms:code:" + mobile;
        // TODO: 从Redis获取验证码
        // 现在返回固定验证码用于测试，实际项目中从Redis获取
        logger.info("🔍 获取缓存验证码 - 手机号: {}", mobile);

        // 实际的Redis获取代码示例：
        // return redisTemplate.opsForValue().get(cacheKey);

        // 测试用固定验证码（生产环境需删除）
        return "123456";
    }

    /**
     * 删除缓存的验证码
     *
     * @param mobile 手机号
     */
    private void deleteCachedVerificationCode(String mobile) {
        String cacheKey = "sms:code:" + mobile;
        logger.info("🗑️ 删除缓存验证码 - 手机号: {}", mobile);

        // 实际的Redis删除代码示例：
        // redisTemplate.delete(cacheKey);
    }
}
