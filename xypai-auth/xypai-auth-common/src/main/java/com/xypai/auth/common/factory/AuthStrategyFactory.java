package com.xypai.auth.common.factory;

import com.xypai.auth.common.strategy.AuthenticationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🏭 认证策略工厂
 * <p>
 * 负责管理和创建不同客户端类型的认证策略实例
 * 使用策略模式 + 工厂模式的组合设计
 *
 * @author xypai
 * @version 4.1.0
 * @since 2024-01-15
 */
@Component
public class AuthStrategyFactory {

    /**
     * 客户端类型常量
     */
    public static final String CLIENT_TYPE_WEB = "web";
    public static final String CLIENT_TYPE_APP = "app";
    public static final String CLIENT_TYPE_MINI = "mini";
    private static final Logger logger = LoggerFactory.getLogger(AuthStrategyFactory.class);
    /**
     * 策略实例缓存
     * 使用ConcurrentHashMap确保线程安全
     */
    private final Map<String, AuthenticationStrategy> strategyMap = new ConcurrentHashMap<>();

    /**
     * 构造函数 - 通过依赖注入获取所有策略实现
     *
     * @param strategies Spring自动注入的所有AuthenticationStrategy实现
     */
    public AuthStrategyFactory(Map<String, AuthenticationStrategy> strategies) {
        initializeStrategies(strategies);
        logAvailableStrategies();
    }

    /**
     * 根据客户端类型获取对应的认证策略
     *
     * @param clientType 客户端类型 (web/app/mini)
     * @return 对应的认证策略实例
     * @throws IllegalArgumentException 当clientType为null或不支持时抛出
     */
    public AuthenticationStrategy getStrategy(String clientType) {
        Objects.requireNonNull(clientType, "客户端类型不能为空");

        String normalizedType = clientType.toLowerCase().trim();
        AuthenticationStrategy strategy = strategyMap.get(normalizedType);

        if (strategy == null) {
            logger.error("❌ 不支持的客户端类型: {}, 可用类型: {}",
                    clientType, strategyMap.keySet());
            throw new IllegalArgumentException(
                    String.format("不支持的客户端类型: %s, 可用类型: %s",
                            clientType, strategyMap.keySet()));
        }

        logger.debug("🎯 获取认证策略 - 类型: {}, 策略: {}",
                clientType, strategy.getStrategyName());
        return strategy;
    }

    /**
     * 检查是否支持指定的客户端类型
     *
     * @param clientType 客户端类型
     * @return 是否支持
     */
    public boolean supportsClientType(String clientType) {
        if (clientType == null) {
            return false;
        }
        return strategyMap.containsKey(clientType.toLowerCase().trim());
    }

    /**
     * 获取所有支持的客户端类型
     *
     * @return 支持的客户端类型集合
     */
    public Set<String> getSupportedClientTypes() {
        return Set.copyOf(strategyMap.keySet());
    }

    /**
     * 注册新的认证策略
     *
     * @param clientType 客户端类型
     * @param strategy   认证策略实例
     */
    public void registerStrategy(String clientType, AuthenticationStrategy strategy) {
        Objects.requireNonNull(clientType, "客户端类型不能为空");
        Objects.requireNonNull(strategy, "认证策略不能为空");

        String normalizedType = clientType.toLowerCase().trim();
        AuthenticationStrategy oldStrategy = strategyMap.put(normalizedType, strategy);

        if (oldStrategy != null) {
            logger.warn("⚠️  客户端类型 '{}' 的策略已被替换: {} -> {}",
                    clientType, oldStrategy.getStrategyName(), strategy.getStrategyName());
        } else {
            logger.info("✅ 注册新认证策略 - 类型: {}, 策略: {}",
                    clientType, strategy.getStrategyName());
        }
    }

    /**
     * 初始化策略映射
     */
    private void initializeStrategies(Map<String, AuthenticationStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            logger.warn("⚠️  未发现任何认证策略实现");
            return;
        }

        // 根据bean名称或类名映射到客户端类型
        strategies.forEach((beanName, strategy) -> {
            String clientType = extractClientTypeFromBeanName(beanName);
            if (clientType != null) {
                strategyMap.put(clientType, strategy);
                logger.info("📋 映射认证策略 - Bean: {} -> 类型: {}, 策略: {}",
                        beanName, clientType, strategy.getStrategyName());
            } else {
                logger.warn("⚠️  无法识别Bean名称对应的客户端类型: {}", beanName);
            }
        });

        // 确保基本策略都有实现
        ensureDefaultStrategies();
    }

    /**
     * 从Bean名称提取客户端类型
     */
    private String extractClientTypeFromBeanName(String beanName) {
        if (beanName == null) {
            return null;
        }

        String lowerName = beanName.toLowerCase();

        // 策略命名规则: xxxAuthStrategy -> xxx
        if (lowerName.contains("app")) {
            return CLIENT_TYPE_APP;
        } else if (lowerName.contains("web")) {
            return CLIENT_TYPE_WEB;
        } else if (lowerName.contains("mini")) {
            return CLIENT_TYPE_MINI;
        }

        return null;
    }

    /**
     * 确保默认策略的存在
     */
    private void ensureDefaultStrategies() {
        // 如果没有web策略，使用app策略作为fallback
        if (!strategyMap.containsKey(CLIENT_TYPE_WEB) && strategyMap.containsKey(CLIENT_TYPE_APP)) {
            AuthenticationStrategy appStrategy = strategyMap.get(CLIENT_TYPE_APP);
            strategyMap.put(CLIENT_TYPE_WEB, appStrategy);
            logger.info("📋 使用APP策略作为Web端默认策略");
        }

        // 如果没有mini策略，使用app策略作为fallback
        if (!strategyMap.containsKey(CLIENT_TYPE_MINI) && strategyMap.containsKey(CLIENT_TYPE_APP)) {
            AuthenticationStrategy appStrategy = strategyMap.get(CLIENT_TYPE_APP);
            strategyMap.put(CLIENT_TYPE_MINI, appStrategy);
            logger.info("📋 使用APP策略作为小程序端默认策略");
        }
    }

    /**
     * 记录可用策略信息
     */
    private void logAvailableStrategies() {
        if (strategyMap.isEmpty()) {
            logger.error("❌ 没有可用的认证策略！");
        } else {
            logger.info("🏭 认证策略工厂初始化完成, 可用策略: {}", strategyMap.size());
            strategyMap.forEach((type, strategy) ->
                    logger.info("  📍 {} -> {}", type, strategy.getStrategyName()));
        }
    }
}
