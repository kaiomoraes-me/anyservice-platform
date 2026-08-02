package com.anyservice.backend.service.event;

public record NotificationEvent(
        Long recipientId,
        Long actorId,
        String entityType,
        Long entityId,
        String actionType
) {
}
