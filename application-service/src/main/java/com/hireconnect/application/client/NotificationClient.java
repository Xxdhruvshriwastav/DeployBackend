package com.hireconnect.application.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestTemplate restTemplate;

    private static final String NOTIFICATION_URL = "http://NOTIFICATION-SERVICE/api/v1/notifications/send";

    public void sendEmail(String toEmail, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", toEmail);
            payload.put("type", "EMAIL");
            payload.put("message", message);
            payload.put("isRead", false);

            restTemplate.postForObject(NOTIFICATION_URL, payload, Void.class); // postForObject() already predefine
                                                                               // methods
          //  restTemplate.postForObject("http://NOTIFICATION-SERVICE/api/v1/notifications/send", payload, Void.class);
           // void.class because we do not need anything  in responece, we want only to send mail
            log.info("Email notification sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email notification to {}: {}", toEmail, e.getMessage());
        }
    }
}
