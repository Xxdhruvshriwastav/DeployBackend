package com.hireconnect.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() throws RazorpayException {
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
    }

    public Order createOrder(Double amount) throws RazorpayException {
        log.info("Creating Razorpay Order for amount: {}", amount);
        JSONObject orderRequest = new JSONObject();
        // Razorpay requires amount in paisa (smallest currency unit)
        orderRequest.put("amount", amount * 100); 
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

        Order order = razorpayClient.orders.create(orderRequest);
        log.info("Order created successfully: {}", order.get("id").toString());
        return order;
    }

    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        log.info("Verifying Razorpay Signature for Order: {}, Payment: {}", orderId, paymentId);
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (RazorpayException e) {
            log.error("Exception during signature verification: {}", e.getMessage());
            return false;
        }
    }
}
