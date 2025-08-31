package com.xypai.security.oauth.feign;

import com.xypai.common.core.domain.R;
import com.xypai.security.oauth.feign.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 🌐 用户服务Feign客户端
 * <p>
 * 用于认证服务调用用户服务获取用户信息
 *
 * @author xypai
 * @since 3.0.0
 */
@FeignClient(
        name = "user-service",
        path = "/users",
        fallback = UserServiceFeignFallback.class
)
public interface UserServiceFeign {

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    R<UserResponse> getUserByUsername(@PathVariable("username") String username);

    /**
     * 根据手机号获取用户信息
     *
     * @param mobile 手机号
     * @return 用户信息
     */
    @GetMapping("/mobile/{mobile}")
    R<UserResponse> getUserByMobile(@PathVariable("mobile") String mobile);

    /**
     * 根据OpenId获取用户信息
     *
     * @param openId 微信OpenId
     * @return 用户信息
     */
    @GetMapping("/openid/{openId}")
    R<UserResponse> getUserByOpenId(@PathVariable("openId") String openId);
}
