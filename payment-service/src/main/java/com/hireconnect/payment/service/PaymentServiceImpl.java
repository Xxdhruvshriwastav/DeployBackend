package com.hireconnect.payment.service;

import com.hireconnect.payment.client.SubscriptionClient;
import com.hireconnect.payment.dto.OrderRequest;
import com.hireconnect.payment.dto.OrderResponse;
import com.hireconnect.payment.dto.PaymentVerificationRequest;
import com.hireconnect.payment.dto.SubscriptionRequestDto;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl {

    private final RazorpayService razorpayService;
    private final SubscriptionClient subscriptionClient;

    public OrderResponse createOrder(OrderRequest request) {
        try {
            Order order = razorpayService.createOrder(request.getAmount());
            return OrderResponse.builder()
                    .orderId(order.get("id"))
                    .amount((Integer) order.get("amount"))
                    .currency(order.get("currency"))
                    .status(order.get("status"))
                    .build();
        } catch (RazorpayException e) {
            log.error("Failed to create order: {}", e.getMessage());
            throw new RuntimeException("Error creating Razorpay order", e);
        }
    }

    public boolean verifyPayment(PaymentVerificationRequest request) {
        boolean isValid = razorpayService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (isValid) {
            log.info("Payment Signature verified successfully. Calling Subscription Service.");
            // Call subscription service to activate plan and generate invoice
            SubscriptionRequestDto subRequest = SubscriptionRequestDto.builder()
                    .userId(request.getUserId())
                    .plan(request.getPlan())
                    .amount(request.getAmount())
                    .paymentMode(request.getPaymentMode())
                    .transactionId(request.getRazorpayPaymentId())
                    .build();

            try {
                subscriptionClient.createSubscription(subRequest);
                log.info("Subscription created successfully via FeignClient.");
                return true;
            } catch (Exception e) {
                log.error("Failed to call subscription-service: {}", e.getMessage());
                // In a real scenario, handle retry or compensation
                return false;
            }
        } else {
            log.warn("Payment signature verification failed.");
            return false;
        }
    }
}
