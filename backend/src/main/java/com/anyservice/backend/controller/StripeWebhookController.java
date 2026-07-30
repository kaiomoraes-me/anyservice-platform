package com.anyservice.backend.controller;

import com.anyservice.backend.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    private final PaymentService paymentService;

    public StripeWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event = null;

        System.out.println("🔔 Recebido Webhook do Stripe!");
        System.out.println("SigHeader: " + sigHeader);
        
        String secret = endpointSecret != null ? endpointSecret.trim() : "";
        System.out.println("Secret configurado (tamanho): " + secret.length());

        try {
            event = Webhook.constructEvent(payload, sigHeader, secret);
        } catch (SignatureVerificationException e) {
            System.out.println("⚠️ Assinatura do Webhook falhou.");
            System.out.println("Erro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        } catch (Exception e) {
            System.out.println("⚠️ Outro erro no Webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }

        // Lidar com o evento de pagamento concluído
        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            
            StripeObject stripeObject = null;
            if (dataObjectDeserializer.getObject().isPresent()) {
                stripeObject = dataObjectDeserializer.getObject().get();
            } else {
                try {
                    stripeObject = dataObjectDeserializer.deserializeUnsafe();
                } catch (com.stripe.exception.EventDataObjectDeserializationException e) {
                    System.out.println("⚠️ Erro ao forçar leitura do Stripe: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
                }
            }

            if (stripeObject instanceof Session) {
                Session session = (Session) stripeObject;
                System.out.println("✅ Pagamento concluído para a Sessão: " + session.getId());
                // Marca o Pedido como PAGO e libera o Chat
                paymentService.markOrderAsPaid(session.getId());
            }
        }

        return ResponseEntity.ok("");
    }
}
