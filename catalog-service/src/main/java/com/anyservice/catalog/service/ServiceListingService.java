package com.anyservice.catalog.service;

import com.anyservice.catalog.dto.CreateServiceListingDto;
import com.anyservice.catalog.dto.ServiceListingDto;
import com.anyservice.catalog.model.ServiceListing;
import com.anyservice.catalog.repository.ServiceListingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceListingService {

    private final ServiceListingRepository repository;

    public ServiceListingService(ServiceListingRepository repository) {
        this.repository = repository;
    }

    @CacheEvict(value = "catalog", allEntries = true)
    public ServiceListingDto createListing(CreateServiceListingDto dto, Long providerId) {
        ServiceListing listing = new ServiceListing();
        listing.setTitle(dto.getTitle());
        listing.setDescription(dto.getDescription());
        listing.setPrice(dto.getPrice());
        listing.setCategory(dto.getCategory());
        listing.setProviderId(providerId);

        listing = repository.save(listing);
        return mapToDto(listing);
    }

    @Cacheable(value = "catalog")
    public List<ServiceListingDto> getAllListings() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ServiceListingDto> getListingsByCategory(String category) {
        return repository.findByCategory(category).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ServiceListingDto getListingById(Long id) {
        ServiceListing listing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
        return mapToDto(listing);
    }

    public List<ServiceListingDto> getListingsByProvider(Long providerId) {
        return repository.findByProviderId(providerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "catalog", allEntries = true)
    public ServiceListingDto updateListing(Long id, CreateServiceListingDto dto, Long providerId) {
        ServiceListing listing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));

        if (!listing.getProviderId().equals(providerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }

        listing.setTitle(dto.getTitle());
        listing.setDescription(dto.getDescription());
        listing.setPrice(dto.getPrice());
        listing.setCategory(dto.getCategory());

        listing = repository.save(listing);
        return mapToDto(listing);
    }

    @CacheEvict(value = "catalog", allEntries = true)
    public void deleteListing(Long id, Long providerId) {
        ServiceListing listing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));

        if (!listing.getProviderId().equals(providerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }

        repository.delete(listing);
    }

    private ServiceListingDto mapToDto(ServiceListing entity) {
        ServiceListingDto dto = new ServiceListingDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setCategory(entity.getCategory());
        dto.setProviderId(entity.getProviderId());
        
        // Em um microserviço real, providerName e providerUsername seriam buscados do user-service via FeignClient
        // ou agregados no API Gateway. Por enquanto preenchemos com valores padrão ou vazios.
        dto.setProviderName("Provider");
        dto.setProviderUsername("provider");
        
        return dto;
    }
}
