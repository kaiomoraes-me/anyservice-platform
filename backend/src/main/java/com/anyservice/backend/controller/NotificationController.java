package com.anyservice.backend.controller;

import com.anyservice.backend.model.Notification;
import com.anyservice.backend.model.User;
import com.anyservice.backend.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(@AuthenticationPrincipal User currentUser) {
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@AuthenticationPrincipal User currentUser) {
        List<Notification> notifications = notificationRepository.findByRecipientIdAndReadStatusFalseOrderByCreatedAtDesc(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            if (notification.getRecipient().getId().equals(currentUser.getId())) {
                notification.setReadStatus(true);
                notificationRepository.save(notification);
            }
        });
        return ResponseEntity.noContent().build();
    }
}
