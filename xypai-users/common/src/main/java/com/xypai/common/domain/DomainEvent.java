package com.xypai.common.domain;

import java.time.Instant;

/**
 * 🔔 领域事件基础接口 - MVP版本
 * <p>
 * 设计原则：
 * - 简单够用
 * - 易于扩展
 * - 避免过度设计
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
public interface DomainEvent {

    /**
     * 事件唯一标识
     */
    String getEventId();

    /**
     * 事件发生时间
     */
    Instant getOccurredOn();

    /**
     * 事件类型
     */
    String getEventType();

    /**
     * 聚合根ID
     */
    String getAggregateId();

    /**
     * 事件版本 - MVP固定为1.0
     */
    default String getVersion() {
        return "1.0";
    }

    /**
     * 聚合根类型 - 子类重写
     */
    default String getAggregateType() {
        return "Unknown";
    }
}
