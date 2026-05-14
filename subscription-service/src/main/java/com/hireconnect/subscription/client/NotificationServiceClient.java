package com.hireconnect.subscription.client;

import com.hireconnect.subscription.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {

    @PostMapping("/api/v1/notifications/send")
    void sendNotification(@RequestBody NotificationDTO notification);
}
