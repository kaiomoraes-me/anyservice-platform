package com.anyservice.backend.service;

import com.anyservice.backend.controller.dto.CreateServiceListingDto;
import com.anyservice.backend.controller.dto.ServiceListingDto;
import com.anyservice.backend.model.ServiceListing;
import com.anyservice.backend.model.User;
import com.anyservice.backend.repository.ServiceListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceListingService {

    private final ServiceListingRepository repository;

    public ServiceListingService(ServiceListingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ServiceListingDto createListing(User provider, CreateServiceListingDto dto) {
        ServiceListing listing = new ServiceListing();
        listing.setTitle(dto.getTitle());
        listing.setDescription(dto.getDescription());
        listing.setPrice(dto.getPrice());
        listing.setCategory(dto.getCategory());
        listing.setProvider(provider);

        ServiceListing saved = repository.save(listing);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ServiceListingDto> getAllListings() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceListingDto> getListingsByCategory(String category) {
        return repository.findByCategoryOrderByCreatedAtDesc(category)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceListingDto> getMyListings(User provider) {
        return repository.findByProviderIdOrderByCreatedAtDesc(provider.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceListingDto getListingById(Long id) {
        ServiceListing listing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        return mapToDto(listing);
    }

    @Transactional
    public void deleteListing(User provider, Long id) {
        ServiceListing listing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        
        if (!listing.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Não tens permissão para apagar este serviço");
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
        dto.setCreatedAt(entity.getCreatedAt());

        ServiceListingDto.ProviderDto providerDto = new ServiceListingDto.ProviderDto();
        providerDto.setId(entity.getProvider().getId());
        providerDto.setName(entity.getProvider().getName());
        providerDto.setProfilePictureUrl(entity.getProvider().getProfilePictureUrl());
        
        dto.setProvider(providerDto);
        return dto;
    }
}
