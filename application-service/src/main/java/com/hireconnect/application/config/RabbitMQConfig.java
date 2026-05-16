package com.hireconnect.application.config;

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

    // RPC: this service CALLS job-service
    public static final String RPC_GET_RECRUITER_EMAIL = "rpc.job.getRecruiterEmail";

    // RPC: analytics-service CALLS this service
    public static final String RPC_COUNT_ALL            = "rpc.app.countAll";
    public static final String RPC_COUNT_BY_STATUS      = "rpc.app.countByStatus";
    public static final String RPC_COUNT_BY_JOBS        = "rpc.app.countByJobs";
    public static final String RPC_COUNT_BY_JOBS_STATUS = "rpc.app.countByJobsStatus";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue rpcCountAllQueue() {
        return new Queue(RPC_COUNT_ALL, true);
    }

    @Bean
    public Queue rpcCountByStatusQueue() {
        return new Queue(RPC_COUNT_BY_STATUS, true);
    }

    @Bean
    public Queue rpcCountByJobsQueue() {
        return new Queue(RPC_COUNT_BY_JOBS, true);
    }

    @Bean
    public Queue rpcCountByJobsStatusQueue() {
        return new Queue(RPC_COUNT_BY_JOBS_STATUS, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
