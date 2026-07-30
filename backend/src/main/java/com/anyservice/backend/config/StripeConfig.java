package com.anyservice.backend.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/** Initializes the Stripe SDK with the secret API key at application startup. */
@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    /** Sets the global Stripe API key. Logs a warning if the key is empty. */
    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isBlank()) {
            System.out.println("⚠️ WARNING: Stripe secret key is empty. Payment features will not work.");
            return;
        }
        Stripe.apiKey = secretKey;
        System.out.println("✅ Stripe initialized successfully.");
    }
}
