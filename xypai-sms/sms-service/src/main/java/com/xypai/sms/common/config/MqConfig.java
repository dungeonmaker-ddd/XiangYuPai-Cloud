package com.xypai.sms.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Config: 消息队列配置类
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Configuration
public class MqConfig {

    // 队列名称常量
    public static final String SMS_SEND_QUEUE = "sms.send.queue";
    public static final String SMS_SEND_EXCHANGE = "sms.send.exchange";
    public static final String SMS_SEND_ROUTING_KEY = "sms.send";
    public static final String SMS_DLQ = "sms.send.dlq";
    public static final String SMS_DLX = "sms.send.dlx";

    /**
     * Config: 短信发送队列
     */
    @Bean
    public Queue smsSendQueue() {
        return QueueBuilder.durable(SMS_SEND_QUEUE)
                .withArgument("x-dead-letter-exchange", SMS_DLX)
                .withArgument("x-dead-letter-routing-key", "sms.send.dlq")
                .withArgument("x-message-ttl", 600000) // 10分钟TTL
                .build();
    }

    /**
     * Config: 短信发送交换机
     */
    @Bean
    public DirectExchange smsSendExchange() {
        return new DirectExchange(SMS_SEND_EXCHANGE, true, false);
    }

    /**
     * Config: 绑定队列和交换机
     */
    @Bean
    public Binding smsSendBinding() {
        return BindingBuilder
                .bind(smsSendQueue())
                .to(smsSendExchange())
                .with(SMS_SEND_ROUTING_KEY);
    }

    /**
     * Config: 死信队列
     */
    @Bean
    public Queue smsDeadLetterQueue() {
        return QueueBuilder.durable(SMS_DLQ).build();
    }

    /**
     * Config: 死信交换机
     */
    @Bean
    public DirectExchange smsDeadLetterExchange() {
        return new DirectExchange(SMS_DLX, true, false);
    }

    /**
     * Config: 死信绑定
     */
    @Bean
    public Binding smsDeadLetterBinding() {
        return BindingBuilder
                .bind(smsDeadLetterQueue())
                .to(smsDeadLetterExchange())
                .with("sms.send.dlq");
    }
}
