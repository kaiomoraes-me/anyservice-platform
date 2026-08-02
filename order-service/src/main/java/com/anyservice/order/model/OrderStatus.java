package com.anyservice.order.model;

public enum OrderStatus {
    PENDING,    // Aguardando pagamento
    PAID,       // Pagamento efetuado, chat desbloqueado
    COMPLETED,  // Serviço prestado
    CANCELLED   // Pedido cancelado
}
