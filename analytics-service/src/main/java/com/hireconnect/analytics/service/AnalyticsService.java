package com.hireconnect.analytics.service;

import com.hireconnect.analytics.pojo.AnalyticsSummary;
import java.util.Map;

public interface AnalyticsService {
    int getJobViewCount(Long jobId);
    int getAppCountByJob(Long jobId);
    double getViewToApplyRatio(Long jobId);
    double getTimeToHire(Long jobId);
    AnalyticsSummary getPipelineStats(Long recruiterId);
    AnalyticsSummary getPlatformStats();
    Map<String, Long> getTopJobCategories();
}
