package com.hireconnect.analytics.resource;

import com.hireconnect.analytics.pojo.AnalyticsSummary;
import com.hireconnect.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsResource {

    private final AnalyticsService analyticsService;

    @GetMapping("/recruiter/{id}")
    public ResponseEntity<AnalyticsSummary> getRecruiterStats(@PathVariable("id") Long recruiterId) {
        return ResponseEntity.ok(analyticsService.getPipelineStats(recruiterId));
    }

    @GetMapping("/admin")
    public ResponseEntity<AnalyticsSummary> getPlatformStats() {
        return ResponseEntity.ok(analyticsService.getPlatformStats());
    }

    @GetMapping("/job/{jobId}/view-count")
    public ResponseEntity<Integer> getJobViewCount(@PathVariable("jobId") Long jobId) {
        return ResponseEntity.ok(analyticsService.getJobViewCount(jobId));
    }
}
