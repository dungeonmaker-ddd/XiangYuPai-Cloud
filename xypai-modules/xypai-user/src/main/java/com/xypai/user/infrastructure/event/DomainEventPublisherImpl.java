package com.xypai.user.infrastructure.event;

import com.xypai.user.domain.shared.DomainEvent;
import com.xypai.user.domain.shared.service.DomainEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 🔔 领域事件发布器实现 - 基于Spring事件机制
 *
 * @author XyPai
 * @since 2025-01-02
 */
@Slf4j
@Component
public class DomainEventPublisherImpl implements DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public DomainEventPublisherImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        log.debug("发布领域事件: {} - {}", event.eventType(), event.eventId());
        try {
            eventPublisher.publishEvent(event);
            log.debug("领域事件发布成功: {}", event.eventId());
        } catch (Exception e) {
            log.error("领域事件发布失败: {}", event.eventId(), e);
            // 这里可以考虑添加重试机制或将事件存储到死信队列
            throw new RuntimeException("领域事件发布失败", e);
        }
    }

    @Override
    public void publishAll(Iterable<DomainEvent> events) {
        log.debug("批量发布领域事件开始");
        int count = 0;
        for (DomainEvent event : events) {
            publish(event);
            count++;
        }
        log.debug("批量发布领域事件完成，共发布 {} 个事件", count);
    }
}
