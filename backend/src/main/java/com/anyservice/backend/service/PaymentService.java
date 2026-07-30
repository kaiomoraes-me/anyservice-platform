package com.anyservice.backend.service;

import com.anyservice.backend.model.OrderStatus;
import com.anyservice.backend.model.ServiceListing;
import com.anyservice.backend.model.ServiceOrder;
import com.anyservice.backend.model.User;
import com.anyservice.backend.repository.ServiceListingRepository;
import com.anyservice.backend.repository.ServiceOrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final ServiceOrderRepository orderRepository;
    private final ServiceListingRepository listingRepository;

    public PaymentService(ServiceOrderRepository orderRepository, ServiceListingRepository listingRepository) {
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public String createCheckoutSession(User client, Long serviceId) throws StripeException {
        ServiceListing service = listingRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        if (service.getProvider().getId().equals(client.getId())) {
            throw new RuntimeException("Não podes contratar o teu próprio serviço.");
        }

        // 1. Criar a Order (Pedido) no banco de dados como PENDING
        ServiceOrder order = new ServiceOrder();
        order.setClient(client);
        order.setProvider(service.getProvider());
        order.setServiceListing(service);
        order.setTotalPrice(service.getPrice());
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);

        // 2. Configurar o Stripe Checkout
        long amountInCents = service.getPrice().multiply(new java.math.BigDecimal("100")).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:4200/payment/success?order_id=" + order.getId())
                .setCancelUrl("http://localhost:4200/payment/cancel")
                .putMetadata("order_id", order.getId().toString())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur") // Suporta MBWay em Portugal
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(service.getTitle())
                                                                .setDescription("Prestado por: " + service.getProvider().getName())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                // O Stripe Connect exige mais configuração avançada para rotear os fundos automaticamente.
                // Como esta é uma versão simplificada, o dinheiro vem todo para a conta plataforma (tu),
                // e a tabela 'ServiceOrder' regista a matemática dos 30% e 70% para tu pagares ao prestador depois.
                .build();

        Session session = Session.create(params);
        
        // Guardar o ID da sessão do Stripe para validarmos depois
        order.setStripeSessionId(session.getId());
        orderRepository.save(order);

        return session.getUrl();
    }

    @Transactional
    public void markOrderAsPaid(String stripeSessionId) {
        ServiceOrder order = orderRepository.findByStripeSessionId(stripeSessionId);
        if (order != null) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            // Aqui seria desbloqueado o CHAT!
        }
    }
}
