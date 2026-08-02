package com.anyservice.catalog.controller;

import com.anyservice.catalog.dto.CreateServiceListingDto;
import com.anyservice.catalog.dto.MessageResponse;
import com.anyservice.catalog.dto.ServiceListingDto;
import com.anyservice.catalog.service.ServiceListingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceListingController {

    private final ServiceListingService serviceListingService;

    public ServiceListingController(ServiceListingService serviceListingService) {
        this.serviceListingService = serviceListingService;
    }

    @PostMapping("/listings")
    public ResponseEntity<ServiceListingDto> createListing(
            @Valid @RequestBody CreateServiceListingDto request,
            Principal principal) {
        Long providerId = Long.valueOf(principal.getName());
        
        // Ensure user is PROVIDER
        Authentication auth = (Authentication) principal;
        if (auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_PROVIDER"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceListingService.createListing(request, providerId));
    }

    @GetMapping("/public/listings")
    public ResponseEntity<List<ServiceListingDto>> getAllListings() {
        return ResponseEntity.ok(serviceListingService.getAllListings());
    }

    @GetMapping("/public/listings/category/{category}")
    public ResponseEntity<List<ServiceListingDto>> getListingsByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(serviceListingService.getListingsByCategory(category));
    }

    @GetMapping("/public/listings/{id}")
    public ResponseEntity<ServiceListingDto> getListingById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceListingService.getListingById(id));
    }

    @PutMapping("/listings/{id}")
    public ResponseEntity<ServiceListingDto> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody CreateServiceListingDto request,
            Principal principal) {
        Long providerId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(serviceListingService.updateListing(id, request, providerId));
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<MessageResponse> deleteListing(
            @PathVariable Long id,
            Principal principal) {
        Long providerId = Long.valueOf(principal.getName());
        serviceListingService.deleteListing(id, providerId);
        return ResponseEntity.ok(new MessageResponse("ServiÃ§o removido com sucesso"));
    }
}
