package com.anyservice.order.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_orders")
public class ServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "service_listing_id", nullable = false)
    private Long serviceListingId;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private BigDecimal platformFee; // Os 30% da plataforma

    @Column(nullable = false)
    private BigDecimal providerEarnings; // Os 70% do prestador

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "stripe_session_id")
    private String stripeSessionId; // Referência do checkout do Stripe

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Métodos Auxiliares
    public void calculateFees() {
        if (this.totalPrice != null) {
            this.platformFee = this.totalPrice.multiply(new BigDecimal("0.30")); // 30%
            this.providerEarnings = this.totalPrice.subtract(this.platformFee);  // 70%
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public Long getServiceListingId() { return serviceListingId; }
    public void setServiceListingId(Long serviceListingId) { this.serviceListingId = serviceListingId; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { 
        this.totalPrice = totalPrice; 
        calculateFees();
    }
    public BigDecimal getPlatformFee() { return platformFee; }
    public BigDecimal getProviderEarnings() { return providerEarnings; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getStripeSessionId() { return stripeSessionId; }
    public void setStripeSessionId(String stripeSessionId) { this.stripeSessionId = stripeSessionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
