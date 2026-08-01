<h1 align="center">
  AnyService Platform
</h1>

<p align="center">
  <img alt="Status" src="https://img.shields.io/badge/Status-Em%20Desenvolvimento-orange">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.1-6DB33F?logo=springboot&logoColor=white">
  <img alt="Angular" src="https://img.shields.io/badge/Angular-18-DD0031?logo=angular&logoColor=white">
  <img alt="Docker" src="https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white">
  <img alt="Stripe" src="https://img.shields.io/badge/Stripe-Integration-008CDD?logo=stripe&logoColor=white">
  <img alt="AWS" src="https://img.shields.io/badge/AWS-EC2-232F3E?logo=amazon-aws&logoColor=white">
</p>

## Sobre o Projeto

O **AnyService** é uma plataforma idealizada para conectar prestadores de serviços de diversas áreas (Encanadores, Eletricistas, Designers, Desenvolvedores, etc.) a clientes que buscam soluções rápidas e confiáveis. 

A plataforma foi desenvolvida com foco em **arquitetura limpa (Clean Code/SOLID)**, escalabilidade e segurança, utilizando as melhores práticas do mercado corporativo.

> [!WARNING]
> **Aviso de Status:** Este projeto encontra-se em fase de **Desenvolvimento Ativo**. Funcionalidades estão sendo aprimoradas, e o banco de dados pode ser resetado durante ciclos de teste.

> [!IMPORTANT]
> **Fim Educacional:** Este projeto foi construído **exclusivamente para fins educacionais e de portfólio**. Não possui fins lucrativos e não processa pagamentos ou dados reais em ambiente de produção.

---

## Acesso ao Ambiente de Produção (Live Demo)

A aplicação está hospedada na nuvem da AWS (EC2) com Continuous Deployment (CD) configurado via GitHub Actions.

**Acesse a plataforma aqui:** [http://16.192.77.236/](http://16.192.77.236/)

*(Nota: A configuração de DNS e certificados SSL/HTTPS será implementada em futuras iterações da infraestrutura).*

### Dados para Teste (Ambiente Sandbox)

Como a plataforma está em modo de teste, sinta-se à vontade para explorar os fluxos de contratação e pagamento.

**Cartão de Crédito de Teste (Stripe Sandbox):**
- **Número do Cartão:** `4242 4242 4242 4242`
- **Validade:** Qualquer data futura (ex: `12/30`)
- **CVC:** Qualquer código de 3 dígitos (ex: `123`)
- **Nome/CEP:** Qualquer dado válido

**Validação de SMS (Vonage/Nexmo):**
A funcionalidade de verificação de conta via SMS está totalmente implementada no código (via Vonage API). No entanto, devido ao uso da camada gratuita (Free Tier) da Vonage, **o envio de SMS só funciona para números previamente autorizados/cadastrados na dashboard da Vonage do desenvolvedor**. 
> Se testar com um número não cadastrado na nossa conta Vonage, o SMS de código não será entregue, mas o console do servidor logará o código gerado em ambiente de desenvolvimento.

---

## Arquitetura e Stack Tecnológica

O projeto adota uma arquitetura baseada em microsserviços lógicos, separando claramente o Frontend do Backend, conteinerizados e orquestrados via Docker Compose.

### Backend (API RESTful)
- **Linguagem/Framework:** Java 17, Spring Boot 3.1
- **Segurança:** Autenticação Stateless com **Spring Security** e **JWT (JSON Web Tokens)**.
- **Persistência:** Spring Data JPA / Hibernate conectado a um banco de dados **PostgreSQL 15**.
- **Integrações de Terceiros:**
  - **Stripe API:** Processamento de pagamentos (Checkout Sessions & Webhooks para assincronicidade).
  - **Vonage (Nexmo) SDK:** Envio de SMS para MFA/verificação.
  - **JavaMailSender:** Envio de e-mails transacionais (ex: recuperação de senha).

### Frontend (Single Page Application - SPA)
- **Linguagem/Framework:** TypeScript, Angular 18
- **Estilização:** CSS moderno, design responsivo, Glassmorphism, e animações fluidas.
- **Roteamento & Estado:** Angular Router, interceptores HTTP para injeção de JWT.
- **Servidor Web:** Nginx (configurado via proxy reverso e gerencialmente de rotas do Angular via `try_files`).

### DevOps e Infraestrutura
- **Contêinerização:** Docker e Docker Compose. (Uso de *Multi-stage builds* no Dockerfile para otimizar o tamanho da imagem de produção).
- **CI/CD:** Pipeline automatizada usando **GitHub Actions**. A cada push na `main`, os testes rodam, as imagens são buildadas e o deploy ocorre via SSH na AWS.
- **Cloud Hosting:** Instância Amazon EC2 (Ubuntu).

---

## Como Rodar Localmente

Caso seja um desenvolvedor, avaliador técnico ou queira contribuir, siga o guia abaixo para rodar toda a infraestrutura em sua máquina.

### Pré-requisitos
- [Docker](https://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/) instalados.
- [Node.js 22+](https://nodejs.org/) (apenas se for rodar o Frontend fora do Docker).
- [Java 17+](https://adoptium.net/) e Maven (apenas se for rodar o Backend fora do Docker).
- [Git](https://git-scm.com/)

### 1. Clonar o Repositório
```bash
git clone https://github.com/kaiomoraes-me/anyservice-platform.git
cd anyservice-platform
```

### 2. Configurar Variáveis de Ambiente
Na raiz do repositório clonado, crie um arquivo chamado `.env` e configure as credenciais necessárias. (Você precisará de chaves de teste para o Stripe e Vonage para o sistema completo funcionar):

```env
# Banco de Dados
DB_HOST=db
DB_USER=postgres
DB_PASSWORD=sua_senha_segura
DB_NAME=anyservice_db

# Segurança JWT
JWT_SECRET=super_secret_jwt_key_that_should_be_at_least_256_bits_long

# URL Base (Necessário para o CORS e redirecionamento do Stripe)
APP_BASE_URL=http://localhost:4200

# E-mail (SMTP)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=seu_email@gmail.com
SMTP_PASSWORD=sua_senha_de_app

# Integrações
VONAGE_API_KEY=sua_chave_vonage
VONAGE_API_SECRET=seu_secret_vonage
STRIPE_SECRET_KEY=sk_test_sua_chave_stripe
STRIPE_WEBHOOK_SECRET=whsec_seu_webhook_secret
```

### 3. Iniciar a Aplicação com Docker Compose
Com o `.env` configurado, basta um comando para levantar o Banco de Dados, o Backend e o Frontend:

```bash
docker compose up --build -d
```
Após o build finalizar, acesse:
- **Frontend:** `http://localhost:80` (O Nginx servirá o Angular nesta porta localmente).
- **Backend API:** `http://localhost:8080/api`

---

## Licença
Este é um projeto acadêmico/portfólio e está licenciado sob os termos da licença MIT (sem restrições de uso educacional).
