package com.hireconnect.analytics.service;

import com.hireconnect.analytics.pojo.AnalyticsSummary;
import com.hireconnect.analytics.client.JobClient;
import com.hireconnect.analytics.client.ApplicationClient;
import com.hireconnect.analytics.client.AuthClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private JobClient jobClient;

    @Autowired
    private ApplicationClient applicationClient;

    @Autowired
    private AuthClient authClient;

    private final Random random = new Random();

    @Override
    public int getJobViewCount(Long jobId) {
        // Mocking view counts as there's no view tracking service yet

        return random.nextInt(100, 1000);
    }

    @Override
    public int getAppCountByJob(Long jobId) {
        return applicationClient.countByJobs(List.of(jobId)).intValue();
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
            // 1. Get recruiter email from Auth-Service
            Map<String, Object> user = authClient.getUserById(recruiterId.intValue());
            String email = (String) user.get("email");

            // 2. Get job IDs for this recruiter
            List<Map<String, Object>> recruiterJobs = jobClient.getJobsByRecruiter(email);
            List<Long> jobIds = recruiterJobs.stream()
                    .map(j -> Long.valueOf(j.get("jobId").toString()))
                    .toList();

            if (jobIds.isEmpty()) {
                return AnalyticsSummary.builder().build();
            }

            // 3. Aggregate applications
            long totalApps = applicationClient.countByJobs(jobIds);
            long shortlisted = applicationClient.countByJobsAndStatus(jobIds, "SHORTLISTED");
            long offered = applicationClient.countByJobsAndStatus(jobIds, "OFFERED");
            long rejected = applicationClient.countByJobsAndStatus(jobIds, "REJECTED");

            return AnalyticsSummary.builder()
                    .totalJobs(jobIds.size())
                    .totalApplications((int) totalApps)
                    .shortlistedCount((int) shortlisted)
                    .offeredCount((int) offered)
                    .rejectedCount((int) rejected)
                    .avgTimeToHireDays(22.5) // Simulation
                    .viewToApplyRatio(totalApps > 0 ? (double)offered/totalApps : 0.0)
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
            long totalJobs = jobClient.countJobs();
            long totalApps = applicationClient.countAll();
            long shortlisted = applicationClient.countByStatus("SHORTLISTED");
            long offered = applicationClient.countByStatus("OFFERED");
            long rejected = applicationClient.countByStatus("REJECTED");

            return AnalyticsSummary.builder()
                    .totalJobs((int) totalJobs)
                    .totalApplications((int) totalApps)
                    .shortlistedCount((int) shortlisted)
                    .offeredCount((int) offered)
                    .rejectedCount((int) rejected)
                    .avgTimeToHireDays(28.5)
                    .viewToApplyRatio(totalApps > 0 ? (double)offered/totalApps : 0.0)
                    .build();
        } catch (Exception e) {
            return AnalyticsSummary.builder()
                    .message("Error fetching live stats: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Map<String, Long> getTopJobCategories() {
        // Mocking for now, could be implemented with a group-by query in job-service
        Map<String, Long> categories = new java.util.HashMap<>();
        categories.put("Engineering", 150L);
        categories.put("Marketing", 80L);
        categories.put("Sales", 120L);
        categories.put("HR", 40L);
        return categories;
    }
}
