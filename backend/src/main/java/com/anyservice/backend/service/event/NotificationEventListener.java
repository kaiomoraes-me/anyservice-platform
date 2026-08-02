package com.anyservice.backend.service.event;

import com.anyservice.backend.model.Notification;
import com.anyservice.backend.model.User;
import com.anyservice.backend.repository.NotificationRepository;
import com.anyservice.backend.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationEventListener(NotificationRepository notificationRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    @EventListener
    @Transactional
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            // Idempotency Check
            boolean exists = notificationRepository.existsByRecipientIdAndEntityTypeAndEntityIdAndActionType(
                    event.recipientId(), event.entityType(), event.entityId(), event.actionType()
            );

            if (exists) {
                log.info("Duplicate notification ignored for recipient {} and entity {}", event.recipientId(), event.entityId());
                return;
            }

            User recipient = userRepository.findById(event.recipientId()).orElse(null);
            User actor = userRepository.findById(event.actorId()).orElse(null);

            if (recipient == null || actor == null) {
                log.warn("Recipient or Actor not found. Notification aborted.");
                return;
            }

            Notification notification = new Notification(recipient, actor, event.entityType(), event.entityId(), event.actionType());
            notification = notificationRepository.save(notification);

            // Publish to WebSocket
            messagingTemplate.convertAndSendToUser(
                    recipient.getEmail(), 
                    "/queue/notifications", 
                    notification
            );
            log.info("Notification sent to {}", recipient.getEmail());

        } catch (Exception e) {
            log.error("Failed to process notification event for entity {}", event.entityId(), e);
            // Exception is swallowed here so it does not affect the main thread if called synchronously by accident.
            // But since it's @Async, it already runs in a separate thread.
        }
    }
}
