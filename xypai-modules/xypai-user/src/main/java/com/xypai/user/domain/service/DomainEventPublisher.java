package com.xypai.user.domain.service;

import com.xypai.user.domain.shared.DomainEvent;

/**
 * 🔔 领域事件发布器接口
 *
 * @author XyPai
 * @since 2025-01-02
 */
public interface DomainEventPublisher {

    /**
     * 发布单个领域事件
     */
    void publish(DomainEvent event);

    /**
     * 批量发布领域事件
     */
    void publishAll(Iterable<DomainEvent> events);
}
