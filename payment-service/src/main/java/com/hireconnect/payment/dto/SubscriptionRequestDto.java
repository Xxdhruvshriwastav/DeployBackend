package com.hireconnect.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionRequestDto {
    private Long userId;
    private String plan;
    private Double amount;
    private String paymentMode;
    private String transactionId;
}
