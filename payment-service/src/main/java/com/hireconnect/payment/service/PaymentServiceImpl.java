package com.hireconnect.payment.service;

import com.hireconnect.payment.messaging.PaymentMessagingClient;
import com.hireconnect.payment.dto.OrderRequest;
import com.hireconnect.payment.dto.OrderResponse;
import com.hireconnect.payment.dto.PaymentVerificationRequest;
import com.hireconnect.payment.dto.SubscriptionRequestDto;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import com.hireconnect.payment.exception.CustomException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl {

    private final RazorpayService razorpayService;
    private final PaymentMessagingClient messagingClient;

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
            throw new CustomException("Error creating Razorpay order: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean verifyPayment(PaymentVerificationRequest request) {
        boolean isValid = razorpayService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValid) {
            log.warn("Payment signature verification failed.");
            return false;
        }

        log.info("Payment signature verified. Calling Subscription Service via RabbitMQ.");

        SubscriptionRequestDto subRequest = SubscriptionRequestDto.builder()
                .userId(request.getUserId())
                .plan(request.getPlan())
                .amount(request.getAmount())
                .paymentMode(request.getPaymentMode())
                .transactionId(request.getRazorpayPaymentId())
                .build();

        boolean subscriptionCreated = messagingClient.createSubscription(subRequest);
        if (subscriptionCreated) {
            log.info("Subscription created successfully via RabbitMQ RPC.");
        } else {
            log.error("Subscription creation failed via RabbitMQ RPC.");
        }
        return subscriptionCreated;
    }
}
