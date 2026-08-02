package com.anyservice.order.controller;

import com.anyservice.order.dto.CheckoutResponse;
import com.anyservice.order.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(
            @RequestBody Map<String, Long> payload,
            Principal principal) {
        try {
            Long serviceId = payload.get("serviceId");
            Long clientId = Long.valueOf(principal.getName());
            
            String checkoutUrl = paymentService.createCheckoutSession(clientId, serviceId);
            return ResponseEntity.ok(new CheckoutResponse(checkoutUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
