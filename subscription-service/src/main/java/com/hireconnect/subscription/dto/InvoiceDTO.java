package com.hireconnect.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {
    private Long invoiceId;
    private Long subscriptionId;
    private Long recruiterId;
    private Double amount;
    private LocalDateTime paymentDate;
    private String paymentMode;
    private String transactionId;
}
