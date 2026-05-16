package com.hireconnect.subscription.service;

import com.hireconnect.subscription.dto.InvoiceDTO;
import com.hireconnect.subscription.dto.SubscriptionRequest;
import com.hireconnect.subscription.dto.SubscriptionResponse;
import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.messaging.SubscriptionMessagingClient;
import com.hireconnect.subscription.repository.InvoiceRepository;
import com.hireconnect.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionMessagingClient messagingClient;

    @Override
    @Transactional
    public SubscriptionResponse subscribe(SubscriptionRequest request) {
        log.info("Processing subscription for user: {}", request.getUserId());

        // Cancel any existing active subscription
        Optional<Subscription> existingActive =
                subscriptionRepository.findByRecruiterIdAndStatus(request.getUserId(), "ACTIVE");
        if (existingActive.isPresent()) {
            Subscription activeSub = existingActive.get();
            activeSub.setStatus("CANCELLED");
            subscriptionRepository.save(activeSub);
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate   = "ENTERPRISE".equalsIgnoreCase(request.getPlan())
                ? startDate.plusDays(365)
                : startDate.plusDays(30);

        Subscription subscription = Subscription.builder()
                .recruiterId(request.getUserId())
                .plan(request.getPlan().toUpperCase())
                .startDate(startDate)
                .endDate(endDate)
                .status("ACTIVE")
                .amountPaid(request.getAmount())
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        // Generate Invoice
        generateInvoice(saved.getSubscriptionId(), request.getUserId(),
                request.getAmount(), request.getPaymentMode(), request.getTransactionId());

        // ── Notify via RabbitMQ ───────────────────────────────────────────────
        try {
            // 1. Get user email from auth-service via RabbitMQ RPC
            Map<String, Object> user = messagingClient.getUserById(request.getUserId().intValue());
            String email = (String) user.get("email");

            if (email != null && !email.isBlank()) {
                String invoiceMessage = String.format(
                        "Dear Customer,\n\nThank you for subscribing to %s Plan.\n\n"
                        + "Invoice Details:\n"
                        + "Subscription ID: %d\n"
                        + "Amount Paid: Rs. %.2f\n"
                        + "Payment Mode: %s\n"
                        + "Transaction ID: %s\n"
                        + "Valid Until: %s\n\n"
                        + "Regards,\nHireConnect Team",
                        request.getPlan().toUpperCase(),
                        saved.getSubscriptionId(),
                        request.getAmount(),
                        request.getPaymentMode(),
                        request.getTransactionId(),
                        saved.getEndDate()
                );

                // 2. Send EMAIL notification (fire-and-forget via RabbitMQ)
                messagingClient.sendNotification(email, "EMAIL", invoiceMessage);

                // 3. Send in-app INFO notification
                messagingClient.sendNotification(
                        String.valueOf(request.getUserId()),
                        "INFO",
                        "Your subscription to " + request.getPlan()
                        + " plan was successful. Invoice sent to email."
                );

                log.info("Invoice notifications queued for email={}", email);
            }
        } catch (Exception e) {
            log.error("Failed to send notifications via RabbitMQ", e);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelSubscription(Long userId) {
        Subscription activeSub = subscriptionRepository.findByRecruiterIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("No active subscription found"));
        activeSub.setStatus("CANCELLED");
        subscriptionRepository.save(activeSub);
        return mapToResponse(activeSub);
    }

    @Override
    @Transactional
    public SubscriptionResponse renewSubscription(SubscriptionRequest request) {
        return subscribe(request);
    }

    @Override
    public List<InvoiceDTO> getInvoices(Long userId) {
        return invoiceRepository.findByRecruiterId(userId).stream()
                .map(this::mapToInvoiceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionResponse getActiveSubscription(Long userId) {
        return subscriptionRepository.findByRecruiterIdAndStatus(userId, "ACTIVE")
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    public InvoiceDTO generateInvoice(Long subscriptionId, Long userId, Double amount,
                                      String paymentMode, String transactionId) {
        Invoice invoice = Invoice.builder()
                .subscriptionId(subscriptionId)
                .recruiterId(userId)
                .amount(amount)
                .paymentDate(LocalDateTime.now())
                .paymentMode(paymentMode)
                .transactionId(transactionId)
                .build();
        return mapToInvoiceDTO(invoiceRepository.save(invoice));
    }

    private SubscriptionResponse mapToResponse(Subscription sub) {
        return SubscriptionResponse.builder()
                .subscriptionId(sub.getSubscriptionId())
                .recruiterId(sub.getRecruiterId())
                .plan(sub.getPlan())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .status(sub.getStatus())
                .build();
    }

    private InvoiceDTO mapToInvoiceDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .invoiceId(invoice.getInvoiceId())
                .subscriptionId(invoice.getSubscriptionId())
                .recruiterId(invoice.getRecruiterId())
                .amount(invoice.getAmount())
                .paymentDate(invoice.getPaymentDate())
                .paymentMode(invoice.getPaymentMode())
                .transactionId(invoice.getTransactionId())
                .build();
    }
}
