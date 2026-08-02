package com.anyservice.notification.service;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationEventConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationEventConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "notification_queue", durable = "true"),
            exchange = @Exchange(value = "anyservice_events", type = "topic"),
            key = "notification.created"
    ))
    public void handleNotificationEvent(Map<String, Object> event) {
        try {
            System.out.println("Received notification event: " + event);

            Long recipientId = ((Number) event.get("recipientId")).longValue();
            
            // Format the destination as /user/{recipientId}/queue/notifications
            // Since we set user destination prefix to /user in WebSocketConfig, 
            // messagingTemplate.convertAndSendToUser uses it.
            // E.g., if recipientId is 2, destination is /user/2/queue/notifications
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(recipientId),
                    "/queue/notifications",
                    event
            );
            
            System.out.println("Notification dispatched via WebSocket to user " + recipientId);
        } catch (Exception e) {
            System.err.println("Failed to process notification event: " + e.getMessage());
        }
    }
}
