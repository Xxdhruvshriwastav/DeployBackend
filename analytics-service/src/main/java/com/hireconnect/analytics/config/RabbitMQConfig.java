package com.hireconnect.analytics.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // RPC queues analytics-service CALLS
    public static final String RPC_AUTH_GET_USER     = "rpc.auth.getUserById";
    public static final String RPC_JOB_BY_RECRUITER  = "rpc.job.getJobsByRecruiter";
    public static final String RPC_JOB_COUNT         = "rpc.job.countJobs";
    public static final String RPC_APP_COUNT_ALL     = "rpc.app.countAll";
    public static final String RPC_APP_COUNT_STATUS  = "rpc.app.countByStatus";
    public static final String RPC_APP_COUNT_JOBS    = "rpc.app.countByJobs";
    public static final String RPC_APP_COUNT_JOBS_ST = "rpc.app.countByJobsStatus";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
