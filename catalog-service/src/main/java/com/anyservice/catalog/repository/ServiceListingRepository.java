package com.anyservice.catalog.repository;

import com.anyservice.catalog.model.ServiceListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceListingRepository extends JpaRepository<ServiceListing, Long> {
    List<ServiceListing> findByCategory(String category);
    List<ServiceListing> findByProviderId(Long providerId);
}
