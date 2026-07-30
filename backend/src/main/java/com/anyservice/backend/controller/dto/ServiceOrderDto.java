package com.anyservice.backend.controller.dto;

import com.anyservice.backend.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceOrderDto {
    private Long id;
    private Long serviceId;
    private String serviceTitle;
    private String clientName;
    private String providerName;
    private BigDecimal totalPrice;
    private BigDecimal providerEarnings;
    private OrderStatus status;
    private LocalDateTime createdAt;
    
    // Indica se eu sou o cliente neste pedido ou o prestador
    private String myRole; 

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public String getServiceTitle() { return serviceTitle; }
    public void setServiceTitle(String serviceTitle) { this.serviceTitle = serviceTitle; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public BigDecimal getProviderEarnings() { return providerEarnings; }
    public void setProviderEarnings(BigDecimal providerEarnings) { this.providerEarnings = providerEarnings; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getMyRole() { return myRole; }
    public void setMyRole(String myRole) { this.myRole = myRole; }
}
