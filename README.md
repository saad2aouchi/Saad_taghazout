# Taghazout Surfing App - Microservices

A premium surfing hostel booking platform built with Spring Boot microservices and Flutter Web.

## 🚀 Quick Start (Dockerized)

The entire environment is containerized. To run the project on any machine (Laptop/Desktop) with Docker installed:

### 1. Prerequisites
- **Docker Desktop** (with Compose)
- **Java 17** (optional, if you want to run locally)
- **Flutter SDK** (optional, if you want to run locally)

### 2. First-time Setup
Once you pull the project from GitHub, run the following command from the root directory:

```powershell
docker-compose up -d --build
```
> [!NOTE]
> The first run will take a few minutes as it downloads Java and Flutter base images and Maven dependencies.

### 3. Service URLs
Once all containers are "Healthy" or "Running":

- **Frontend**: [http://127.0.0.1:52903](http://127.0.0.1:52903) (or [http://localhost:52903](http://localhost:52903))
- **API Gateway**: [http://localhost:8080](http://localhost:8080)
- **Eureka Dashboard**: [http://localhost:8762](http://localhost:8762)
- **Config Server**: [http://localhost:8899](http://localhost:8899)

---

## 🛠 Troubleshooting on New Machines

### 🔴 WebSocket/Connection Errors
If you see a "Connection error" on a new machine:
1. Ensure the API Gateway is running: `docker logs -f taghazout-app-api-gateway-1`.
2. Try using **`127.0.0.1`** instead of `localhost` in your browser.

### 🔴 Maven 502 Bad Gateway
If the build fails with a 502 error during `docker-compose up`:
1. It's a transient network issue from Maven Central.
2. **Retry** the command after 1 minute.

---

## 🏗 Architecture
- **API Gateway**: Spring Cloud Gateway (Security & Routing)
- **Discovery**: Eureka Server
- **Config**: Centralized Config Server (Native Profile)
- **Auth Service**: JWT Authentication & User Management
- **Listing Service**: Surfing Hostel management
- **Database**: PostgreSQL (Persistent)
- **Cache**: Redis (Token Revocation)
- **Frontend**: Flutter Web (Served via Nginx)
