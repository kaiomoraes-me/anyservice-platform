# AnyService Platform

This project is currently under development.

AnyService is a distributed microservices platform designed for service listing and hiring. It implements a Database-per-Service architecture, event-driven choreography, and real-time communication.

**Notice:** This project is strictly for educational purposes. No real charges are processed. All payments are simulated using Stripe in test mode.

## Tech Stack

**Backend**
* Java 17
* Spring Boot 3.1.5
* Spring Cloud Gateway (API Gateway)
* Spring Security (JWT Authentication)
* Spring Data JPA / Hibernate

**Frontend**
* Angular 22 (Standalone Components)
* TypeScript
* RxJS
* SCSS

**Infrastructure & Data**
* PostgreSQL 15 (Database-per-Service)
* Redis 7 (In-Memory Cache & Rate Limiting)
* RabbitMQ 3 (Message Broker / Event-Driven Choreography)
* Docker & Docker Compose
* Zipkin (Distributed Tracing / OpenTelemetry)

**Integrations**
* Stripe API (Mock Payments)
* Vonage API (SMS Verification)
* SMTP (Email Delivery)

## Architecture Overview

The system is composed of five independent microservices behind an API Gateway:
1. **User Service:** Manages authentication, JWT generation, and high-performance Trie-based in-memory user search.
2. **Catalog Service:** Handles service listings and categories with Redis caching.
3. **Order Service:** Processes checkout flows and integrates with Stripe webhooks.
4. **Chat Service:** Manages WebSocket-based real-time communication between clients and providers.
5. **Notification Service:** Dispatches global push notifications via STOMP/WebSockets.

## Test Flow

Users can explore the following end-to-end flow:

1. **Authentication:** Register a new account and verify it via Email/SMS.
2. **Catalog Exploration:** Browse available services or use the top search bar for instant user discovery.
3. **Checkout Simulation:** Purchase a service using the Stripe test environment.
   * **Card Number:** 4242 4242 4242 4242
   * **Expiration Date:** Any future date (e.g., 12/28)
   * **CVC:** Any 3 digits
4. **Real-Time Interaction:** Upon payment approval, a chat channel is automatically provisioned via RabbitMQ events, allowing real-time messaging with the provider.

## Local Environment Setup

To run the platform on your local machine, ensure you have Git, Docker, and Docker Compose installed.

1. Clone the repository:
   ```bash
   git clone https://github.com/kaiomoraes-me/anyservice-platform.git
   cd anyservice-platform
   ```

2. Configure environment variables:
   Create a `.env` file at the root of the project and populate the required keys (Stripe, SMTP credentials, Vonage).

3. Boot the microservices cluster:
   ```bash
   docker-compose up --build -d
   ```

4. Access the application:
   * Frontend: `http://localhost`
   * API Gateway: `http://localhost:8080`
   * RabbitMQ Management: `http://localhost:15672`
   * Zipkin Tracing: `http://localhost:9411`
