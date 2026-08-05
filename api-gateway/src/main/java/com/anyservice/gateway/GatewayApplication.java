package com.anyservice.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver ipKeyResolver() {
        return exchange -> reactor.core.publisher.Mono.just(
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user_service", r -> r.path("/api/auth/**", "/api/users/**")
                .filters(f -> f.circuitBreaker(c -> c.setName("userServiceCB").setFallbackUri("forward:/fallback/user")))
                .uri("http://user-service:8081"))
                
            .route("catalog_service", r -> r.path("/api/services/**", "/api/categories/**")
                .filters(f -> f.circuitBreaker(c -> c.setName("catalogServiceCB").setFallbackUri("forward:/fallback/catalog")))
                .uri("http://catalog-service:8082"))
                
            .route("order_service", r -> r.path("/api/orders/**")
                .filters(f -> f.circuitBreaker(c -> c.setName("orderServiceCB").setFallbackUri("forward:/fallback/order")))
                .uri("http://order-service:8083"))
                
            .route("chat_service", r -> r.path("/api/chat/**", "/ws-chat/**")
                .uri("http://chat-service:8084"))
                
            .route("notification_service", r -> r.path("/api/notifications/**", "/ws-notifications/**")
                .uri("http://notification-service:8085"))
            .build();
    }
}
