package com.hireconnect.subscription.service;

import com.hireconnect.subscription.dto.InvoiceDTO;
import com.hireconnect.subscription.dto.SubscriptionRequest;
import com.hireconnect.subscription.dto.SubscriptionResponse;

import java.util.List;

public interface SubscriptionService {
    SubscriptionResponse subscribe(SubscriptionRequest request);
    SubscriptionResponse cancelSubscription(Long userId);
    SubscriptionResponse renewSubscription(SubscriptionRequest request);
    List<InvoiceDTO> getInvoices(Long userId);
    SubscriptionResponse getActiveSubscription(Long userId);
    InvoiceDTO generateInvoice(Long subscriptionId, Long userId, Double amount, String paymentMode, String transactionId);
}
