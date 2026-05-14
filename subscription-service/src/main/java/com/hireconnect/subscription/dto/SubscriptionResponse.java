package com.hireconnect.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionResponse {
    private Long subscriptionId;
    private Long recruiterId;
    private String plan;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
