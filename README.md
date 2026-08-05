# Anyservice Platform

Anyservice is a microservices-based distributed platform connecting clients and service providers. It supports a full lifecycle: service cataloging, simulated payments, and real-time chat post-payment confirmation.

**Note**: This project is for educational purposes, architecture validation, and portfolio demonstration. Purchases are simulated and do not generate real charges.

## Local Environment Setup

The ecosystem runs locally via Docker Compose, including isolated databases, RabbitMQ, API Gateway, and an SMTP server (MailDev).

1. Execute the build and start command:
   ```bash
   docker-compose up --build -d
   ```
2. Wait for `user-service`, `catalog-service`, `order-service`, `chat-service`, `notification-service`, `api-gateway`, and `frontend` to start.
3. Access the Frontend at `http://localhost:4200` (Node.js) or `http://localhost` (Docker).

## System Usage

### Authentication
- Register as `CLIENT` or `PROVIDER`.
- Email verification is required. Verification codes are available via MailDev at `http://localhost:1080`.

### Service Management (Providers)
- Navigate to the Dashboard after login.
- Create a new service specifying title, description, category, and price.
- Services are instantly published to the global catalog via `catalog-service`.

### Checkout Flow (Clients)
- Select a service from the catalog and initiate purchase.
- The checkout uses Stripe in test mode.
  - Card: `4242 4242 4242 4242`
  - Expiration: Any future date
  - CVC: Any 3 digits
- The `order-service` processes the simulated payment.

### Real-Time Chat
- Chat channels open automatically upon payment confirmation (Stripe Webhook -> RabbitMQ -> Order Service).
- Communication uses WebSockets managed by `chat-service`.

### Distributed Notifications
- The `notification-service` handles global push notifications.
- Events like payment approval or new messages are emitted via RabbitMQ and delivered to the frontend via WebSocket/STOMP.

## Architecture

The platform implements the Database-per-Service pattern and event-driven choreography.

- **Frontend**: Angular 17+ (Monochromatic UI)
- **API Gateway**: Spring Cloud Gateway (Port 8080)
- **User Service**: JWT authentication and profiles (Port 8081)
- **Catalog Service**: Ad management (Port 8082)
- **Order Service**: Checkout and webhook processing (Port 8083)
- **Chat Service**: WebSocket communication (Port 8084)
- **Notification Service**: WebSocket/STOMP notifications (Port 8085)
- **Message Broker**: RabbitMQ
- **Database**: PostgreSQL (Isolated per service)

---
*Open-Source project for study purposes.*
