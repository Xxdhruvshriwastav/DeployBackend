package com.hireconnect.application.listener;

import com.hireconnect.application.config.RabbitMQConfig;
import com.hireconnect.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * RPC Listener: analytics-service calls these to aggregate application stats.
 * Each method returns a value which Spring AMQP auto-sends to the replyTo queue.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationRpcListener {

    private final ApplicationService applicationService;

    /** Total application count across the platform */
    @RabbitListener(queues = RabbitMQConfig.RPC_COUNT_ALL)
    public Long countAll(String ignored) {
        log.info("[RPC] countAll applications requested");
        return applicationService.countAll();
    }

    /** Count by a specific status (e.g. "SHORTLISTED") */
    @RabbitListener(queues = RabbitMQConfig.RPC_COUNT_BY_STATUS)
    public Long countByStatus(String status) {
        log.info("[RPC] countByStatus={} requested", status);
        return applicationService.countByStatus(status);
    }

    /**
     * Count applications for a list of jobIds.
     * Payload is a Map with key "jobIds" containing a List<Integer> (Jackson deserializes numbers as Integer).
     */
    @RabbitListener(queues = RabbitMQConfig.RPC_COUNT_BY_JOBS)
    @SuppressWarnings("unchecked")
    public Long countByJobs(Map<String, Object> payload) {
        List<Integer> rawIds = (List<Integer>) payload.get("jobIds");
        List<Long> jobIds = rawIds.stream().map(Integer::longValue).toList();
        log.info("[RPC] countByJobs for {} jobIds", jobIds.size());
        return applicationService.countByJobs(jobIds);
    }

    /**
     * Count applications for a list of jobIds filtered by status.
     * Payload map contains "jobIds" (List<Integer>) and "status" (String).
     */
    @RabbitListener(queues = RabbitMQConfig.RPC_COUNT_BY_JOBS_STATUS)
    @SuppressWarnings("unchecked")
    public Long countByJobsAndStatus(Map<String, Object> payload) {
        List<Integer> rawIds = (List<Integer>) payload.get("jobIds");
        List<Long> jobIds = rawIds.stream().map(Integer::longValue).toList();
        String status = (String) payload.get("status");
        log.info("[RPC] countByJobsAndStatus status={} for {} jobIds", status, jobIds.size());
        return applicationService.countByJobsAndStatus(jobIds, status);
    }
}
