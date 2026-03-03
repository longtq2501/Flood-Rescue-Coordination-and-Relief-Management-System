# 🌊 Flood Rescue Coordination and Relief Management System

> A microservices-based platform for managing flood emergency rescue operations — from citizen rescue requests to team dispatching, relief supply tracking, and real-time coordination.

![Java](https://img.shields.io/badge/Java-Spring%20Boot-6DB33F?style=flat&logo=springboot&logoColor=white)
![Next.js](https://img.shields.io/badge/Frontend-Next.js-000000?style=flat&logo=nextdotjs&logoColor=white)
![Docker](https://img.shields.io/badge/Deploy-Docker-2496ED?style=flat&logo=docker&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/Broker-RabbitMQ-FF6600?style=flat&logo=rabbitmq&logoColor=white)
![Kafka](https://img.shields.io/badge/Streaming-Kafka-231F20?style=flat&logo=apachekafka&logoColor=white)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=flat&logo=githubactions&logoColor=white)

---

## 📌 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Actors & Roles](#actors--roles)
- [Tech Stack](#tech-stack)
- [Microservices](#microservices)
- [Getting Started](#getting-started)
- [Deployment on VPS](#deployment-on-vps)
- [CI/CD Pipeline](#cicd-pipeline)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

---

## 📖 Overview

During flood disasters, rescue and relief operations often suffer from fragmented information, delayed responses, and poor coordination between forces. This system addresses those challenges by providing a **centralized microservices platform** that:

- Accepts and tracks rescue requests from citizens in real time
- Dispatches rescue teams and vehicles efficiently via a coordination dashboard
- Monitors relief supply inventory and distribution
- Communicates between services via **RabbitMQ** (task events & notifications) and **Kafka** (real-time data streaming)
- Deployed on a **VPS** using **Docker Compose** with automated **CI/CD via GitHub Actions**

---

## 🏗️ Architecture

```
                        ┌────────────────────┐
                        │   Next.js Frontend  │
                        └────────┬───────────┘
                                 │ HTTPS
                        ┌────────▼───────────┐
                        │   Nginx Reverse     │
                        │      Proxy          │
                        └──┬──────┬──────┬───┘
                           │      │      │
              ┌────────────▼─┐ ┌──▼───┐ ┌▼────────────┐
              │ Rescue Request│ │ User │ │  Resource   │
              │   Service     │ │ Svc  │ │  Service    │
              └──────┬────────┘ └──┬───┘ └──────┬──────┘
                     │             │             │
              ┌──────▼─────────────▼─────────────▼──────┐
              │              Message Layer                │
              │  RabbitMQ  (task events & notifications)  │
              │  Kafka     (real-time streams & logs)     │
              └──────┬─────────────┬─────────────┬───────┘
                     │             │             │
              ┌──────▼──┐   ┌──────▼──┐   ┌─────▼───────┐
              │Dispatch │   │ Notif.  │   │ Reporting   │
              │ Service │   │ Service │   │  Service    │
              └─────────┘   └─────────┘   └─────────────┘
```

---

## ✨ Features

### 👤 Citizen
- Submit rescue requests with location, description, and images
- Track request status and receive real-time notifications
- Confirm rescue completion or relief received

### 🚤 Rescue Team
- Receive assigned rescue missions
- View request details and rescue location on map
- Update task progress and report results

### 🧭 Rescue Coordinator
- Receive and verify incoming rescue requests
- Classify urgency levels
- Dispatch rescue teams and vehicles
- Monitor and adjust request handling in real time

### 🗂️ Manager
- Manage rescue vehicles and their availability status
- Manage relief supply warehouse and inventory
- Track and record relief distribution
- Generate resource usage statistics

### 🔧 Admin
- Manage user accounts and role-based access control
- Configure system categories and parameters
- Generate comprehensive activity reports

---

## 👥 Actors & Roles

| Role | Description |
|------|-------------|
| **Citizen** | Flood victims who submit rescue or relief requests |
| **Rescue Team** | Field teams executing assigned rescue missions |
| **Rescue Coordinator** | Operators who verify requests and dispatch resources |
| **Manager** | Oversees vehicles, inventory, and resource statistics |
| **Admin** | System administrator with full configuration access |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | Next.js (React) |
| **Backend Services** | Spring Boot (Java 17) |
| **API Proxy** | Nginx (Reverse Proxy) |
| **Message Broker** | RabbitMQ — task events & notifications |
| **Data Streaming** | Apache Kafka — real-time streams & logs |
| **Database** | PostgreSQL (per service) |
| **Maps** | Google Maps API / Leaflet.js |
| **Auth** | JWT + Spring Security |
| **Containerization** | Docker + Docker Compose |
| **CI/CD** | GitHub Actions |
| **Deployment** | VPS (Ubuntu) |

---

## 🧩 Microservices

| Service | Responsibility | Port |
|---------|---------------|------|
| `user-service` | Authentication, user management, role-based access | 8081 |
| `rescue-request-service` | Submit, verify, classify, and track rescue requests | 8082 |
| `dispatch-service` | Assign teams and vehicles to requests | 8083 |
| `resource-service` | Vehicle fleet and relief supply management | 8084 |
| `notification-service` | Real-time alerts (RabbitMQ consumer) | 8085 |
| `reporting-service` | Statistics, activity reports, resource usage | 8086 |

> Each service owns its **own database** following the *Database per Service* pattern.

**Message Flow:**
- `rescue-request-service` → **RabbitMQ** → `dispatch-service` (new request assigned)
- `dispatch-service` → **RabbitMQ** → `notification-service` (status update alerts)
- All services → **Kafka** → `reporting-service` (event logging & analytics)

---

## 🚀 Getting Started

### Prerequisites

```bash
Java >= 17
Maven >= 3.8
Node.js >= 18.x
Docker >= 24.x
Docker Compose >= 2.x
```

### Run Locally (Development)

```bash
# 1. Clone the repository
git clone https://github.com/your-org/flood-rescue-system.git
cd flood-rescue-system

# 2. Start local infrastructure (PostgreSQL, RabbitMQ, Kafka, Zookeeper)
docker compose -f docker-compose.infra.yml up -d

# 3. Start all backend services
cd services/user-service && mvn spring-boot:run &
cd services/rescue-request-service && mvn spring-boot:run &
# ... repeat for each service

# 4. Start frontend
cd frontend
npm install
npm run dev
```

### Environment Variables

Each service has its own `.env` file. Copy from examples:

```bash
cp services/user-service/.env.example          services/user-service/.env
cp services/rescue-request-service/.env.example services/rescue-request-service/.env
# ... repeat for each service
cp frontend/.env.example frontend/.env.local
```

Key variables:

```env
# Database (per service)
DB_URL=jdbc:postgresql://localhost:5432/service_db
DB_USERNAME=postgres
DB_PASSWORD=secret

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000

# Frontend (Next.js)
NEXT_PUBLIC_API_BASE_URL=http://localhost
NEXT_PUBLIC_MAPS_API_KEY=your_google_maps_api_key
```

---

## 🖥️ Deployment on VPS

### 1. Prepare the VPS

```bash
# Install Docker & Docker Compose
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Install Nginx
sudo apt install nginx -y
```

### 2. Deploy with Docker Compose

```bash
# SSH into VPS and clone the repo
git clone https://github.com/your-org/flood-rescue-system.git /opt/flood-rescue-system
cd /opt/flood-rescue-system

# Copy and configure production env files
cp .env.prod.example .env.prod

# Start all services
docker compose -f docker-compose.prod.yml up -d --build

# Verify running containers
docker compose -f docker-compose.prod.yml ps
```

### 3. Nginx Reverse Proxy Config

```nginx
server {
    listen 80;
    server_name yourdomain.com;

    location /api/users/      { proxy_pass http://localhost:8081; }
    location /api/requests/   { proxy_pass http://localhost:8082; }
    location /api/dispatch/   { proxy_pass http://localhost:8083; }
    location /api/resources/  { proxy_pass http://localhost:8084; }
    location /api/reports/    { proxy_pass http://localhost:8086; }
    location /                { proxy_pass http://localhost:3000; }  # Next.js
}
```

> 💡 Enable HTTPS with **Let's Encrypt + Certbot**: `sudo certbot --nginx -d yourdomain.com`

---

## ⚙️ CI/CD Pipeline

Automated pipeline via **GitHub Actions** triggered on every push to `main`:

```
Push to main
    │
    ├── 🧪 Run unit tests (each service)
    ├── 🐳 Build Docker images
    ├── 📦 Push images to Docker Hub / GHCR
    └── 🚀 SSH into VPS → git pull → docker compose up -d
```

Pipeline config: `.github/workflows/deploy.yml`

```yaml
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Build Docker Images
        run: docker compose -f docker-compose.prod.yml build

      - name: Push to Docker Hub
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          docker compose -f docker-compose.prod.yml push

      - name: Deploy to VPS via SSH
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.VPS_HOST }}
          username: ${{ secrets.VPS_USER }}
          key: ${{ secrets.VPS_SSH_KEY }}
          script: |
            cd /opt/flood-rescue-system
            git pull origin main
            docker compose -f docker-compose.prod.yml up -d --build
```

**Required GitHub Secrets:**

| Secret | Description |
|--------|-------------|
| `VPS_HOST` | VPS IP address or domain |
| `VPS_USER` | SSH username (e.g. `ubuntu`) |
| `VPS_SSH_KEY` | Private SSH key |
| `DOCKER_USERNAME` | Docker Hub username |
| `DOCKER_PASSWORD` | Docker Hub access token |

---

## 📁 Project Structure

```
flood-rescue-system/
├── services/
│   ├── user-service/                # Spring Boot — Auth & Users
│   ├── rescue-request-service/      # Spring Boot — Rescue Requests
│   ├── dispatch-service/            # Spring Boot — Team Dispatching
│   ├── resource-service/            # Spring Boot — Vehicles & Inventory
│   ├── notification-service/        # Spring Boot — RabbitMQ Consumer
│   └── reporting-service/           # Spring Boot — Kafka Consumer & Reports
├── frontend/                        # Next.js App
├── infrastructure/
│   ├── nginx/
│   │   └── nginx.conf
│   └── kafka/
│       └── kafka-config.yml
├── docker-compose.infra.yml         # Local infra (DB, RabbitMQ, Kafka)
├── docker-compose.prod.yml          # Production full stack
├── .github/
│   └── workflows/
│       └── deploy.yml               # CI/CD pipeline
└── README.md
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a new branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

Please follow the [Conventional Commits](https://www.conventionalcommits.org/) standard for commit messages.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">
  <sub>Built with ❤️ to help save lives during flood emergencies.</sub>
</div>