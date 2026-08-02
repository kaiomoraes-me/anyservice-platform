package com.anyservice.order.repository;

import com.anyservice.order.model.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    List<ServiceOrder> findByClientIdOrderByCreatedAtDesc(Long clientId);
    List<ServiceOrder> findByProviderIdOrderByCreatedAtDesc(Long providerId);
    ServiceOrder findByStripeSessionId(String stripeSessionId);
}
