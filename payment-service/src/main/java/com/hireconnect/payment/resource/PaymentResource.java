package com.hireconnect.payment.resource;

import com.hireconnect.payment.dto.OrderRequest;
import com.hireconnect.payment.dto.OrderResponse;
import com.hireconnect.payment.dto.PaymentVerificationRequest;
import com.hireconnect.payment.service.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentResource {

    private final PaymentServiceImpl paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            System.out.println("DEBUG: Received Order Request for user: " + request.getUserId());
            OrderResponse response = paymentService.createOrder(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("DEBUG ERROR in createOrder: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Payment Error: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        boolean isVerified = paymentService.verifyPayment(request);
        if (isVerified) {
            return ResponseEntity.ok(Collections.singletonMap("status", "SUCCESS"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("status", "FAILED"));
        }
    }
}
