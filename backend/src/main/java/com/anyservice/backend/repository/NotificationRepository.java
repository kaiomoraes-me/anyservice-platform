package com.anyservice.backend.repository;

import com.anyservice.backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    List<Notification> findByRecipientIdAndReadStatusFalseOrderByCreatedAtDesc(Long recipientId);
    boolean existsByRecipientIdAndEntityTypeAndEntityIdAndActionType(Long recipientId, String entityType, Long entityId, String actionType);
}
