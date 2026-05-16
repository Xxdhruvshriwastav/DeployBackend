package com.hireconnect.job.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Existing: fire-and-forget notification queue ──────────────────────────
    public static final String QUEUE       = "notification_queue";
    public static final String EXCHANGE    = "notification_exchange";
    public static final String ROUTING_KEY = "notification_routingKey";

    // ── RPC queues job-service listens on ─────────────────────────────────────
    public static final String RPC_GET_RECRUITER_EMAIL   = "rpc.job.getRecruiterEmail";
    public static final String RPC_GET_JOBS_BY_RECRUITER = "rpc.job.getJobsByRecruiter";
    public static final String RPC_COUNT_JOBS            = "rpc.job.countJobs";

    @Bean
    public Queue notificationQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue notificationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(notificationQueue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public Queue rpcGetRecruiterEmailQueue() {
        return new Queue(RPC_GET_RECRUITER_EMAIL, true);
    }

    @Bean
    public Queue rpcGetJobsByRecruiterQueue() {
        return new Queue(RPC_GET_JOBS_BY_RECRUITER, true);
    }

    @Bean
    public Queue rpcCountJobsQueue() {
        return new Queue(RPC_COUNT_JOBS, true);
    }

    // Spring Boot auto-config picks this up → injects into its RabbitTemplate
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}
