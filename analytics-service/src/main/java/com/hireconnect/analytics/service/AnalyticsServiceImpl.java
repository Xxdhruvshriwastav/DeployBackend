package com.hireconnect.analytics.service;

import com.hireconnect.analytics.pojo.AnalyticsSummary;
import com.hireconnect.analytics.messaging.AnalyticsRpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRpcClient rpcClient;
    private final Random random = new Random();

    @Override
    public int getJobViewCount(Long jobId) {
        // Mocking view counts — no view tracking service yet
        return random.nextInt(100, 1000);
    }

    @Override
    public int getAppCountByJob(Long jobId) {
        return rpcClient.countByJobs(List.of(jobId)).intValue();
    }

    @Override
    public double getViewToApplyRatio(Long jobId) {
        return random.nextDouble(0.05, 0.25);
    }

    @Override
    public double getTimeToHire(Long jobId) {
        return random.nextDouble(10, 45);
    }

    @Override
    public AnalyticsSummary getPipelineStats(Long recruiterId) {
        try {
            // 1. Get recruiter email from auth-service via RabbitMQ RPC
            Map<String, Object> user = rpcClient.getUserById(recruiterId.intValue());
            String email = (String) user.get("email");

            if (email == null || email.isBlank()) {
                return AnalyticsSummary.builder()
                        .message("User not found for recruiterId=" + recruiterId)
                        .build();
            }

            // 2. Get jobs for recruiter from job-service via RabbitMQ RPC
            List<Map<String, Object>> recruiterJobs = rpcClient.getJobsByRecruiter(email);
            List<Long> jobIds = recruiterJobs.stream()
                    .map(j -> Long.valueOf(j.get("jobId").toString()))
                    .toList();

            if (jobIds.isEmpty()) {
                return AnalyticsSummary.builder().build();
            }

            // 3. Aggregate application counts from application-service via RabbitMQ RPC
            long totalApps    = rpcClient.countByJobs(jobIds);
            long shortlisted  = rpcClient.countByJobsAndStatus(jobIds, "SHORTLISTED");
            long offered      = rpcClient.countByJobsAndStatus(jobIds, "OFFERED");
            long rejected     = rpcClient.countByJobsAndStatus(jobIds, "REJECTED");

            return AnalyticsSummary.builder()
                    .totalJobs(jobIds.size())
                    .totalApplications((int) totalApps)
                    .shortlistedCount((int) shortlisted)
                    .offeredCount((int) offered)
                    .rejectedCount((int) rejected)
                    .avgTimeToHireDays(22.5)
                    .viewToApplyRatio(totalApps > 0 ? (double) offered / totalApps : 0.0)
                    .build();

        } catch (Exception e) {
            return AnalyticsSummary.builder()
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public AnalyticsSummary getPlatformStats() {
        try {
            long totalJobs  = rpcClient.countJobs();
            long totalApps  = rpcClient.countAllApplications();
            long shortlisted = rpcClient.countByStatus("SHORTLISTED");
            long offered    = rpcClient.countByStatus("OFFERED");
            long rejected   = rpcClient.countByStatus("REJECTED");

            return AnalyticsSummary.builder()
                    .totalJobs((int) totalJobs)
                    .totalApplications((int) totalApps)
                    .shortlistedCount((int) shortlisted)
                    .offeredCount((int) offered)
                    .rejectedCount((int) rejected)
                    .avgTimeToHireDays(28.5)
                    .viewToApplyRatio(totalApps > 0 ? (double) offered / totalApps : 0.0)
                    .build();

        } catch (Exception e) {
            return AnalyticsSummary.builder()
                    .message("Error fetching live stats: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Map<String, Long> getTopJobCategories() {
        Map<String, Long> categories = new HashMap<>();
        categories.put("Engineering", 150L);
        categories.put("Marketing", 80L);
        categories.put("Sales", 120L);
        categories.put("HR", 40L);
        return categories;
    }
}
