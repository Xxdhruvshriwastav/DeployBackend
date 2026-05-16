package com.hireconnect.subscription.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // fire-and-forget: push notifications
    public static final String NOTIFICATION_EXCHANGE    = "notification_exchange";
    public static final String NOTIFICATION_ROUTING_KEY = "notification_routingKey";

    // RPC: subscription-service CALLS auth-service
    public static final String RPC_AUTH_GET_USER = "rpc.auth.getUserById";

    // RPC: payment-service CALLS subscription-service (this service listens)
    public static final String RPC_CREATE_SUBSCRIPTION = "rpc.payment.createSubscription";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue createSubscriptionQueue() {
        return new Queue(RPC_CREATE_SUBSCRIPTION, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
