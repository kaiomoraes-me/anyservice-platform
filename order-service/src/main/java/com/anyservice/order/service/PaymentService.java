package com.anyservice.order.service;

import com.anyservice.order.model.OrderStatus;
import com.anyservice.order.model.ServiceOrder;
import com.anyservice.order.repository.ServiceOrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

@Service
public class PaymentService {

    private final ServiceOrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;

    @Value("${app.base-url:http://localhost:4200}")
    private String appBaseUrl;

    @Value("${catalog-service.url:http://localhost:8082/api/services}")
    private String catalogServiceUrl;

    public PaymentService(ServiceOrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public String createCheckoutSession(Long clientId, Long serviceId) throws StripeException {
        // Fetch service listing details from catalog-service
        ResponseEntity<Map> response = restTemplate.getForEntity(catalogServiceUrl + "/public/listings/" + serviceId, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("ServiÃ§o nÃ£o encontrado no catÃ¡logo.");
        }
        
        Map<String, Object> serviceListing = response.getBody();
        Long providerId = ((Number) serviceListing.get("providerId")).longValue();
        BigDecimal price = new BigDecimal(serviceListing.get("price").toString());
        String title = (String) serviceListing.get("title");

        if (providerId.equals(clientId)) {
            throw new RuntimeException("NÃ£o podes contratar o teu prÃ³prio serviÃ§o.");
        }

        ServiceOrder order = new ServiceOrder();
        order.setClientId(clientId);
        order.setProviderId(providerId);
        order.setServiceListingId(serviceId);
        order.setTotalPrice(price);
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);

        long amountInCents = price.multiply(new BigDecimal("100")).longValue();

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
                                                                .setName(title)
                                                                .setDescription("ID do Prestador: " + providerId)
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

    @Transactional
    public void markOrderAsPaid(String stripeSessionId) {
        ServiceOrder order = orderRepository.findByStripeSessionId(stripeSessionId);
        if (order != null && order.getStatus() != OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            
            // Disparar evento de notificaÃ§Ã£o via RabbitMQ
            Map<String, Object> event = new HashMap<>();
            event.put("providerId", order.getProviderId());
            event.put("clientId", order.getClientId());
            event.put("entityType", "SERVICE_ORDER");
            event.put("entityId", order.getId());
            event.put("action", "PURCHASED");
            rabbitTemplate.convertAndSend("anyservice_events", "notification.created", event);
        }
    }
}
