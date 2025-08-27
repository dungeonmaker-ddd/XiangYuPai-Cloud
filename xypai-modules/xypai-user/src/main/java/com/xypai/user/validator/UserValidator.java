package com.xypai.user.validator;

import com.xypai.common.core.utils.StringUtils;
import com.xypai.user.domain.record.UserCreateRequest;
import com.xypai.user.domain.record.UserUpdateRequest;
import com.xypai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 用户验证器
 *
 * @author XyPai
 */
@Component
@RequiredArgsConstructor
public class UserValidator {

    // 正则表达式常量
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{6,20}$");
    private final UserService userService;

    /**
     * 验证用户创建请求
     */
    public List<String> validateCreateRequest(UserCreateRequest request) {
        Objects.requireNonNull(request, "用户创建请求不能为null");

        List<String> errors = new ArrayList<>();

        // 基础格式验证
        validateBasicFormat(request.username(), request.nickname(), request.email(),
                request.phone(), request.password(), request.gender(), errors);

        // 唯一性验证
        validateUniqueness(request.username(), request.email(), request.phone(), null, errors);

        return errors;
    }

    /**
     * 验证用户更新请求
     */
    public List<String> validateUpdateRequest(UserUpdateRequest request) {
        Objects.requireNonNull(request, "用户更新请求不能为null");

        List<String> errors = new ArrayList<>();

        // 验证用户ID
        if (request.userId() == null || request.userId() <= 0) {
            errors.add("用户ID无效");
            return errors; // 用户ID无效时直接返回
        }

        // 验证更新字段格式
        request.nickname().ifPresent(nickname -> {
            if (StringUtils.isEmpty(nickname) || nickname.length() > 30) {
                errors.add("昵称长度必须在1-30个字符之间");
            }
        });

        request.email().ifPresent(email -> {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                errors.add("邮箱格式不正确");
            }
        });

        request.phone().ifPresent(phone -> {
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                errors.add("手机号格式不正确");
            }
        });

        request.gender().ifPresent(gender -> {
            if (gender < 0 || gender > 2) {
                errors.add("性别值无效");
            }
        });

        request.status().ifPresent(status -> {
            if (status < 0 || status > 1) {
                errors.add("状态值无效");
            }
        });

        // 验证唯一性
        request.email().ifPresent(email -> {
            if (userService.existsEmail(email, request.userId())) {
                errors.add("邮箱已存在");
            }
        });

        request.phone().ifPresent(phone -> {
            if (userService.existsPhone(phone, request.userId())) {
                errors.add("手机号已存在");
            }
        });

        return errors;
    }

    /**
     * 验证用户名格式
     */
    public boolean isValidUsername(String username) {
        return StringUtils.isNotEmpty(username) && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 验证邮箱格式
     */
    public boolean isValidEmail(String email) {
        return StringUtils.isNotEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证手机号格式
     */
    public boolean isValidPhone(String phone) {
        return StringUtils.isNotEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证密码格式
     */
    public boolean isValidPassword(String password) {
        return StringUtils.isNotEmpty(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 验证性别值
     */
    public boolean isValidGender(Integer gender) {
        return gender != null && gender >= 0 && gender <= 2;
    }

    /**
     * 验证状态值
     */
    public boolean isValidStatus(Integer status) {
        return status != null && (status == 0 || status == 1);
    }

    // Private helper methods
    private void validateBasicFormat(String username, String nickname, String email,
                                     String phone, String password, Integer gender,
                                     List<String> errors) {
        // 用户名验证
        if (!isValidUsername(username)) {
            errors.add("用户名格式不正确，只能包含字母、数字和下划线，长度3-20位");
        }

        // 昵称验证
        if (StringUtils.isEmpty(nickname) || nickname.length() > 30) {
            errors.add("昵称不能为空且长度不能超过30个字符");
        }

        // 邮箱验证
        if (!isValidEmail(email)) {
            errors.add("邮箱格式不正确");
        }

        // 手机号验证
        if (!isValidPhone(phone)) {
            errors.add("手机号格式不正确");
        }

        // 密码验证
        if (!isValidPassword(password)) {
            errors.add("密码长度必须在6-20个字符之间");
        }

        // 性别验证
        if (!isValidGender(gender)) {
            errors.add("性别值无效");
        }
    }

    private void validateUniqueness(String username, String email, String phone,
                                    Long excludeUserId, List<String> errors) {
        // 用户名唯一性
        if (userService.existsUsername(username, excludeUserId)) {
            errors.add("用户名已存在");
        }

        // 邮箱唯一性
        if (userService.existsEmail(email, excludeUserId)) {
            errors.add("邮箱已存在");
        }

        // 手机号唯一性
        if (userService.existsPhone(phone, excludeUserId)) {
            errors.add("手机号已存在");
        }
    }
}
