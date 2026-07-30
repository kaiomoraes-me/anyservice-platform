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

/** Receives and processes Stripe webhook events (e.g., checkout.session.completed). */
@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    private final PaymentService paymentService;

    public StripeWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Validates the Stripe signature and processes payment completion events. */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        String secret = endpointSecret != null ? endpointSecret.trim() : "";

        try {
            event = Webhook.constructEvent(payload, sigHeader, secret);
        } catch (SignatureVerificationException e) {
            System.out.println("⚠️ Webhook signature verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        } catch (Exception e) {
            System.out.println("⚠️ Webhook processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            StripeObject stripeObject = null;
            if (deserializer.getObject().isPresent()) {
                stripeObject = deserializer.getObject().get();
            } else {
                try {
                    stripeObject = deserializer.deserializeUnsafe();
                } catch (com.stripe.exception.EventDataObjectDeserializationException e) {
                    System.out.println("⚠️ Failed to deserialize Stripe event: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
                }
            }

            if (stripeObject instanceof Session session) {
                System.out.println("✅ Payment completed for session: " + session.getId());
                paymentService.markOrderAsPaid(session.getId());
            }
        }

        return ResponseEntity.ok("");
    }
}
