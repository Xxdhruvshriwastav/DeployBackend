package com.hireconnect.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;

@FeignClient(name = "JOB-SERVICE", path = "/api/v1/jobs")
public interface JobClient {

    @GetMapping("/count")
    Long countJobs();

    @GetMapping("/count/recruiter/{email}")
    Long countJobsByRecruiter(@PathVariable("email") String email);

    @GetMapping("/recruiter/{email}")
    List<Map<String, Object>> getJobsByRecruiter(@PathVariable("email") String email);
}
