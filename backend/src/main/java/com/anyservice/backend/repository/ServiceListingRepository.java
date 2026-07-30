package com.anyservice.backend.repository;

import com.anyservice.backend.model.ServiceListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceListingRepository extends JpaRepository<ServiceListing, Long> {
    
    // Encontrar os serviços criados por um prestador específico
    List<ServiceListing> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    // Encontrar todos os serviços de uma categoria
    List<ServiceListing> findByCategoryOrderByCreatedAtDesc(String category);
    
    // Todos os serviços ordenados pelos mais recentes
    List<ServiceListing> findAllByOrderByCreatedAtDesc();
}
