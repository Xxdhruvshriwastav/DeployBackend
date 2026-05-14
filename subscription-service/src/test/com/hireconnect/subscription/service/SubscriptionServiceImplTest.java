package com.hireconnect.subscription.service;

import com.hireconnect.subscription.client.AuthServiceClient;
import com.hireconnect.subscription.client.NotificationServiceClient;
import com.hireconnect.subscription.dto.*;
import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.repository.InvoiceRepository;
import com.hireconnect.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private SubscriptionRequest request;

    @BeforeEach
    void setup() {
        request = SubscriptionRequest.builder()
                .userId(1L)
                .plan("PREMIUM")
                .amount(500.0)
                .paymentMode("UPI")
                .transactionId("txn_123")
                .build();
    }

    // =========================
    // SUBSCRIBE TEST
    // =========================
    @Test
    void testSubscribe_success() {

        // No active subscription
        when(subscriptionRepository.findByRecruiterIdAndStatus(1L, "ACTIVE"))
                .thenReturn(Optional.empty());

        Subscription savedSub = Subscription.builder()
                .subscriptionId(10L)
                .recruiterId(1L)
                .plan("PREMIUM")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status("ACTIVE")
                .amountPaid(500.0)
                .build();

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(savedSub);

        when(invoiceRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO user = new UserDTO();
        user.setEmail("test@gmail.com");

        when(authServiceClient.getUserById(1))
                .thenReturn(user);

        doNothing().when(notificationServiceClient)
                .sendNotification(any(NotificationDTO.class));

        SubscriptionResponse response = subscriptionService.subscribe(request);

        assertNotNull(response);
        assertEquals("PREMIUM", response.getPlan());
        assertEquals("ACTIVE", response.getStatus());

        verify(subscriptionRepository, times(1)).save(any());
        verify(invoiceRepository, times(1)).save(any());

        // 2 notifications: email + in-app
        verify(notificationServiceClient, times(2))
                .sendNotification(any(NotificationDTO.class));
    }

    // =========================
    // EXISTING ACTIVE SUB CANCEL
    // =========================
    @Test
    void testSubscribe_existingActiveCancelled() {

        Subscription active = Subscription.builder()
                .subscriptionId(5L)
                .recruiterId(1L)
                .status("ACTIVE")
                .build();

        when(subscriptionRepository.findByRecruiterIdAndStatus(1L, "ACTIVE"))
                .thenReturn(Optional.of(active));

        when(subscriptionRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(invoiceRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Return null user → notification block is skipped (guarded by null check in impl)
        when(authServiceClient.getUserById(1))
                .thenReturn(null);

        SubscriptionResponse response = subscriptionService.subscribe(request);

        assertNotNull(response);

        verify(subscriptionRepository, atLeast(2)).save(any());
        // No notifications when user is null
        verify(notificationServiceClient, never()).sendNotification(any());
    }

    // =========================
    // CANCEL SUBSCRIPTION
    // =========================
    @Test
    void testCancelSubscription_success() {

        Subscription active = Subscription.builder()
                .subscriptionId(10L)
                .recruiterId(1L)
                .status("ACTIVE")
                .build();

        when(subscriptionRepository.findByRecruiterIdAndStatus(1L, "ACTIVE"))
                .thenReturn(Optional.of(active));

        when(subscriptionRepository.save(any()))
                .thenReturn(active);

        SubscriptionResponse response =
                subscriptionService.cancelSubscription(1L);

        assertEquals("CANCELLED", active.getStatus());
        assertNotNull(response);
    }

    @Test
    void testCancelSubscription_notFound() {

        when(subscriptionRepository.findByRecruiterIdAndStatus(1L, "ACTIVE"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                subscriptionService.cancelSubscription(1L));
    }

    // =========================
    // GET ACTIVE SUB
    // =========================
    @Test
    void testGetActiveSubscription() {

        Subscription active = Subscription.builder()
                .subscriptionId(10L)
                .recruiterId(1L)
                .status("ACTIVE")
                .build();

        when(subscriptionRepository.findByRecruiterIdAndStatus(1L, "ACTIVE"))
                .thenReturn(Optional.of(active));

        SubscriptionResponse response =
                subscriptionService.getActiveSubscription(1L);

        assertNotNull(response);
        assertEquals("ACTIVE", response.getStatus());
    }

    // =========================
    // VERIFY INVOICE GENERATION
    // =========================
    @Test
    void testGenerateInvoice() {

        when(invoiceRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceDTO dto = subscriptionService.generateInvoice(
                10L, 1L, 500.0, "UPI", "txn_123"
        );

        assertNotNull(dto);
        assertEquals(500.0, dto.getAmount());
    }
}