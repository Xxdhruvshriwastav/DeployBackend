package com.hireconnect.payment.client;

import com.hireconnect.payment.dto.SubscriptionRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "subscription-service")
public interface SubscriptionClient {

    @PostMapping("/api/v1/subscription/subscribe")
    ResponseEntity<Object> createSubscription(@RequestBody SubscriptionRequestDto request);
}
