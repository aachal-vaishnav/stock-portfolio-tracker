# 📈 Indian Stock Portfolio Tracker + Tax Calculator

> A full-stack web application for tracking Indian stock investments (NSE/BSE) with automatic capital gains tax calculation as per Indian Income Tax rules.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)]() [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green.svg)]() [![H2](https://img.shields.io/badge/H2-Database-blue.svg)]() [![License](https://img.shields.io/badge/license-MIT-blue.svg)]()

---

## 🎯 Project Overview

A complete portfolio management system built for Indian retail investors. Tracks stock purchases/sales across NSE-listed companies, computes real-time profit/loss with simulated live prices, and generates **STCG (15%) and LTCG (10%) tax reports** with FIFO accounting as per Indian Income Tax Act.

### 🎨 Key Features

- 🔐 **User Authentication** — Spring Security with BCrypt password hashing
- 📊 **Live Portfolio Dashboard** — real-time P&L, holdings table, sector allocation
- 📈 **Interactive Charts** — pie chart by stock & sector (Chart.js)
- 💰 **Transaction Management** — BUY/SELL with full history
- 🧮 **Indian Tax Calculator** — STCG/LTCG with ₹1L exemption (FIFO method)
- 📄 **PDF Report Export** — downloadable capital gains report (iText)
- 🔄 **Auto Price Updates** — geometric random walk simulation every 30 sec
- 🗄️ **Embedded Database** — H2 file-based, browseable web console
- 📱 **Responsive UI** — Bootstrap 5, mobile-friendly

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.5 |
| **Web Layer** | Spring MVC + Thymeleaf |
| **Security** | Spring Security 6 + BCrypt |
| **Database** | H2 (embedded, file-based) |
| **ORM** | Spring Data JPA + Hibernate |
| **Frontend** | Bootstrap 5, Chart.js, Bootstrap Icons |
| **PDF** | iText 5.5.13 |
| **Build** | Maven 3.8+ |
| **Testing** | JUnit 5 + Mockito + Spring Security Test |

---

## 🚀 Quick Start

### Prerequisites
- JDK 17+
- Maven 3.8+

### Run
```bash
mvn spring-boot:run
```

Open browser: **http://localhost:8080**

**Default login:**
- Username: `admin`
- Password: `admin123`

➡️ **For detailed setup, see [INSTALL_GUIDE.md](INSTALL_GUIDE.md)**

---

## 📂 Project Structure

```
stock-tracker/
├── pom.xml
├── src/main/java/com/portfolio/
│   ├── PortfolioApplication.java     # Main Spring Boot entry
│   ├── config/
│   │   ├── SecurityConfig.java       # Spring Security setup
│   │   └── DataInitializer.java      # Seed default admin user
│   ├── model/                        # JPA entities
│   │   ├── User.java
│   │   ├── Stock.java
│   │   ├── Transaction.java
│   │   └── TransactionType.java
│   ├── repository/                   # Spring Data JPA repos
│   ├── service/                      # Business logic
│   │   ├── UserService.java          # Auth + registration
│   │   ├── PortfolioService.java     # Holdings + P&L
│   │   ├── TaxCalculatorService.java # STCG/LTCG (FIFO)
│   │   ├── PriceService.java         # Live price simulation
│   │   └── PdfReportService.java     # Tax PDF generation
│   ├── controller/                   # MVC + REST controllers
│   └── dto/                          # Data transfer objects
├── src/main/resources/
│   ├── application.properties        # Spring config
│   ├── data.sql                      # 20 NSE stocks seed
│   ├── templates/                    # Thymeleaf HTML pages
│   └── static/                       # CSS + JS
└── src/test/                         # Unit tests
```

---

## 💡 Key Algorithms

### 1. FIFO Tax Lot Matching
When a SELL happens, oldest BUY lots are matched first (Indian tax law). Each matched lot's holding period determines STCG vs LTCG classification.

```
For each SELL transaction:
    Match against BUY queue (FIFO)
    For each matched lot:
        days = sellDate - buyDate
        gain = (sellPrice - buyPrice) * matchedQty
        if days >= 365 → LTCG
        else            → STCG
```

### 2. Capital Gains Tax Rules (FY 2024-25)
- **STCG** (< 12 months): 15% flat rate, no exemption
- **LTCG** (>= 12 months): 10% on gains exceeding ₹1,00,000 per FY

### 3. Geometric Random Walk (Price Simulation)
```
newPrice = currentPrice × (1 + Z × σ)
where Z ~ N(0, 1)  // standard gaussian
      σ = volatility (0.5%)
```

---

## 🔐 Security Features

- **BCrypt password hashing** (configurable strength)
- **Session-based authentication** with Spring Security
- **CSRF protection** on all state-changing endpoints
- **Authorization checks** — users can only see their own data
- **SQL injection protection** via JPA parameterized queries

---

## 📊 Database Schema

```sql
USERS (id, username UNIQUE, password, email, full_name, created_at)
STOCKS (id, symbol UNIQUE, name, exchange, sector, current_price)
TRANSACTIONS (id, user_id FK, stock_id FK, type, quantity, price, transaction_date, created_at)
```

Browse the database at: **http://localhost:8080/h2-console**

---

## 🌐 REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/portfolio/summary` | Portfolio totals (invested, current value, P&L) |
| GET | `/api/portfolio/allocation` | Allocation by stock and by sector |

(MVC pages: `/`, `/login`, `/register`, `/dashboard`, `/transactions`, `/transactions/add`, `/tax`, `/tax/pdf/{fy}`)

---

## 🧪 Testing

```bash
mvn test
```

Tests cover:
- ✅ STCG calculation (sale within 12 months)
- ✅ LTCG below exemption (no tax)
- ✅ LTCG above exemption (10% on excess)
- ✅ FIFO matching with multiple buy lots

---

## 🎓 What This Project Demonstrates

For **interviewers / professors / recruiters:**

- ✅ Full-stack web development (backend + frontend + database)
- ✅ MVC architectural pattern with clean layering
- ✅ Production authentication and security practices
- ✅ ORM (Hibernate) with proper entity relationships
- ✅ RESTful API design
- ✅ Domain-specific business logic (Indian tax law)
- ✅ Algorithm implementation (FIFO accounting)
- ✅ External library integration (PDF generation)
- ✅ Unit testing with mocking
- ✅ Concurrent task scheduling (`@Scheduled`)
- ✅ Modern UI/UX (Bootstrap, Chart.js)

---

## 📜 License

MIT License. Free to use for learning and portfolio.

---

## 👨‍💻 Built By
Aachal Vaishnav