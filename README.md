# RFQ Auction System

A full-stack **Request for Quotation (RFQ) Auction System** with real-time bid rankings, automated deadline extension logic, and complete Docker + CI/CD setup.

---

## Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Backend    | Spring Boot 3.2 (Java 17) + Maven   |
| Frontend   | React 18 + Vite + Recharts          |
| Database   | MySQL 8.0                           |
| Container  | Docker + Docker Compose             |
| CI/CD      | GitHub Actions → Docker Hub         |

---

## Quick Start (Docker)

```bash
# Clone the repo
git clone <your-repo-url>
cd rfq-auction

# Start all 3 containers (MySQL + Backend + Frontend)
docker-compose up --build
```

| Service  | URL                        |
|----------|----------------------------|
| Frontend | http://localhost:3000       |
| Backend  | http://localhost:8080       |
| MySQL    | localhost:3307 (host port)  |

---

## REST API Reference

| Method | Endpoint                | Description               |
|--------|-------------------------|---------------------------|
| POST   | /api/rfq/create         | Create RFQ auction        |
| GET    | /api/rfq/all            | List all auctions         |
| GET    | /api/rfq/active         | List active auctions      |
| GET    | /api/rfq/{id}           | Get auction by ID         |
| PUT    | /api/rfq/close/{id}     | Force-close an auction    |
| POST   | /api/bid/submit         | Submit a bid              |
| GET    | /api/bid/{rfqId}        | All bids for an auction   |
| GET    | /api/ranking/{rfqId}    | L1/L2/L3 rankings         |
| GET    | /api/activity/{rfqId}   | Activity log              |

---

## Bid Extension Logic

When a bid is submitted, the system evaluates:

| Mode       | Trigger condition                                  |
|------------|----------------------------------------------------|
| TIME_BASED | Bid placed within X minutes of `scheduledCloseTime`|
| RANK_BASED | Bid changes the current L1 (lowest) supplier       |
| COMBINED   | **Both** conditions must be true                   |

If triggered, `scheduledCloseTime` is extended by N minutes.  
**`hardCloseTime` is NEVER exceeded** regardless of how many extensions occur.

---

## GitHub Actions CI/CD

Set these **Repository Secrets** in GitHub:

| Secret            | Value                          |
|-------------------|--------------------------------|
| `DOCKER_USERNAME` | Your Docker Hub username       |
| `DOCKER_PASSWORD` | Your Docker Hub access token   |

On every push to `main`:
1. JDK 17 setup + `mvn clean package`
2. Docker build & push backend image → `<user>/rfq-auction-backend:latest`
3. Docker build & push frontend image → `<user>/rfq-auction-frontend:latest`

---

## Project Structure

```
rfq-auction/
├── backend/                  # Spring Boot
│   ├── src/main/java/com/rfqauction/
│   │   ├── model/            # JPA entities
│   │   ├── repository/       # Spring Data repos
│   │   ├── service/          # Business logic + extension engine
│   │   ├── controller/       # REST endpoints
│   │   ├── dto/              # Request/Response DTOs
│   │   └── config/           # CORS config
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                 # React + Vite
│   ├── src/
│   │   ├── pages/            # Dashboard, CreateRfq, AuctionDetail
│   │   ├── components/       # Navbar, Countdown
│   │   └── api.js            # Axios client
│   ├── Dockerfile
│   └── nginx.conf
├── database/
│   └── init.sql              # Schema + seed data
├── .github/workflows/
│   └── main.yml              # CI/CD pipeline
└── docker-compose.yml
```
