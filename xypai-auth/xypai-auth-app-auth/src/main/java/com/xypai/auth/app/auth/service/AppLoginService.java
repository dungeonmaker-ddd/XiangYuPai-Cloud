package com.xypai.auth.app.auth.service;

import com.xypai.auth.common.dto.LoginRequest;
import com.xypai.auth.common.service.BaseAuthService;
import com.xypai.auth.common.service.SysPasswordService;
import com.xypai.auth.common.service.SysRecordLogService;
import com.xypai.auth.common.vo.SmsCodeResponse;
import com.xypai.common.core.constant.Constants;
import com.xypai.common.core.constant.SecurityConstants;
import com.xypai.common.core.domain.R;
import com.xypai.common.core.exception.ServiceException;
import com.xypai.common.core.utils.DateUtils;
import com.xypai.common.core.utils.StringUtils;
import com.xypai.common.redis.service.RedisService;
import com.xypai.common.security.utils.SecurityUtils;
import com.xypai.system.api.RemoteUserService;
import com.xypai.system.api.domain.SysUser;
import com.xypai.system.api.model.LoginUser;
import com.xypai.user.domain.record.AppUserRegisterRequest;
import com.xypai.user.domain.record.AppUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * APP端登录服务 - 相对宽松的验证策略
 *
 * @author xypai
 */
@Component
public class AppLoginService extends BaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AppLoginService.class);

    private final RedisService redisService;
    private final RemoteAppUserService remoteAppUserService;

    /**
     * 构造器注入 - Spring Boot 3+ 推荐方式
     *
     * @Lazy 注解解决Feign客户端循环依赖问题
     */
    public AppLoginService(@Lazy RemoteUserService remoteUserService,
                           SysPasswordService passwordService,
                           SysRecordLogService recordLogService,
                           RedisService redisService,
                           @Lazy RemoteAppUserService remoteAppUserService) {
        super(remoteUserService, passwordService, recordLogService);
        this.redisService = redisService;
        this.remoteAppUserService = remoteAppUserService;
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
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "密码长度不符合要求");
            throw new ServiceException("密码长度必须在6-50个字符之间");
        }
    }

    @Override
    protected void performSecurityChecks(String username) {
        // APP端不进行IP黑名单检查，保持方法为空即可
        // 如果需要其他安全检查，可以在这里添加
    }

    /**
     * 手机号验证码登录 - 使用新的APP用户服务
     */
    public LoginUser loginBySms(String mobile, String code) {
        return loginBySms(mobile, code, "app");
    }

    /**
     * 手机号验证码登录（带客户端类型）
     */
    public LoginUser loginBySms(String mobile, String code, String clientType) {
        // 验证手机号格式
        if (!mobile.matches("^1[3-9]\\d{9}$")) {
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "手机号格式错误");
            throw new ServiceException("手机号格式不正确");
        }

        // 验证短信验证码
        if (StringUtils.isBlank(code) || code.length() != 6) {
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "验证码格式错误");
            throw new ServiceException("验证码格式不正确");
        }

        // 验证短信验证码的有效性
        validateSmsCode(mobile, code);

        // 使用远程APP用户服务查询用户
        Optional<AppUserResponse> appUserOpt;
        try {
            appUserOpt = remoteAppUserService.getByMobile(mobile);
        } catch (Exception e) {
            logger.error("APP用户服务：查询用户失败 - 手机号: {}, 错误: {}", mobile, e.getMessage());
            // 如果远程服务不可用，返回空Optional，触发自动创建用户流程
            appUserOpt = Optional.empty();
        }
        
        LoginUser userInfo;
        if (appUserOpt.isEmpty()) {
            // 手机号未注册，自动创建账号
            logger.info("APP用户服务：检测到未注册手机号，开始自动创建账号 - 手机号: {}", mobile);
            AppUserResponse appUser = autoCreateAppUser(mobile, clientType);

            // 将APP用户转换为LoginUser（保持向后兼容）
            userInfo = convertAppUserToLoginUser(appUser);
            
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_SUCCESS, "自动注册并登录成功");
        } else {
            AppUserResponse appUser = appUserOpt.get();
            
            // 检查用户状态
            if (appUser.status() == 0) {
                recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "账号已停用");
                throw new ServiceException("账号已停用");
            }

            // 更新最后登录时间
            remoteAppUserService.updateLastLoginTime(appUser.userId());

            // 将APP用户转换为LoginUser
            userInfo = convertAppUserToLoginUser(appUser);
            
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_SUCCESS, "短信登录成功");
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

        // 检查发送频率限制
        checkSmsFrequencyLimit(mobile);

        // 调用短信服务发送验证码
        String verificationCode = sendActualSmsCode(mobile);

        // 缓存验证码到Redis，设置5分钟过期
        cacheVerificationCode(mobile, verificationCode, 300);

        // 缓存发送时间，用于频率限制（5秒 - 测试用）
        cacheSmsFrequencyLimit(mobile, 5);

        recordLogService.recordLogininfor(mobile, Constants.LOGIN_SUCCESS, "短信发送成功");

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
        return loginBySms(mobile, code, "app", deviceInfo, pushToken);
    }

    /**
     * 🚀 APP端短信登录 - 完整版本
     *
     * @param mobile     手机号
     * @param code       验证码
     * @param clientType 客户端类型
     * @param deviceInfo 设备信息
     * @param pushToken  推送token
     * @return 用户信息
     */
    public LoginUser loginBySms(String mobile, String code, String clientType,
                                String deviceInfo, String pushToken) {
        // 执行基本短信登录
        LoginUser userInfo = loginBySms(mobile, code, clientType);

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
     * 🚀 通过手机号自动创建APP用户账号
     *
     * @param mobile 手机号
     * @param clientType 客户端类型
     * @return 创建的APP用户信息
     */
    private AppUserResponse autoCreateAppUser(String mobile, String clientType) {
        try {
            // 生成默认昵称
            String nickname = "手机用户" + mobile.substring(7);

            // 创建注册请求
            AppUserRegisterRequest registerRequest = AppUserRegisterRequest.of(mobile, nickname, clientType);

            // 调用远程APP用户服务注册
            AppUserResponse appUser = remoteAppUserService.register(registerRequest);

            logger.info("APP用户服务：自动创建用户成功 - 手机号: {}, 用户ID: {}", mobile, appUser.userId());
            recordLogService.recordLogininfor(mobile, Constants.REGISTER, "APP用户自动注册成功");

            return appUser;

        } catch (Exception e) {
            logger.error("APP用户服务：自动创建用户异常 - 手机号: {}", mobile, e);
            throw new ServiceException("自动创建账号失败，请稍后重试: " + e.getMessage());
        }
    }

    /**
     * 将APP用户转换为LoginUser（保持向后兼容）
     */
    private LoginUser convertAppUserToLoginUser(AppUserResponse appUser) {
        try {
            // 创建SysUser对象
            SysUser sysUser = new SysUser();
            sysUser.setUserId(appUser.userId());
            sysUser.setUserName(appUser.username() != null ? appUser.username() : appUser.mobile());
            sysUser.setNickName(appUser.nickname());
            sysUser.setPhonenumber(appUser.mobile());
            sysUser.setEmail(appUser.email());
            sysUser.setAvatar(appUser.avatar());
            sysUser.setSex(appUser.gender() != null ? appUser.gender().toString() : "0");
            sysUser.setStatus(appUser.status().toString());
            sysUser.setDelFlag("0"); // APP用户都是正常状态
            sysUser.setLoginDate(appUser.lastLoginTime() != null ?
                    java.util.Date.from(appUser.lastLoginTime().atZone(java.time.ZoneId.systemDefault()).toInstant()) : null);
            sysUser.setCreateTime(appUser.createTime() != null ?
                    java.util.Date.from(appUser.createTime().atZone(java.time.ZoneId.systemDefault()).toInstant()) : null);

            // 创建LoginUser对象
            LoginUser loginUser = new LoginUser();
            loginUser.setSysUser(sysUser);

            // 设置默认权限（APP用户基础权限）
            loginUser.setPermissions(java.util.Set.of("app:user:basic"));

            return loginUser;

        } catch (Exception e) {
            logger.error("转换APP用户到LoginUser失败 - 用户ID: {}", appUser.userId(), e);
            throw new ServiceException("用户信息转换失败");
        }
    }

    /**
     * 🚀 通过手机号自动创建用户账号（兼容旧方法）
     *
     * @param mobile 手机号
     * @return 创建的用户登录信息
     * @deprecated 使用 autoCreateAppUser 替代
     */
    @Deprecated
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

            // 🔧 添加必要的默认字段
            sysUser.setDeptId(103L); // 设置默认部门ID（请根据实际情况调整）
            sysUser.setSex("2"); // 未知性别
            sysUser.setCreateBy("system"); // 系统创建
            sysUser.setCreateTime(DateUtils.getNowDate());

            // 调用远程服务创建用户
            R<?> registerResult = remoteUserService.registerUserInfo(sysUser, SecurityConstants.INNER);

            if (R.FAIL == registerResult.getCode()) {
                String errorMsg = registerResult.getMsg();
                logger.error("📱 自动创建用户失败 - 手机号: {}, 用户名: {}, 错误详情: {}",
                        mobile, username, errorMsg);
                logger.error("📱 创建失败的用户对象: {}", sysUser.toString());

                // 🔧 提供更友好的错误提示
                String userFriendlyMsg = getUserFriendlyErrorMessage(errorMsg);
                throw new ServiceException(userFriendlyMsg);
            }

            logger.info("✅ 自动创建用户成功 - 手机号: {}, 用户名: {}", mobile, username);
            recordLogService.recordLogininfor(mobile, Constants.REGISTER, "自动注册成功");

            // 重新获取创建的用户信息（使用用户名而不是手机号）
            R<LoginUser> userResult = remoteUserService.getUserInfo(username, SecurityConstants.INNER);
            if (R.FAIL == userResult.getCode()) {
                logger.error("📱 获取新创建用户信息失败 - 用户名: {}, 手机号: {}, 错误: {}",
                        username, mobile, userResult.getMsg());
                throw new ServiceException("获取新创建用户信息失败: " + userResult.getMsg());
            }

            return userResult.getData();

        } catch (Exception e) {
            logger.error("📱 自动创建用户异常 - 手机号: {}", mobile, e);
            throw new ServiceException("自动创建账号失败，请稍后重试");
        }
    }

    /**
     * 🔧 将系统错误信息转换为用户友好的提示
     */
    private String getUserFriendlyErrorMessage(String systemError) {
        if (StringUtils.isBlank(systemError)) {
            return "创建账号失败，请稍后重试";
        }

        // 常见错误的友好提示
        if (systemError.contains("当前系统没有开启注册功能")) {
            return "系统暂时无法创建新账号，请联系管理员";
        }
        if (systemError.contains("注册账号已存在")) {
            return "该手机号已注册，请直接登录";
        }
        if (systemError.contains("用户账号不能为空")) {
            return "账号创建失败，系统错误";
        }
        if (systemError.contains("部门")) {
            return "账号创建失败，部门配置异常";
        }

        // 默认返回系统错误信息
        return "创建账号失败：" + systemError;
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
            recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "验证码已过期");
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
        try {
            String cacheKey = "sms:code:" + mobile;
            redisService.setCacheObject(cacheKey, code, (long) expireTime, TimeUnit.SECONDS);
            logger.info("🔄 缓存验证码到Redis - 手机号: {}, 过期时间: {}秒", mobile, expireTime);
        } catch (Exception e) {
            logger.error("❌ 缓存验证码失败 - 手机号: {}, 错误: {}", mobile, e.getMessage());
            throw new ServiceException("验证码缓存失败，请稍后重试");
        }
    }

    /**
     * 获取缓存的验证码
     *
     * @param mobile 手机号
     * @return 验证码
     */
    private String getCachedVerificationCode(String mobile) {
        try {
            String cacheKey = "sms:code:" + mobile;
            String cachedCode = redisService.getCacheObject(cacheKey);
            logger.info("🔍 从Redis获取缓存验证码 - 手机号: {}, 存在: {}", mobile, cachedCode != null);
            return cachedCode;
        } catch (Exception e) {
            logger.error("❌ 获取缓存验证码失败 - 手机号: {}, 错误: {}", mobile, e.getMessage());
            return null;
        }
    }

    /**
     * 删除缓存的验证码
     *
     * @param mobile 手机号
     */
    private void deleteCachedVerificationCode(String mobile) {
        try {
            String cacheKey = "sms:code:" + mobile;
            boolean deleted = redisService.deleteObject(cacheKey);
            logger.info("🗑️ 从Redis删除缓存验证码 - 手机号: {}, 删除成功: {}", mobile, deleted);
        } catch (Exception e) {
            logger.error("❌ 删除缓存验证码失败 - 手机号: {}, 错误: {}", mobile, e.getMessage());
            // 删除失败不影响登录流程，仅记录日志
        }
    }

    /**
     * 检查短信发送频率限制
     *
     * @param mobile 手机号
     */
    private void checkSmsFrequencyLimit(String mobile) {
        try {
            String frequencyKey = "sms:frequency:" + mobile;
            String lastSendTime = redisService.getCacheObject(frequencyKey);

            if (StringUtils.isNotEmpty(lastSendTime)) {
                recordLogService.recordLogininfor(mobile, Constants.LOGIN_FAIL, "发送过于频繁");
                throw new ServiceException("短信发送过于频繁，请5秒后再试");
            }

            logger.info("✅ 短信频率限制检查通过 - 手机号: {}", mobile);
        } catch (ServiceException e) {
            throw e; // 重新抛出业务异常
        } catch (Exception e) {
            logger.error("❌ 检查短信频率限制失败 - 手机号: {}, 错误: {}", mobile, e.getMessage());
            // 检查失败不阻止发送，记录日志即可
        }
    }

    /**
     * 缓存短信发送频率限制
     *
     * @param mobile     手机号
     * @param expireTime 限制时间（秒）
     */
    private void cacheSmsFrequencyLimit(String mobile, int expireTime) {
        try {
            String frequencyKey = "sms:frequency:" + mobile;
            String currentTime = String.valueOf(System.currentTimeMillis());
            redisService.setCacheObject(frequencyKey, currentTime, (long) expireTime, TimeUnit.SECONDS);
            logger.info("⏰ 设置短信发送频率限制 - 手机号: {}, 限制时间: {}秒 (测试模式)", mobile, expireTime);
        } catch (Exception e) {
            logger.error("❌ 设置短信频率限制失败 - 手机号: {}, 错误: {}", mobile, e.getMessage());
            // 设置失败不影响发送流程，仅记录日志
        }
    }
}
