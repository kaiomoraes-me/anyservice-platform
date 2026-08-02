package com.anyservice.backend.service.event;

import com.anyservice.backend.model.Notification;
import com.anyservice.backend.model.User;
import com.anyservice.backend.repository.NotificationRepository;
import com.anyservice.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationEventListenerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldProcessNotificationSuccessfully() {
        Long recipientId = 100L;
        Long actorId = 200L;
        Long entityId = 300L;
        
        User recipient = new User();
        recipient.setId(recipientId);
        recipient.setEmail("recipient@test.com");
        
        User actor = new User();
        actor.setId(actorId);

        NotificationEvent event = new NotificationEvent(recipientId, actorId, "CHAT_MESSAGE", entityId, "SENT_MESSAGE");

        when(notificationRepository.existsByRecipientIdAndEntityTypeAndEntityIdAndActionType(
                recipientId, "CHAT_MESSAGE", entityId, "SENT_MESSAGE")).thenReturn(false);
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));
        when(userRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        listener.handleNotificationEvent(event);

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq("recipient@test.com"),
                eq("/queue/notifications"),
                any(Notification.class)
        );
    }

    @Test
    void shouldDropDuplicateNotificationIdempotencyCheck() {
        Long recipientId = 100L;
        Long entityId = 300L;
        NotificationEvent event = new NotificationEvent(recipientId, 200L, "SERVICE_ORDER", entityId, "PURCHASED");

        when(notificationRepository.existsByRecipientIdAndEntityTypeAndEntityIdAndActionType(
                recipientId, "SERVICE_ORDER", entityId, "PURCHASED")).thenReturn(true); // Exists!

        listener.handleNotificationEvent(event);

        verify(userRepository, never()).findById(any());
        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    void shouldNotCrashOnWebSocketFailure_DbShouldStillBeSaved() {
        Long recipientId = 100L;
        User recipient = new User();
        recipient.setId(recipientId);
        recipient.setEmail("test@test.com");

        NotificationEvent event = new NotificationEvent(recipientId, 200L, "TEST", 300L, "TEST");

        when(notificationRepository.existsByRecipientIdAndEntityTypeAndEntityIdAndActionType(any(), any(), any(), any())).thenReturn(false);
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(new User()));
        
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // Simular falha de rede/broker no envio WebSocket
        doThrow(new RuntimeException("Broker is down!")).when(messagingTemplate).convertAndSendToUser(any(), any(), any());

        // A exceção não deve vazar para a thread chamadora
        listener.handleNotificationEvent(event);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
