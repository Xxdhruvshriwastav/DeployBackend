package com.hireconnect.job.listener;

import com.hireconnect.job.dto.JobDTO;
import com.hireconnect.job.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobRpcListener {

    private final JobService jobService;

    /**
     * RPC: application-service asks "who posted jobId X?" (recruiter email)
     * Input: jobId (Long)
     * Output: Map with "email" key
     */
    @RabbitListener(queues = "rpc.job.getRecruiterEmail")
    public Map<String, Object> getRecruiterEmail(Long jobId) {
        log.info("[RPC] getRecruiterEmail for jobId={}", jobId);
        Map<String, Object> response = new HashMap<>();
        try {
            JobDTO job = jobService.getJobById(jobId);
            response.put("email", job != null ? job.getPostedBy() : null);
        } catch (Exception e) {
            log.error("[RPC] Error in getRecruiterEmail for jobId={}: {}", jobId, e.getMessage());
            response.put("email", null);
            response.put("error", e.getMessage());
        }
        return response;
    }

    /**
     * RPC: analytics-service asks for jobs by recruiter email
     * Input: String email
     * Output: List of job Maps
     */
    @RabbitListener(queues = "rpc.job.getJobsByRecruiter")
    public List<Map<String, Object>> getJobsByRecruiter(String email) {
        log.info("[RPC] getJobsByRecruiter for email={}", email);
        List<JobDTO> jobs = jobService.getJobsByRecruiter(email);
        return jobs.stream().map(job -> {
            Map<String, Object> m = new HashMap<>();
            m.put("jobId", job.getJobId());
            m.put("title", job.getTitle());
            m.put("postedBy", job.getPostedBy());
            m.put("status", job.getStatus());
            m.put("category", job.getCategory());
            m.put("location", job.getLocation());
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * RPC: analytics-service asks total job count
     * Input: ignored (send any string e.g. "count")
     * Output: Long count
     */
    @RabbitListener(queues = "rpc.job.countJobs")
    public Long countJobs(String ignored) {
        log.info("[RPC] countJobs requested");
        return jobService.countJobs();
    }
}
