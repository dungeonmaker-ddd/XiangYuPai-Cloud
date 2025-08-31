package com.xypai.sms.service.business;

import com.xypai.sms.common.constant.ChannelConstants;
import com.xypai.sms.common.constant.SmsConstants;
import com.xypai.sms.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Business: 短信渠道业务服务
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final AtomicLong roundRobinCounter = new AtomicLong(0);

    /**
     * Business: 选择发送渠道
     */
    public String selectChannel(String preferredChannel, String loadBalanceStrategy, String phoneNumber) {
        log.debug("Business: 选择发送渠道, preferred={}, strategy={}", preferredChannel, loadBalanceStrategy);

        // 获取可用渠道列表
        List<String> availableChannels = getAvailableChannels();
        if (availableChannels.isEmpty()) {
            throw new BusinessException.ChannelUnavailableException("没有可用的发送渠道");
        }

        // 如果指定了优先渠道且可用，直接使用
        if (preferredChannel != null && availableChannels.contains(preferredChannel)) {
            log.debug("Business: 使用优先渠道, channel={}", preferredChannel);
            return preferredChannel;
        }

        // 根据负载均衡策略选择渠道
        String strategy = loadBalanceStrategy != null ? loadBalanceStrategy : SmsConstants.Default.LOAD_BALANCE_STRATEGY;
        String selectedChannel = switch (strategy) {
            case SmsConstants.LoadBalanceStrategy.ROUND_ROBIN -> selectByRoundRobin(availableChannels);
            case SmsConstants.LoadBalanceStrategy.RANDOM -> selectByRandom(availableChannels);
            case SmsConstants.LoadBalanceStrategy.HASH -> selectByHash(availableChannels, phoneNumber);
            default -> selectByRoundRobin(availableChannels);
        };

        log.debug("Business: 选择渠道完成, channel={}, strategy={}", selectedChannel, strategy);
        return selectedChannel;
    }

    /**
     * Business: 发送短信
     */
    public SendResult sendSms(String channel, Set<String> phoneNumbers, Object template, Map<String, String> params) {
        log.info("Business: 发送短信, channel={}, phoneCount={}", channel, phoneNumbers.size());

        try {
            // 根据渠道类型调用对应的发送服务
            return switch (channel) {
                case ChannelConstants.Type.ALIYUN -> sendByAliyun(phoneNumbers, template, params);
                case ChannelConstants.Type.TENCENT -> sendByTencent(phoneNumbers, template, params);
                case ChannelConstants.Type.BAIDU -> sendByBaidu(phoneNumbers, template, params);
                default -> throw new BusinessException.ChannelUnavailableException("不支持的渠道: " + channel);
            };

        } catch (Exception e) {
            log.error("Business: 渠道发送失败, channel={}, error={}", channel, e.getMessage(), e);
            throw new BusinessException.SmsSendException("渠道发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * Business: 轮询选择
     */
    private String selectByRoundRobin(List<String> channels) {
        long count = roundRobinCounter.getAndIncrement();
        int index = (int) (count % channels.size());
        return channels.get(index);
    }

    /**
     * Business: 随机选择
     */
    private String selectByRandom(List<String> channels) {
        int index = (int) (Math.random() * channels.size());
        return channels.get(index);
    }

    /**
     * Business: 哈希选择
     */
    private String selectByHash(List<String> channels, String phoneNumber) {
        int hash = Math.abs(phoneNumber.hashCode());
        int index = hash % channels.size();
        return channels.get(index);
    }

    /**
     * Business: 获取可用渠道列表
     */
    private List<String> getAvailableChannels() {
        // TODO: 从配置或数据库获取可用渠道列表
        return List.of(
                ChannelConstants.Type.ALIYUN,
                ChannelConstants.Type.TENCENT
        );
    }

    /**
     * Business: 阿里云发送
     */
    private SendResult sendByAliyun(Set<String> phoneNumbers, Object template, Map<String, String> params) {
        log.info("Business: 阿里云发送, phoneCount={}", phoneNumbers.size());

        // TODO: 实现阿里云短信发送逻辑
        // 模拟发送成功
        return new SendResult(phoneNumbers.size(), phoneNumbers.size(), 0, "阿里云发送成功");
    }

    /**
     * Business: 腾讯云发送
     */
    private SendResult sendByTencent(Set<String> phoneNumbers, Object template, Map<String, String> params) {
        log.info("Business: 腾讯云发送, phoneCount={}", phoneNumbers.size());

        // TODO: 实现腾讯云短信发送逻辑
        // 模拟发送成功
        return new SendResult(phoneNumbers.size(), phoneNumbers.size(), 0, "腾讯云发送成功");
    }

    /**
     * Business: 百度云发送
     */
    private SendResult sendByBaidu(Set<String> phoneNumbers, Object template, Map<String, String> params) {
        log.info("Business: 百度云发送, phoneCount={}", phoneNumbers.size());

        // TODO: 实现百度云短信发送逻辑
        // 模拟发送成功
        return new SendResult(phoneNumbers.size(), phoneNumbers.size(), 0, "百度云发送成功");
    }

    /**
     * Business: 发送结果
     */
    public record SendResult(
            int totalCount,
            int successCount,
            int failedCount,
            String message
    ) {
    }
}
