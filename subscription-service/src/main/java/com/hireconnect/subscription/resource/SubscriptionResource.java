package com.hireconnect.subscription.resource;

import com.hireconnect.subscription.dto.InvoiceDTO;
import com.hireconnect.subscription.dto.SubscriptionRequest;
import com.hireconnect.subscription.dto.SubscriptionResponse;
import com.hireconnect.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionResource {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionResponse> subscribe(@RequestBody SubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.subscribe(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/cancel/{userId}")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(@PathVariable Long userId) {
        SubscriptionResponse response = subscriptionService.cancelSubscription(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/renew")
    public ResponseEntity<SubscriptionResponse> renewSubscription(@RequestBody SubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.renewSubscription(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/invoices/{userId}")
    public ResponseEntity<List<InvoiceDTO>> getInvoices(@PathVariable Long userId) {
        List<InvoiceDTO> invoices = subscriptionService.getInvoices(userId);
        return new ResponseEntity<>(invoices, HttpStatus.OK);
    }

    @GetMapping("/active/{userId}")
    public ResponseEntity<SubscriptionResponse> getActiveSubscription(@PathVariable Long userId) {
        SubscriptionResponse response = subscriptionService.getActiveSubscription(userId);
        if (response != null) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
