package com.anyservice.backend.controller;

import com.anyservice.backend.controller.dto.ServiceOrderDto;
import com.anyservice.backend.model.ServiceOrder;
import com.anyservice.backend.model.User;
import com.anyservice.backend.repository.ServiceOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class ServiceOrderController {

    private final ServiceOrderRepository orderRepository;

    public ServiceOrderController(ServiceOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<List<ServiceOrderDto>> getMyOrders(@AuthenticationPrincipal User currentUser) {
        List<ServiceOrderDto> dtos = new ArrayList<>();

        // Encontrar os pedidos onde sou Cliente
        List<ServiceOrder> asClient = orderRepository.findByClientIdOrderByCreatedAtDesc(currentUser.getId());
        for (ServiceOrder order : asClient) {
            dtos.add(mapToDto(order, "CLIENT"));
        }

        // Encontrar os pedidos onde sou Prestador
        List<ServiceOrder> asProvider = orderRepository.findByProviderIdOrderByCreatedAtDesc(currentUser.getId());
        for (ServiceOrder order : asProvider) {
            dtos.add(mapToDto(order, "PROVIDER"));
        }

        // Ordenar misturado (Opcional, mas para já devolvemos ambos na mesma lista)
        dtos.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));

        return ResponseEntity.ok(dtos);
    }

    private ServiceOrderDto mapToDto(ServiceOrder order, String myRole) {
        ServiceOrderDto dto = new ServiceOrderDto();
        dto.setId(order.getId());
        dto.setServiceId(order.getServiceListing().getId());
        dto.setServiceTitle(order.getServiceListing().getTitle());
        dto.setClientName(order.getClient().getName());
        dto.setProviderName(order.getProvider().getName());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setProviderEarnings(order.getProviderEarnings());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setMyRole(myRole);
        return dto;
    }
}
