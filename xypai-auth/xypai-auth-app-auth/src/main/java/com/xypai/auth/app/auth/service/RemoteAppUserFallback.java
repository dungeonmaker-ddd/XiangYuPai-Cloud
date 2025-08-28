package com.xypai.auth.app.auth.service;

import com.xypai.user.domain.record.AppUserRegisterRequest;
import com.xypai.user.domain.record.AppUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 远程APP用户服务降级处理
 *
 * @author XyPai
 */
@Component
public class RemoteAppUserFallback implements RemoteAppUserService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteAppUserFallback.class);

    @Override
    public AppUserResponse register(AppUserRegisterRequest request) {
        logger.error("远程APP用户服务注册失败，服务降级");
        throw new RuntimeException("用户服务暂时不可用，请稍后重试");
    }

    @Override
    public Optional<AppUserResponse> getByMobile(String mobile) {
        logger.warn("远程APP用户服务查询失败，服务降级 - 手机号: {}", mobile);
        return Optional.empty();
    }

    @Override
    public Optional<AppUserResponse> getById(Long id) {
        logger.error("远程APP用户服务查询失败，服务降级 - 用户ID: {}", id);
        return Optional.empty();
    }

    @Override
    public Boolean updateLastLoginTime(Long id) {
        logger.error("远程APP用户服务更新登录时间失败，服务降级 - 用户ID: {}", id);
        return false;
    }

    @Override
    public Boolean checkMobile(String mobile) {
        logger.error("远程APP用户服务检查手机号失败，服务降级 - 手机号: {}", mobile);
        return false;
    }
}
