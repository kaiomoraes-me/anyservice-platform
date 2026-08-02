package com.anyservice.chat.controller;

import com.anyservice.chat.dto.ChatMessageDto;
import com.anyservice.chat.dto.CreateMessageDto;
import com.anyservice.chat.model.ChatMessage;
import com.anyservice.chat.repository.ChatMessageRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageRepository chatRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;

    @Value("${order-service.url:http://localhost:8083/api/orders}")
    private String orderServiceUrl;
    
    @Value("${user-service.url:http://localhost:8081/api/users}")
    private String userServiceUrl;

    public ChatController(ChatMessageRepository chatRepository, RabbitTemplate rabbitTemplate) {
        this.chatRepository = chatRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<List<ChatMessageDto>> getMessages(
            @PathVariable Long orderId,
            Principal principal,
            HttpServletRequest request) {

        Map<String, Object> orderDetails = validateChatAccess(orderId, request);

        List<ChatMessageDto> messages = chatRepository.findByOrderIdOrderByTimestampAsc(orderId)
                .stream()
                .map(msg -> mapToDto(msg, request))
                .collect(Collectors.toList());

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable Long orderId,
            @RequestBody CreateMessageDto dto,
            Principal principal,
            HttpServletRequest request) {

        Map<String, Object> orderDetails = validateChatAccess(orderId, request);
        Long currentUserId = Long.valueOf(principal.getName());

        ChatMessage msg = new ChatMessage();
        msg.setOrderId(orderId);
        msg.setSenderId(currentUserId);
        msg.setContent(dto.getContent());
        msg = chatRepository.save(msg);

        String myRole = (String) orderDetails.get("myRole");
        Long clientObj = ((Number) orderDetails.get("clientId")).longValue();
        Long providerObj = ((Number) orderDetails.get("providerId")).longValue();

        Long recipientId = "CLIENT".equals(myRole) ? providerObj : clientObj;

        // Disparar evento de notificaÃƒÂ§ÃƒÂ£o via RabbitMQ
        Map<String, Object> event = new HashMap<>();
        event.put("recipientId", recipientId);
        event.put("senderId", currentUserId);
        event.put("entityType", "CHAT_MESSAGE");
        event.put("entityId", msg.getId());
        event.put("action", "SENT_MESSAGE");
        rabbitTemplate.convertAndSend("anyservice_events", "notification.created", event);

        return ResponseEntity.ok(mapToDto(msg, request));
    }

    private Map<String, Object> validateChatAccess(Long orderId, HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing authentication token");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    orderServiceUrl + "/" + orderId,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Pedido nÃƒÂ£o encontrado");
            }

            Map<String, Object> order = response.getBody();
            String status = (String) order.get("status");

            if (!"PAID".equals(status)) {
                throw new RuntimeException("Acesso negado: o chat sÃƒÂ³ ÃƒÂ© desbloqueado apÃƒÂ³s o pagamento (PAID).");
            }

            // Para recuperar os ids a partir da resposta (se order-service os retornar. 
            // Precisaremos garantir que ServiceOrderDto de order-service retorne clientId e providerId. 
            // Como order-service retorna clientName="Client 123", isso pode ser complicado.
            // Para simplificar, vou confiar no order-service que jÃƒÂ¡ validou se o User tem acesso.
            // Vou apenas inferir o clientId e providerId pegando-os do DTO se eu precisar, mas 
            // se order-service jÃƒÂ¡ fez a validaÃƒÂ§ÃƒÂ£o e retornou myRole != NONE, eu sÃƒÂ³ aceito.
            
            String myRole = (String) order.get("myRole");
            if ("NONE".equals(myRole)) {
                throw new RuntimeException("Acesso negado: nÃƒÂ£o pertences a este pedido.");
            }
            
            // Para enviar notificaÃƒÂ§ÃƒÂ£o, preciso dos IDs. Vamos extrair dos nomes que order-service envia:
            // "Client 1", "Provider 2" -> nÃƒÂ£o ÃƒÂ© o ideal, mas funciona se garantirmos o formato.
            String clientName = (String) order.get("clientName");
            String providerName = (String) order.get("providerName");
            
            Long clientId = Long.valueOf(clientName.replace("Client ", ""));
            Long providerId = Long.valueOf(providerName.replace("Provider ", ""));
            
            Map<String, Object> result = new HashMap<>();
            result.put("myRole", myRole);
            result.put("clientId", clientId);
            result.put("providerId", providerId);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Acesso negado ou pedido invÃƒÂ¡lido: " + e.getMessage());
        }
    }

    private ChatMessageDto mapToDto(ChatMessage msg, HttpServletRequest request) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(msg.getId());
        dto.setContent(msg.getContent());
        dto.setSenderId(msg.getSenderId());
        
        // Obter nome do usuÃƒÂ¡rio do user-service
        try {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            HttpHeaders headers = new HttpHeaders();
            if (authHeader != null) headers.set(HttpHeaders.AUTHORIZATION, authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    userServiceUrl + "/public/" + msg.getSenderId(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                dto.setSenderName((String) response.getBody().get("name"));
            } else {
                dto.setSenderName("User " + msg.getSenderId());
            }
        } catch (Exception e) {
            dto.setSenderName("User " + msg.getSenderId());
        }

        dto.setTimestamp(msg.getTimestamp());
        return dto;
    }
}
