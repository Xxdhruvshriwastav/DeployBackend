package com.hireconnect.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;


// to collect info, we are using this because we are not using db for this
@FeignClient(name = "APPLICATION-SERVICE", path = "/api/v1/applications")
public interface ApplicationClient {

    @GetMapping("/count")
    Long countAll();

    @GetMapping("/count/status/{status}")
    Long countByStatus(@PathVariable("status") String status);

    @PostMapping("/count/jobs")
    Long countByJobs(@RequestBody List<Long> jobIds);

    @PostMapping("/count/jobs/status/{status}")
    Long countByJobsAndStatus(@RequestBody List<Long> jobIds, @PathVariable("status") String status);
}
