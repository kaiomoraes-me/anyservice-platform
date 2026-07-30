package com.anyservice.backend.controller;

import com.anyservice.backend.controller.dto.CheckoutResponse;
import com.anyservice.backend.model.User;
import com.anyservice.backend.service.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout/{serviceId}")
    public ResponseEntity<CheckoutResponse> checkout(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long serviceId) {
        
        try {
            String checkoutUrl = paymentService.createCheckoutSession(currentUser, serviceId);
            return ResponseEntity.ok(new CheckoutResponse(checkoutUrl));
        } catch (StripeException e) {
            throw new RuntimeException("Erro ao comunicar com o Stripe: " + e.getMessage());
        }
    }
}
