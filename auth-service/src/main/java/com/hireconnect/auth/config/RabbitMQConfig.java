package com.hireconnect.auth.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String AUTH_GET_USER_BY_ID_QUEUE = "rpc.auth.getUserById";

    @Bean
    public Queue authGetUserByIdQueue() {
        return new Queue(AUTH_GET_USER_BY_ID_QUEUE, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
