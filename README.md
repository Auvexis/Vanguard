<div align="center">

<img width="1401" height="200" alt="vanguard_banner" src="https://github.com/user-attachments/assets/2e233c5e-7d19-48f9-915e-904993c12f88" />

<br>

> <h3>Vanguard is a production-ready authentication platform that demonstrates secure JWT handling, refresh token rotation, adaptive rate limiting, and event-driven architecture using Spring Boot 3.</h3>

🌐 Try Vanguard Live Demo: https://vanguard.auvexis.com
<br><br>
[🚀 Getting Started](#-getting-started) • 
[🔌 API Reference](#-api-reference) • 
[🔒 Security Features](#-security-features) • 
[🏗 Architecture](#-architecture)

</div>

## Technical Stack

- **Framework**: Spring Boot 3.x (Java 17)
- **Security**: Spring Security 6
- **Persistence**: PostgreSQL & Redis
- **Messaging**: RabbitMQ
- **Email**: Resend API
- **Infrastructure**: Docker & Docker Compose

---

## 🏗 Architecture

Vanguard follows a modular architecture, separating responsibilities into clear bounded contexts:

- **`auth`**: Handles registration, login, token lifecycle, and session management.
- **`email`**: Asynchronous consumer for dispatching system notifications.
- **`shared`**: Common utilities, global error handling, and cross-cutting events.

---

## 🚀 Getting Started

### Pre-requisites

- Docker & Docker Compose
- JDK 17 (for local builds)
- Maven

### Environment Configuration

The application requires several environment variables. Copy the example file and fill in your credentials:

```bash
cp .env.example .env.dev
```

| Variable                 | Description                      | Default |
| ------------------------ | -------------------------------- | ------- |
| `SPRING_PROFILES_ACTIVE` | Active profile (`prod` or `dev`) | `prod`  |
| `VANGUARD_DB_URL`        | PostgreSQL connection string     | -       |
| `VANGUARD_REDIS_URL`     | Redis connection string          | -       |
| `VANGUARD_RABBITMQ_URL`  | RabbitMQ connection string       | -       |
| `VANGUARD_JWT_SECRET`    | HS256 Secret (Base64)            | -       |
| `VANGUARD_RESEND_APIKEY` | Email provider API Key           | -       |

### Running with Docker (Recommended)

Launch the entire ecosystem (App, Postgres, Redis, RabbitMQ) with a single command:

```bash
cd docker
docker compose -f docker-compose.dev.yaml up --build
```

---

## 🔌 API Reference

### Authentication Lifecycle

All endpoints are prefixed with `/api/v1/auth`.

| Method | Endpoint                     | Description                    | Auth Required | Rate Limited |
| ------ | ---------------------------- | ------------------------------ | ------------- | ------------ |
| `POST` | `/register`                  | Create a new user account      | No            | Yes          |
| `POST` | `/login`                     | Authenticate and get JWT       | No            | Yes          |
| `GET`  | `/me`                        | Get current user profile       | **Yes**       | No           |
| `POST` | `/refresh`                   | Rotate expired access tokens   | No            | Yes          |
| `POST` | `/logout`                    | Invalidate current session     | **Yes**       | No           |
| `POST` | `/verify-email`              | Confirm email ownership        | No            | Yes          |
| `POST` | `/resend-verification-email` | Request new verification email | No            | Yes          |

### System Health

- **GET** `/api/v1/health`: Returns service heartbeat and status.

---

## 🔒 Security Features

### Adaptive Rate Limiting

Vanguard implements a Token Bucket algorithm to prevent Brute Force and DoS attacks. Sensitive endpoints like `/login` and `/register` have strict limits configured via the `@RateLimit` annotation.

### Token Lifecycle

- **Access Token**: Short-lived JWT (default 15 mins) for request authorization.
- **Refresh Token**: Opaque token stored in the database for secure session rotation.

---

## 🧪 Testing

The project includes a comprehensive test suite for both unit and integration testing.

```bash
./mvnw test
```

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
