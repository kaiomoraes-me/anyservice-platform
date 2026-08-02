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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Handles Stripe Checkout session creation and order lifecycle. */
@Service
public class PaymentService {

    private final ServiceOrderRepository orderRepository;
    private final ServiceListingRepository listingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.base-url:http://localhost:4200}")
    private String appBaseUrl;

    public PaymentService(ServiceOrderRepository orderRepository, ServiceListingRepository listingRepository, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
        this.eventPublisher = eventPublisher;
    }

    /** Creates a Stripe Checkout session and persists a PENDING order in the database. */
    @Transactional
    public String createCheckoutSession(User client, Long serviceId) throws StripeException {
        ServiceListing service = listingRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        if (service.getProvider().getId().equals(client.getId())) {
            throw new RuntimeException("Não podes contratar o teu próprio serviço.");
        }

        ServiceOrder order = new ServiceOrder();
        order.setClient(client);
        order.setProvider(service.getProvider());
        order.setServiceListing(service);
        order.setTotalPrice(service.getPrice());
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);

        long amountInCents = service.getPrice().multiply(new BigDecimal("100")).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(appBaseUrl + "/payment/success?order_id=" + order.getId())
                .setCancelUrl(appBaseUrl + "/payment/cancel")
                .putMetadata("order_id", order.getId().toString())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
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
                .build();

        Session session = Session.create(params);

        order.setStripeSessionId(session.getId());
        orderRepository.save(order);

        return session.getUrl();
    }

    /** Marks an order as PAID after Stripe webhook confirmation. */
    @Transactional
    public void markOrderAsPaid(String stripeSessionId) {
        ServiceOrder order = orderRepository.findByStripeSessionId(stripeSessionId);
        if (order != null && order.getStatus() != OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            
            // Disparar evento de notificação para o prestador (Assíncrono)
            eventPublisher.publishEvent(new com.anyservice.backend.service.event.NotificationEvent(
                    order.getProvider().getId(),
                    order.getClient().getId(),
                    "SERVICE_ORDER",
                    order.getId(),
                    "PURCHASED"
            ));
        }
    }
}
