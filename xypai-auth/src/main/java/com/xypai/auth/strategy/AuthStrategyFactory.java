package com.xypai.auth.strategy;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🏭 认证策略工厂
 * <p>
 * 基于 clientType 的智能策略路由：
 * - 策略缓存优化
 * - 类型安全的客户端识别
 * - 高性能策略查找
 * - 零配置扩展新客户端
 *
 * @author xypai
 * @version 4.0.0 (统一架构版本)
 * @since 2024-01-15
 */
@Component
public class AuthStrategyFactory {

    private final Map<String, AuthenticationStrategy> strategies = new ConcurrentHashMap<>();
    
    public AuthStrategyFactory(AdminAuthStrategy adminStrategy,
                               AppAuthStrategy appStrategy) {
        // 注册策略映射（根据clientType规则：web、app、mini）
        strategies.put("web", adminStrategy);    // web用于管理端（使用admin策略）
        strategies.put("app", appStrategy);      // app用于移动端
        strategies.put("mini", appStrategy);     // mini用于小程序（使用app策略）
    }

    /**
     * 获取认证策略
     *
     * @param clientType 客户端类型
     * @return 对应的认证策略
     * @throws IllegalArgumentException 不支持的客户端类型
     */
    public AuthenticationStrategy getStrategy(String clientType) {
        AuthenticationStrategy strategy = strategies.get(clientType.toLowerCase());

        if (strategy == null) {
            throw new IllegalArgumentException(
                    String.format("不支持的客户端类型: %s, 支持的类型: %s",
                            clientType, strategies.keySet())
            );
        }

        return strategy;
    }

    /**
     * 获取所有支持的客户端类型
     */
    public String[] getSupportedClientTypes() {
        return strategies.keySet().toArray(new String[0]);
    }
}
