package com.hireconnect.subscription.service;

import com.hireconnect.subscription.dto.InvoiceDTO;
import com.hireconnect.subscription.dto.SubscriptionRequest;
import com.hireconnect.subscription.dto.SubscriptionResponse;
import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.client.AuthServiceClient;
import com.hireconnect.subscription.client.NotificationServiceClient;
import com.hireconnect.subscription.dto.NotificationDTO;
import com.hireconnect.subscription.dto.UserDTO;
import com.hireconnect.subscription.repository.InvoiceRepository;
import com.hireconnect.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuthServiceClient authServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    @Override
    @Transactional
    public SubscriptionResponse subscribe(SubscriptionRequest request) {
        log.info("Processing subscription for user: {}", request.getUserId());
        
        // Cancel any existing active subscriptions first (or handle upgrades)
        Optional<Subscription> existingActive = subscriptionRepository.findByRecruiterIdAndStatus(request.getUserId(), "ACTIVE");
        if (existingActive.isPresent()) {
            Subscription activeSub = existingActive.get();
            activeSub.setStatus("CANCELLED");
            subscriptionRepository.save(activeSub);
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30); // Default 1 month for this case study
        
        if ("ENTERPRISE".equalsIgnoreCase(request.getPlan())) {
            endDate = startDate.plusDays(365);
        }

        // Create new subscription
        Subscription subscription = Subscription.builder()
                .recruiterId(request.getUserId())
                .plan(request.getPlan().toUpperCase())
                .startDate(startDate)
                .endDate(endDate)
                .status("ACTIVE")
                .amountPaid(request.getAmount())
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // Generate Invoice
        generateInvoice(savedSubscription.getSubscriptionId(), request.getUserId(), request.getAmount(), request.getPaymentMode(), request.getTransactionId());

        try {
            UserDTO user = authServiceClient.getUserById(request.getUserId().intValue());
            if (user != null && user.getEmail() != null) {
                String invoiceMessage = String.format(
                        "Dear Customer,\n\nThank you for subscribing to %s Plan.\n\n" +
                        "Invoice Details:\n" +
                        "Subscription ID: %d\n" +
                        "Amount Paid: Rs. %.2f\n" +
                        "Payment Mode: %s\n" +
                        "Transaction ID: %s\n" +
                        "Valid Until: %s\n\n" +
                        "Regards,\nHireConnect Team",
                        request.getPlan().toUpperCase(),
                        savedSubscription.getSubscriptionId(),
                        request.getAmount(),
                        request.getPaymentMode(),
                        request.getTransactionId(),
                        savedSubscription.getEndDate()
                );

                NotificationDTO emailNotification = NotificationDTO.builder()
                        .userId(user.getEmail())
                        .type("EMAIL")
                        .message(invoiceMessage)
                        .build();
                notificationServiceClient.sendNotification(emailNotification);

                NotificationDTO inAppNotification = NotificationDTO.builder()
                        .userId(String.valueOf(request.getUserId()))
                        .type("INFO") // due to this, email will not send because type is not Alert and Email
                        .message("Your subscription to " + request.getPlan() + " plan was successful. Invoice sent to email.")
                        .build();
                notificationServiceClient.sendNotification(inAppNotification);
                
                log.info("Invoice email sent to {}", user.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to send invoice email/notification via Feign", e);
        }

        return mapToResponse(savedSubscription);
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
        Optional<Subscription> activeSub = subscriptionRepository.findByRecruiterIdAndStatus(userId, "ACTIVE");
        return activeSub.map(this::mapToResponse).orElse(null);
    }

    @Override
    public InvoiceDTO generateInvoice(Long subscriptionId, Long userId, Double amount, String paymentMode, String transactionId) {
        Invoice invoice = Invoice.builder()
                .subscriptionId(subscriptionId)
                .recruiterId(userId)
                .amount(amount)
                .paymentDate(LocalDateTime.now())
                .paymentMode(paymentMode)
                .transactionId(transactionId)
                .build();
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToInvoiceDTO(savedInvoice);
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
