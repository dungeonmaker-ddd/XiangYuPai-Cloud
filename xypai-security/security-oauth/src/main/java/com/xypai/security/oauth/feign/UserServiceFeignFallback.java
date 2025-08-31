package com.xypai.security.oauth.feign;

import com.xypai.common.core.domain.R;
import com.xypai.security.oauth.feign.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 🛡️ 用户服务Feign降级处理
 * <p>
 * 当用户服务不可用时的降级逻辑
 *
 * @author xypai
 * @since 3.0.0
 */
@Slf4j
@Component
public class UserServiceFeignFallback implements UserServiceFeign {

    @Override
    public R<UserResponse> getUserByUsername(String username) {
        log.warn("用户服务不可用，调用降级方法: getUserByUsername({})", username);
        return R.fail("用户服务暂时不可用，请稍后重试");
    }

    @Override
    public R<UserResponse> getUserByMobile(String mobile) {
        log.warn("用户服务不可用，调用降级方法: getUserByMobile({})", mobile);
        return R.fail("用户服务暂时不可用，请稍后重试");
    }

    @Override
    public R<UserResponse> getUserByOpenId(String openId) {
        log.warn("用户服务不可用，调用降级方法: getUserByOpenId({})", openId);
        return R.fail("用户服务暂时不可用，请稍后重试");
    }
}
