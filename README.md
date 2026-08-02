# Anyservice Platform

**Anyservice** é uma plataforma distribuída (baseada em microsserviços) construída para conectar **Clientes** e **Prestadores de Serviços**. O sistema suporta um ciclo de vida completo: desde a oferta do serviço no catálogo, passando pelo pagamento simulado e culminando em um chat em tempo real liberado após a confirmação do pagamento.

> **Aviso Importante:** Este projeto não possui fins lucrativos. Foi desenhado e implementado exclusivamente para fins educacionais, validação de arquiteturas escaláveis (Graph Engineering e Event-Driven Architecture) e portfólio. As compras realizadas na plataforma são simuladas e não geram cobranças reais.

---

## 🚀 Como Usar a Plataforma

Para garantir a melhor experiência, o ecossistema deve ser executado localmente utilizando o **Docker Compose**. O ambiente já inclui bancos de dados isolados, RabbitMQ para mensageria, API Gateway e um servidor SMTP local (MailDev) para testes.

### 1. Inicializando o Ambiente

Na raiz do projeto (onde se encontra o `docker-compose.yml`), execute o comando para compilar e iniciar todos os contêineres:

```bash
docker-compose up --build -d
```

Aguarde até que os serviços `user-service`, `catalog-service`, `order-service`, `chat-service`, `notification-service`, `api-gateway` e `frontend` estejam de pé.
O Frontend (UI) estará acessível em: **[http://localhost:4200](http://localhost:4200)** (caso rode o Node.js manualmente) ou **[http://localhost](http://localhost)** via Docker.

### 2. Cadastro e Verificação de E-mail (MailDev)

Acesse a plataforma, clique em **Registrar** e crie a sua conta (como `CLIENT` ou `PROVIDER`). 
O sistema exige a verificação de e-mail antes do login.

- Como estamos em ambiente de desenvolvimento, não enviamos e-mails de verdade. Em vez disso, utilizamos o **MailDev**.
- Acesse a interface do MailDev em: **[http://localhost:1080](http://localhost:1080)**
- Lá você encontrará o e-mail contendo o seu **código de verificação secreto**. Copie o código, ative sua conta e faça o login.

### 3. Criando um Serviço (Perfil: Prestador)

Se você se cadastrou como **Prestador de Serviço (`PROVIDER`)**:
1. Após o login, vá para o seu **Dashboard** ou seção **Meus Serviços**.
2. Clique em **Novo Serviço**.
3. Preencha o título, descrição, categoria e o valor do seu serviço. 
4. O seu serviço será publicado no catálogo global instantaneamente (gerenciado pelo `catalog-service`).

### 4. Pagamento e Contratação (Perfil: Cliente)

Se você se cadastrou como **Cliente (`CLIENT`)**:
1. Na página inicial, navegue pelo catálogo e escolha um serviço oferecido por um Prestador.
2. Clique em **Contratar** ou **Comprar**.
3. Você será redirecionado para o Checkout (integrado em modo de teste).
4. **Cartão de Crédito Falso:** Você pode simular a compra utilizando os cartões de teste da Stripe. 
   - **Número do Cartão:** `4242 4242 4242 4242`
   - **Validade:** Qualquer data futura (ex: `12/34`)
   - **CVC:** Qualquer número de 3 dígitos (ex: `123`)
5. Ao confirmar, o `order-service` processará o pagamento de forma simulada.

### 5. Conversação no Chat (Tempo Real)

Uma vez que o pagamento foi confirmado pelo sistema (Webhook da Stripe -> RabbitMQ -> Order Service), o canal de comunicação entre o **Cliente** e o **Prestador** é liberado.

1. Acesse a aba **Mensagens / Chat**.
2. A sala de bate-papo correspondente àquela ordem de serviço estará ativa.
3. Você pode trocar mensagens em tempo real com o prestador/cliente. Toda a comunicação ocorre via WebSockets (gerenciado pelo `chat-service`).

### 6. Notificações (Push)

O sistema conta com um módulo de notificações distribuídas (`notification-service`). Quando uma compra é aprovada ou uma nova mensagem chega, o backend emite eventos via RabbitMQ. O Frontend, conectado via WebSocket/STOMP, recebe a notificação em tempo real, informando o usuário sem a necessidade de recarregar a página.

---

## 📐 Arquitetura do Sistema

A plataforma Anyservice abandonou as amarras do monólito e abraçou a complexidade dos Microsserviços para garantir resiliência (*Harness Engineering*) e escalabilidade horizontal:

- **Frontend:** Angular 17+ (Design Estrito e Monocromático: apenas Preto `#000000` e Branco `#FFFFFF`).
- **API Gateway:** Spring Cloud Gateway (Porta 8080) com Circuit Breakers.
- **User Service:** Emissão de JWT, autenticação e perfis (Porta 8081).
- **Catalog Service:** Gestão de anúncios (Porta 8082).
- **Order Service:** Checkouts e processamento de status via Webhooks (Porta 8083).
- **Chat Service:** WebSockets para conversas pós-pagamento (Porta 8084).
- **Notification Service:** WebSockets/STOMP para avisos globais (Porta 8085).
- **Mensageria:** RabbitMQ para coreografia SAGA e processamento assíncrono.
- **Dados:** Um container PostgreSQL para cada serviço (Database-per-Service isolado).

---
*Este é um projeto Open-Source para estudos. Sinta-se livre para clonar e explorar a infraestrutura.*
