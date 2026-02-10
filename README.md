# FicMart Payment Gateway

A production-inspired **payment gateway** built as a **modular monolith**, designed to demonstrate real-world payment concerns such as **idempotency**, **state management**, **concurrency**, **failure handling**, and **event-driven receipts**, while remaining simple enough to reason about and extend.

This project intentionally balances **practical realism** with **clarity**, making it suitable for:
- Backend engineering interviews
- Distributed systems practice
- Payment architecture learning
- Advanced Spring Boot / Redis / PostgreSQL usage

---

## Table of Contents

- [High-Level Architecture](#high-level-architecture)
- [Core Design Principles](#core-design-principles)
- [Module Breakdown](#module-breakdown)
- [Payment Lifecycle](#payment-lifecycle)
- [Request–Response Lifecycle](#requestresponse-lifecycle)
- [Idempotency Design (Redis + Database)](#idempotency-design-redis--database)
- [Event-Driven Receipts](#event-driven-receipts)
- [Swapping Event Systems](#swapping-event-systems)
- [Payment Provider Abstraction](#payment-provider-abstraction)
- [Mock Bank Design](#mock-bank-design)
- [Setup & Configuration](#setup--configuration)
- [Running with Docker](#running-with-docker)
- [Extending the Project](#extending-the-project)
- [Why This Architecture](#why-this-architecture)

---

## High-Level Architecture

This project is implemented as a **modular monolith** with strict internal boundaries.


The gateway owns **business correctness**, while the bank/provider owns **money correctness**.

---

## Core Design Principles

- **Idempotency-first**: Every payment operation is safe to retry.
- **State machines over flags**: Payment transitions are explicit and validated.
- **Concurrency-aware**: Database row locks simulate real banking behavior.
- **Infrastructure is swappable**: Redis Streams, Kafka, RabbitMQ, or DB-based events.
- **Provider-agnostic**: MockBank, PayPal-like, or future providers can be plugged in.

---

## Module Breakdown

### Payments (Gateway)
Responsible for:
- Public API (authorize / capture / void / refund)
- Idempotency enforcement
- Payment state management
- Receipt storage and printing

Key components:
- `PaymentGatewayService`
- `HybridIdempotencyService` (Redis + DB)
- `PaymentProvider` (port)
- `Payment` and `PaymentReceipt` entities

---

### Bank (Mock Bank)
Responsible for:
- Account balances
- Authorizations, captures, refunds
- Concurrency and database locking
- Publishing receipt events

Key components:
- `Account`
- `BankAuthorization`
- `BankTransaction`
- `MockBankCoreService`
- `ConcurrentMockBankProcessor`

---

## Payment Lifecycle


Rules:
- Full capture only
- Full refund only
- No partial operations
- USD only

These constraints keep the focus on **state management, idempotency, and failures**.

---

## Request–Response Lifecycle

### Authorization Flow

1. Client sends:

2. Gateway:
- Validates request
- Builds an application command
- Computes a request hash

3. Idempotency Service:
- Redis gate for in-flight requests
- DB for durable replay and audit

4. PaymentProvider:
- Mock bank authorizes funds
- DB locks ensure balance correctness

5. Response:
- Payment stored
- Result cached
- Idempotent replay enabled

---

## Idempotency Design (Redis + Database)

This project uses **hybrid idempotency**.

### Redis
- Fast-path lookup
- In-flight execution lock
- Response replay cache

### Database
- Durable source of truth
- Survives Redis restart
- Audit and security guarantees

Each operation:
- Has its own idempotency scope
- Uses request hashing
- Supports safe retries

---

## Event-Driven Receipts

- The bank publishes receipt events after:
- Capture
- Refund
- Void
- Events are written to a Redis Stream
- The gateway consumes and stores receipts
- Receipts can be printed or queried

This models **real settlement and reporting flows**.

---

## Swapping Event Systems

The event boundary is intentionally thin.

You can replace Redis Streams with:
- Kafka
- RabbitMQ
- Database outbox
- Spring application events

Only the publisher and consumer change —  
**gateway business logic remains untouched**.

---

## Payment Provider Abstraction

The gateway interacts only with:

```java
interface PaymentProvider {
authorize(...)
capture(...)
voidAuth(...)
refund(...)
}
#####
** Setup & Configuration
Requirements

Java 21+

Docker

Docker Compose

Environment Variables

SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ficmart
SPRING_DATASOURCE_USERNAME=ficmart
SPRING_DATASOURCE_PASSWORD=ficmart

REDIS_HOST=redis
REDIS_PORT=6379


##  Running with Docker
docker compose up --build

docker compose down

