package com.hireconnect.notification.resource;

import com.hireconnect.notification.dto.NotificationDTO;
import com.hireconnect.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationResource {

    private final NotificationService notifService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationDTO notification) {
        notifService.sendNotification(notification);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getByUser(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(notifService.getByUser(userId));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(notifService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id) {
        notifService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllRead(@PathVariable("userId") String userId) {
        notifService.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable("id") Long id) {
        notifService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
