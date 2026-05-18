# RFQ (Request For Quotation) Auction System

A full-stack **Request for Quotation (RFQ) Auction System** with real-time bid rankings, automated deadline extension logic, and complete Docker + CI/CD setup.

---

## 1. Project Overview

- **Project Name:** RFQ (Request For Quotation) Auction System
- **Objective:** To architect and implement a full-stack RFQ Auction System that facilitates real-time bidding, rank-based leaderboard tracking, and sophisticated time-extension logic enforced by hard-deadline constraints.

---

## 2. Technology Stack & Tools Used

### Frontend
- **Framework:** React.js (v18.3)
- **Build Tool:** Vite (for fast, optimized builds and hot module replacement)
- **Routing:** React Router DOM (v6)
- **HTTP Client:** Axios (for connecting to backend REST APIs)
- **Data Visualization:** Recharts (for analytics and visual data representation)
- **Date Management:** date-fns (for handling complex auction deadlines and timestamps)

### Backend
- **Language:** Java 17
- **Framework:** Spring Boot (v3.2.5)
- **Core Dependencies:** 
  - Spring Web (for RESTful APIs)
  - Spring Data JPA (for ORM and database interactions)
  - Spring Validation (for data integrity)
- **Boilerplate Reduction:** Lombok
- **Build Tool:** Maven

### Database
- **RDBMS:** MySQL 8.0 (Persisting users, auctions, bids, and activity logs)

### DevOps, Architecture & Deployment
- **Containerization:** Docker (using multi-stage Dockerfiles for both frontend and backend)
- **Orchestration:** Docker Compose (used to spin up the frontend, backend, and MySQL database simultaneously in isolated containers)
- **CI/CD Pipeline:** GitHub Actions (Automated pipeline configured to trigger on pushes/pull requests to the `main` branch. It automatically builds the Maven backend, creates Docker images for both frontend and backend, and pushes them to Docker Hub)
- **Architecture Style:** Monolithic Backend with a Decoupled Single Page Application (SPA) Frontend, communicating via REST APIs with configured CORS policies.

---

## 3. Core Features & Functionalities

1. **Auction Management:** Users can create auctions with specific start times, end times, and base prices.
2. **Real-Time Bidding Engine:** Bidders can place competitive bids against active RFQs. 
3. **Rank-Based Leaderboard:** Dynamic tracking of bids to rank participants based on their submitted quotes.
4. **Time-Extension Logic & Hard Deadlines:** Sophisticated logic that handles auction extensions (e.g., if a bid is placed in the last few minutes) while strictly adhering to maximum hard-deadline constraints.
5. **Activity Logging:** System-wide tracking of user actions and bid placements (implemented via `ActivityLogRepository`).
6. **Cross-Origin Resource Sharing (CORS):** Securely configured to allow seamless communication between the React frontend and Spring Boot backend.

---

## 4. Quick Start (Docker)

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

## 5. REST API Reference

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

## 6. Bid Extension Logic

When a bid is submitted, the system evaluates:

| Mode       | Trigger condition                                  |
|------------|----------------------------------------------------|
| TIME_BASED | Bid placed within X minutes of `scheduledCloseTime`|
| RANK_BASED | Bid changes the current L1 (lowest) supplier       |
| COMBINED   | **Both** conditions must be true                   |

If triggered, `scheduledCloseTime` is extended by N minutes.  
**`hardCloseTime` is NEVER exceeded** regardless of how many extensions occur.

---

## 7. GitHub Actions CI/CD

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

## 8. Project Structure

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
