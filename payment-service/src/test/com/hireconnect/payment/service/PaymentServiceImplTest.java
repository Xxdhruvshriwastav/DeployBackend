package com.hireconnect.payment.service;

import com.hireconnect.payment.client.SubscriptionClient;
import com.hireconnect.payment.dto.OrderRequest;
import com.hireconnect.payment.dto.OrderResponse;
import com.hireconnect.payment.dto.PaymentVerificationRequest;
import com.hireconnect.payment.dto.SubscriptionRequestDto;
import com.razorpay.Order;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private RazorpayService razorpayService;

    @Mock
    private SubscriptionClient subscriptionClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private OrderRequest orderRequest;
    private PaymentVerificationRequest verificationRequest;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest(500.0, 1L, "PREMIUM");

        verificationRequest = new PaymentVerificationRequest(
                "order_123",
                "payment_123",
                "signature_123",
                1L,
                "PREMIUM",
                500.0,
                "ONLINE"
        );
    }

    // =========================
    // createOrder TEST
    // =========================
    @Test
    void testCreateOrder_success() throws Exception {

        Order mockOrder = mock(Order.class);

        when(razorpayService.createOrder(500.0)).thenReturn(mockOrder);

        when(mockOrder.get("id")).thenReturn("order_123");
        when(mockOrder.get("amount")).thenReturn(50000); // paisa
        when(mockOrder.get("currency")).thenReturn("INR");
        when(mockOrder.get("status")).thenReturn("created");

        OrderResponse response = paymentService.createOrder(orderRequest);

        assertNotNull(response);
        assertEquals("order_123", response.getOrderId());
        assertEquals(50000, response.getAmount());
        assertEquals("INR", response.getCurrency());
        assertEquals("created", response.getStatus());

        verify(razorpayService, times(1)).createOrder(500.0);
    }

    @Test
    void testCreateOrder_exception() throws Exception {
        when(razorpayService.createOrder(500.0))
                .thenThrow(new RuntimeException("Razorpay error"));

        assertThrows(RuntimeException.class, () ->
                paymentService.createOrder(orderRequest));

        verify(razorpayService, times(1)).createOrder(500.0);
    }

    // =========================
    // verifyPayment TEST
    // =========================
    @Test
    void testVerifyPayment_success() {

        when(razorpayService.verifyPaymentSignature(
                "order_123", "payment_123", "signature_123"
        )).thenReturn(true);

        when(subscriptionClient.createSubscription(any(SubscriptionRequestDto.class)))
                .thenReturn(null); // no response needed

        boolean result = paymentService.verifyPayment(verificationRequest);

        assertTrue(result);

        verify(razorpayService, times(1))
                .verifyPaymentSignature(anyString(), anyString(), anyString());

        verify(subscriptionClient, times(1))
                .createSubscription(any(SubscriptionRequestDto.class));
    }

    @Test
    void testVerifyPayment_signatureFailed() {

        when(razorpayService.verifyPaymentSignature(
                "order_123", "payment_123", "signature_123"
        )).thenReturn(false);

        boolean result = paymentService.verifyPayment(verificationRequest);

        assertFalse(result);

        verify(subscriptionClient, never())
                .createSubscription(any());
    }

    @Test
    void testVerifyPayment_subscriptionFails() {

        when(razorpayService.verifyPaymentSignature(
                "order_123", "payment_123", "signature_123"
        )).thenReturn(true);

        when(subscriptionClient.createSubscription(any()))
                .thenThrow(new RuntimeException("Subscription error"));

        boolean result = paymentService.verifyPayment(verificationRequest);

        assertFalse(result);

        verify(subscriptionClient, times(1))
                .createSubscription(any());
    }
}