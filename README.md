# FLOOD RESCUE COORDINATION & RELIEF MANAGEMENT SYSTEM

> A microservices platform built for real flood emergency scenarios — coordinating rescue requests, team dispatching, GPS tracking, relief supply management, and real-time operations monitoring across 5 actor roles.

[![Java](https://img.shields.io/badge/Java-Spring%20Boot-6DB33F?style=flat&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Frontend-Next.js-000000?style=flat&logo=nextdotjs&logoColor=white)](https://nextjs.org)
[![Docker](https://img.shields.io/badge/Deploy-Docker-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com)
[![RabbitMQ](https://img.shields.io/badge/Broker-RabbitMQ-FF6600?style=flat&logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com)
[![Kafka](https://img.shields.io/badge/Streaming-Kafka-231F20?style=flat&logo=apachekafka&logoColor=white)](https://kafka.apache.org)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=flat&logo=githubactions&logoColor=white)](https://github.com/features/actions)

> 🚧 **Active Development** — Currently in Sprint 1. Architecture, infrastructure, and module skeleton are complete. Feature implementation is in progress across the team.

---

## Context

Flood disasters in Vietnam suffer from a coordination gap: rescue requests come in through informal channels, teams are dispatched manually, and relief supply tracking is done on paper. This system centralizes the entire operation — from a citizen submitting a GPS-tagged rescue request to a coordinator dispatching the nearest available team, tracked live on a map.

**This is a 6-person team project.** As Tech Lead, my responsibilities cover: architecture decisions, tech stack selection, microservices module skeleton setup (frontend + backend), full infrastructure configuration, CI/CD pipeline, and sprint management via Jira.

---

## Operational Flow

The system supports 5 actor roles with distinct workflows that connect end-to-end:

```
CITIZEN
  → Submit rescue request (GPS + description + photos + headcount)
  → Receive SSE notification: "Team dispatched"
  → Confirm rescue completion

        ↓

COORDINATOR
  → Receive SSE alert: "New request"
  → Verify request (PENDING → VERIFIED)
  → Open map: view available rescue teams by proximity & capacity
  → Assign team + vehicle
  → Monitor GPS tracking of team in real time

        ↓

RESCUE TEAM
  → Receive SSE alert: "New mission assigned"
  → View mission details (address, headcount, description)
  → Start → GPS auto-reports every 10s → Coordinator sees live on map
  → Complete + submit result notes

        ↓

CITIZEN
  → Receive SSE: "Rescue team completed"
  → Confirm → Request moves to CONFIRMED

        ↓

MANAGER (parallel oversight)
  → Dashboard: daily request count, completion rate, avg response time
  → Relief supply warehouse management → distribute to victims
  → Low-stock alerts
  → Per-team performance reports
```

---

## Architecture

### Why Microservices — and Why Modular Monolith First

The system is architected as microservices, but **development begins as a modular monolith**. This is a deliberate engineering decision: establish correct business logic and service boundaries before introducing the operational overhead of distributed systems. Once Sprint 1 business flows are validated, services will be extracted independently.

Each service owns its own database (Database per Service pattern), communicates asynchronously via message brokers, and is independently deployable.

```
                    ┌──────────────────────┐
                    │    Next.js Frontend   │
                    └──────────┬───────────┘
                               │ HTTPS
                    ┌──────────▼───────────┐
                    │    Nginx Reverse Proxy │
                    └───┬──────┬───────┬───┘
                        │      │       │
         ┌──────────────▼─┐ ┌──▼───┐ ┌▼─────────────┐
         │ rescue-request  │ │ user │ │   resource   │
         │    service      │ │ svc  │ │   service    │
         └───────┬─────────┘ └──┬───┘ └──────┬───────┘
                 │               │             │
         ┌───────▼───────────────▼─────────────▼───────┐
         │                 Message Layer                 │
         │   RabbitMQ — task events & notifications      │
         │   Kafka    — real-time streams & audit logs   │
         └───────┬───────────────┬─────────────┬────────┘
                 │               │             │
         ┌───────▼────┐  ┌───────▼──┐  ┌──────▼────────┐
         │  dispatch  │  │ notif.   │  │  reporting    │
         │  service   │  │ service  │  │  service      │
         └────────────┘  └──────────┘  └───────────────┘
```

**Message flow:**
- `rescue-request-service` → **RabbitMQ** → `dispatch-service` (new request ready for assignment)
- `dispatch-service` → **RabbitMQ** → `notification-service` (status updates to citizens & teams via SSE)
- All services → **Kafka** → `reporting-service` (event streaming for analytics & audit logs)

### Why RabbitMQ and Kafka — not just one?

They solve different problems. RabbitMQ handles **task-based messaging** — discrete events that must be consumed exactly once (a rescue request assigned to exactly one team). Kafka handles **event streaming** — high-throughput, ordered, replayable logs that feed the reporting service. Using one for both would mean either losing replay capability or overcomplicating task routing.

---

## Microservices

| Service | Responsibility | Port |
|:--------|:--------------|:----:|
| `user-service` | Auth, user management, RBAC | 8081 |
| `rescue-request-service` | Submit, verify, classify, track requests | 8082 |
| `dispatch-service` | Assign teams & vehicles, GPS tracking | 8083 |
| `resource-service` | Vehicle fleet & relief supply inventory | 8084 |
| `notification-service` | SSE delivery (RabbitMQ consumer) | 8085 |
| `reporting-service` | Statistics, performance reports (Kafka consumer) | 8086 |

---

## Tech Stack

```
Frontend          Next.js · React · TypeScript
                  Tailwind CSS · Shadcn/UI
                  Google Maps API / Leaflet.js

Backend           Spring Boot (Java 17) — per service
                  JPA/Hibernate · PostgreSQL (per service)
                  Spring Security · JWT

Messaging         RabbitMQ — task events & notifications
                  Apache Kafka — streaming & audit logs

Infrastructure    Nginx (reverse proxy)
                  Docker + Docker Compose
                  VPS (Ubuntu) deployment
                  GitHub Actions (CI/CD)
```

---

## Infrastructure & CI/CD

Infrastructure is fully configured and deployed. The pipeline triggers on every push to `main`:

```
Push to main
  │
  ├── Build Docker images (per service)
  ├── Push to Docker Hub
  └── SSH into VPS → docker compose up -d
```

### Local Development Setup

```bash
# Clone
git clone https://github.com/longtq2501/Flood-Rescue-Coordination-and-Relief-Management-System.git
cd Flood-Rescue-Coordination-and-Relief-Management-System

# Start infrastructure (PostgreSQL, RabbitMQ, Kafka, Zookeeper)
docker compose -f docker-compose.infra.yml up -d

# Backend (repeat per service)
cd backend/user-service && mvn spring-boot:run

# Frontend
cd frontend && npm install && npm run dev
```

### Production Deployment (VPS)

```bash
# On VPS
git clone ... /opt/flood-rescue
cd /opt/flood-rescue
docker compose -f docker-compose.prod.yml up -d --build
```

Nginx routes requests by path prefix to the appropriate service. HTTPS via Let's Encrypt + Certbot.

---

## Project Structure

```
flood-rescue-system/
├── backend/
│   ├── user-service/
│   ├── rescue-request-service/
│   ├── dispatch-service/
│   ├── resource-service/
│   ├── notification-service/
│   └── reporting-service/
├── frontend/
├── infrastructure/
│   ├── nginx/nginx.conf
│   └── kafka/kafka-config.yml
├── docs/
├── docker-compose.infra.yml
├── docker-compose.prod.yml
└── .github/workflows/deploy.yml
```

---

## Team & Workflow

**Team size:** 6 members  
**Project management:** Jira (sprint planning, task breakdown, progress tracking)  
**Branching strategy:** `feature/*` → `develop` → `main` (merge on sprint completion)

**Tech Lead responsibilities (Tôn Quỳnh Long):**
- Architecture design & service boundary decisions
- Tech stack selection
- Module skeleton setup — frontend features, backend service structure
- Full infrastructure configuration (Docker, Nginx, Kafka, RabbitMQ)
- CI/CD pipeline setup (GitHub Actions → VPS)
- Sprint planning & task delegation via Jira

Team members are currently implementing features within the established skeleton across all services and the frontend.

---

## Development Status

| Area | Status |
|:-----|:------:|
| Architecture & service boundaries | ✅ Complete |
| Infrastructure (Docker, Nginx, Kafka, RabbitMQ) | ✅ Complete |
| CI/CD pipeline | ✅ Complete |
| Module skeleton (FE + BE) | ✅ Complete |
| Feature implementation | 🚧 In Progress |
| Integration testing | ⏳ Planned |
| Production deployment | ⏳ Post Sprint 1 |

---

## Author & Contact

**Tôn Quỳnh Long** — Third-year IT student, Tech Lead  
Concurrently maintaining [Tutor Pro](https://github.com/longtq2501/Tutor-Pro) — a solo full-stack production project.

📧 tonquynhlong05@gmail.com  
🔗 [GitHub](https://github.com/longtq2501) · [Linkedln](https://www.linkedin.com/in/ton-quynh-long-dev)
