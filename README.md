# HookLens – Webhook Management Platform

A backend application built with Spring Boot that allows developers to create webhook endpoints, receive HTTP events, and inspect stored webhook data securely.

## Features

- **User Authentication:** JWT-based authentication for secure API access.
- **API Authorization:** API key-based authorization for developer APIs.
- **Webhook Management:** Create and manage webhook endpoints.
- **Webhook Ingestion:** Receive and store incoming HTTP events.
- **Event Storage:** Store request headers, query parameters, and JSON payloads in PostgreSQL.
- **JSON Redaction:** Recursively mask sensitive fields such as passwords, API keys, and access tokens.
- **Event Filtering:** Retrieve events with pagination, HTTP method, and date-range filters.
- **Event Statistics:** Retrieve webhook event statistics.

## Tech Stack

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT
- Jackson
- JUnit
- Maven
- Postman

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register a user |
| POST | `/api/v1/auth/login` | Authenticate a user |
| GET | `/api/v1/auth/me` | Get authenticated user details |
| POST | `/api/v1/endpoints` | Create a webhook endpoint |
| POST | `/e/{webhook-token}` | Receive webhook events |
| GET | `/api/v1/developer/endpoints/{endpointId}/events` | Retrieve webhook events |
| GET | `/api/v1/developer/events/{eventId}` | Retrieve a specific event |
| GET | `/api/v1/developer/endpoints/{endpointId}/stats` | Get event statistics |

## Getting Started

### Prerequisites

- Java
- Maven
- PostgreSQL

### 1. Clone the repository

```bash
git clone <your-repository-url>
cd HookLens
```

### 2. Create the database

Create a PostgreSQL database named:

```sql
CREATE DATABASE hooklens_db;
```

### 3. Configure environment variables

Configure the following environment variables:

```text
DB_PASSWORD=your_database_password
JWT_SECRET=your_jwt_secret
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The application will start at:

```text
http://localhost:8080
```

## Testing

- Unit tests using JUnit.
- API testing using Postman.
- Authentication and authorization testing.
- JSON redaction testing.
- Webhook ingestion and event retrieval testing.

## Project Purpose

HookLens is designed to help developers inspect incoming webhook requests, debug event payloads, and protect sensitive information through recursive JSON redaction.