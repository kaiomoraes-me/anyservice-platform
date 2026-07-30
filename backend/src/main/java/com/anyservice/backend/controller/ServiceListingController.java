package com.anyservice.backend.controller;

import com.anyservice.backend.controller.dto.CreateServiceListingDto;
import com.anyservice.backend.controller.dto.MessageResponse;
import com.anyservice.backend.controller.dto.ServiceListingDto;
import com.anyservice.backend.model.User;
import com.anyservice.backend.service.ServiceListingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Exposes REST endpoints for browsing, creating, and managing service listings. */
@RestController
@RequestMapping("/api/services")
public class ServiceListingController {

    private final ServiceListingService serviceListingService;

    public ServiceListingController(ServiceListingService serviceListingService) {
        this.serviceListingService = serviceListingService;
    }

    /** Returns all listings, optionally filtered by category (public endpoint). */
    @GetMapping
    public ResponseEntity<List<ServiceListingDto>> getAllListings(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(serviceListingService.getListingsByCategory(category));
        }
        return ResponseEntity.ok(serviceListingService.getAllListings());
    }

    /** Returns a single listing by ID (public endpoint). */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceListingDto> getListingById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceListingService.getListingById(id));
    }

    /** Returns all listings created by the authenticated provider. */
    @GetMapping("/me")
    public ResponseEntity<List<ServiceListingDto>> getMyListings(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(serviceListingService.getMyListings(currentUser));
    }

    /** Creates a new service listing for the authenticated provider. */
    @PostMapping
    public ResponseEntity<ServiceListingDto> createListing(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateServiceListingDto request) {
        return ResponseEntity.ok(serviceListingService.createListing(currentUser, request));
    }

    /** Deletes a service listing owned by the authenticated provider. */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteListing(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        serviceListingService.deleteListing(currentUser, id);
        return ResponseEntity.ok(new MessageResponse("Serviço apagado com sucesso."));
    }
}
