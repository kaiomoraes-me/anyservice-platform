package com.anyservice.order.controller;

import com.anyservice.order.dto.ServiceOrderDto;
import com.anyservice.order.model.ServiceOrder;
import com.anyservice.order.repository.ServiceOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class ServiceOrderController {

    private final ServiceOrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Value("${catalog-service.url:http://localhost:8082/api/services}")
    private String catalogServiceUrl;

    public ServiceOrderController(ServiceOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/me")
    public ResponseEntity<List<ServiceOrderDto>> getMyOrders(Principal principal) {
        Long currentUserId = Long.valueOf(principal.getName());
        List<ServiceOrderDto> dtos = new ArrayList<>();

        List<ServiceOrder> asClient = orderRepository.findByClientIdOrderByCreatedAtDesc(currentUserId);
        for (ServiceOrder order : asClient) {
            dtos.add(mapToDto(order, "CLIENT"));
        }

        List<ServiceOrder> asProvider = orderRepository.findByProviderIdOrderByCreatedAtDesc(currentUserId);
        for (ServiceOrder order : asProvider) {
            dtos.add(mapToDto(order, "PROVIDER"));
        }

        dtos.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderDto> getOrderById(@PathVariable Long id, Principal principal) {
        ServiceOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido nÃƒÂ£o encontrado"));
        Long currentUserId = Long.valueOf(principal.getName());
        
        String myRole = "NONE";
        if (order.getClientId().equals(currentUserId)) {
            myRole = "CLIENT";
        } else if (order.getProviderId().equals(currentUserId)) {
            myRole = "PROVIDER";
        } else {
            throw new RuntimeException("Acesso negado: nÃƒÂ£o pertences a este pedido.");
        }
        
        return ResponseEntity.ok(mapToDto(order, myRole));
    }

    private ServiceOrderDto mapToDto(ServiceOrder order, String myRole) {
        ServiceOrderDto dto = new ServiceOrderDto();
        dto.setId(order.getId());
        dto.setServiceId(order.getServiceListingId());
        
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(catalogServiceUrl + "/public/listings/" + order.getServiceListingId(), Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                dto.setServiceTitle((String) response.getBody().get("title"));
            } else {
                dto.setServiceTitle("ServiÃƒÂ§o Desconhecido");
            }
        } catch (Exception e) {
            dto.setServiceTitle("ServiÃƒÂ§o IndisponÃƒÂ­vel");
        }
        
        dto.setClientName("Client " + order.getClientId());
        dto.setProviderName("Provider " + order.getProviderId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setProviderEarnings(order.getProviderEarnings());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setMyRole(myRole);
        return dto;
    }
}
