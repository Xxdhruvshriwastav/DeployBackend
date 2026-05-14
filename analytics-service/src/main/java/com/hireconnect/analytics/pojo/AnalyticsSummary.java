package com.hireconnect.analytics.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSummary {
    private int totalJobs;
    private int totalApplications;
    private int shortlistedCount;
    private int offeredCount;
    private int rejectedCount;
    private double avgTimeToHireDays;
    private double viewToApplyRatio;
    private String message;
}
