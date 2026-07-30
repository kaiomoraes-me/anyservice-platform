package com.anyservice.backend.controller;

import com.anyservice.backend.controller.dto.ChatMessageDto;
import com.anyservice.backend.controller.dto.CreateMessageDto;
import com.anyservice.backend.model.ChatMessage;
import com.anyservice.backend.model.OrderStatus;
import com.anyservice.backend.model.ServiceOrder;
import com.anyservice.backend.model.User;
import com.anyservice.backend.repository.ChatMessageRepository;
import com.anyservice.backend.repository.ServiceOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageRepository chatRepository;
    private final ServiceOrderRepository orderRepository;

    public ChatController(ChatMessageRepository chatRepository, ServiceOrderRepository orderRepository) {
        this.chatRepository = chatRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<List<ChatMessageDto>> getMessages(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User currentUser) {
        
        ServiceOrder order = validateChatAccess(orderId, currentUser);
        
        List<ChatMessageDto> messages = chatRepository.findByOrderIdOrderByTimestampAsc(orderId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable Long orderId,
            @RequestBody CreateMessageDto dto,
            @AuthenticationPrincipal User currentUser) {

        ServiceOrder order = validateChatAccess(orderId, currentUser);

        ChatMessage msg = new ChatMessage();
        msg.setOrder(order);
        msg.setSender(currentUser);
        msg.setContent(dto.getContent());
        msg = chatRepository.save(msg);

        return ResponseEntity.ok(mapToDto(msg));
    }

    private ServiceOrder validateChatAccess(Long orderId, User user) {
        ServiceOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (!order.getClient().getId().equals(user.getId()) && !order.getProvider().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado: não pertences a este pedido.");
        }

        if (order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("Acesso negado: o chat só é desbloqueado após o pagamento (PAID).");
        }

        return order;
    }

    private ChatMessageDto mapToDto(ChatMessage msg) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(msg.getId());
        dto.setContent(msg.getContent());
        dto.setSenderId(msg.getSender().getId());
        dto.setSenderName(msg.getSender().getName());
        dto.setTimestamp(msg.getTimestamp());
        return dto;
    }
}
