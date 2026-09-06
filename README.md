# 🤖 Production-Ready Telegram AI Assistant Bot

A high-performance, modular, and scalable **Telegram AI Assistant Bot** built with **Java 21**, **Spring Boot 3.4**, **PostgreSQL**, **Redis**, **Flyway**, and pluggable **AI Provider** architecture (OpenAI, DeepSeek, Groq, Ollama, etc.).

---

## 🌟 Key Features

### 1. 💬 Intelligent Conversational AI
- **Context-Aware Memory:** Sliding window of recent messages + automated conversation summarization.
- **Language Detection & Multi-Language Support:** Seamlessly communicates in **Khmer (ភាសាខ្មែរ)**, **English**, **Chinese**, **Thai**, and **Vietnamese**.
- **Instant Memory Reset:** Users can clear history anytime with `/clear`.

### 2. ⚡ Multi-Modal AI Toolkit
- **📝 Text Summarizer:** `/summary <text>` returns key bullet points.
- **🌐 Multilingual Translator:** `/translate` with interactive target language keyboard.
- **💻 Coding Assistant:** Elite software engineer assistant for Java, Spring Boot, React, Python, SQL, C#, and HTML/CSS.
- **📄 Document Assistant:** Upload **PDF** or **DOCX** files to extract text (via Apache PDFBox & Apache POI) and perform Q&A or generate instant summaries.

### 3. ⏰ Smart Reminder & Task Scheduler
- **Natural Language Parsing:** Automatically parses prompts like `"Remind me tomorrow at 8 AM to study Java"` or `"in 30 mins to take a break"`.
- **Background Cron Engine:** Spring `@Scheduled` worker dispatches Telegram alert notifications precisely when due.
- **Task Management:** View `/tasks` and cancel reminders with inline buttons.

### 4. ⚙️ Interactive Telegram UI
- Modern inline keyboards with callback buttons for `/start`, `/menu`, `/settings`, `/tasks`, and `/help`.
- Friendly UX: Sends `"⏳ Bro, I'm thinking and processing your request..."` and edits the message once AI completes.

### 5. 📊 Admin Web Dashboard & Real-Time Analytics
- **Live Metrics:** Total users, active users, messages today, AI requests today, token count, and USD cost.
- **Visual Charts:** Usage history & Model cost distribution using Chart.js.
- **User Management:** Search, block/unblock, and upgrade users between `FREE`, `PREMIUM`, and `UNLIMITED`.
- **Queued Safe Broadcast:** Send announcements to all or filtered users with rate limiting to prevent Telegram 429 throttling.

### 6. 🔒 Enterprise Security & Scalability
- **Flyway Database Migrations:** Fully versioned schema migrations.
- **Redis Rate Limiting:** Per-user request throttling (15 req/min).
- **Daily Quotas:** Configurable Free (20 req/day) vs Premium (200 req/day) limits.
- **Async Execution:** Non-blocking thread pools (`@Async`) for instant webhook/polling responses.
- **Flexible Modes:** Supports both **Polling** (for local dev) and **Webhook** (`POST /api/telegram/webhook` for production).

---

## 🏗️ Architecture

```
[ Telegram Users ] 
       │
       ▼ (Webhook / Polling)
[ Spring Boot 3.4 App ] ────────► [ Redis ] (Rate Limiting, Callback States, Caching)
       │                             │
       ├─► [ AI Provider Layer ] ────► OpenAI / DeepSeek / Groq / Ollama
       │        (Chat, Summary, Translate, Coding, Doc Q&A)
       │
       ├─► [ Document Engine ] ──────► Apache PDFBox & Apache POI (PDF, DOCX, TXT)
       │
       ├─► [ Task Scheduler ] ───────► @Scheduled Background Dispatcher -> Telegram
       │
       ├─► [ PostgreSQL + Flyway ] ──► Users, Conversations, Messages, Tasks, Usage, Costs
       │
       └─► [ Admin Web Dashboard ] ──► Chart.js, REST API, User Management, Broadcast Queue
```

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java 21** or later
- **Maven 3.8+**
- **Docker & Docker Compose** (optional, recommended for production)
- Telegram Bot Token (from [@BotFather](https://t.me/BotFather))
- OpenAI API Key (or OpenAI-compatible provider key)

---

### 2. Environment Configuration

Copy `.env.example` to `.env` or set environment variables:

```bash
cp .env.example .env
```

Key variables:
| Variable | Description | Default |
|---|---|---|
| `TELEGRAM_BOT_TOKEN` | Bot API Token from @BotFather | *(Required)* |
| `TELEGRAM_BOT_USERNAME` | Telegram handle of your bot | `MyAiAssistantBot` |
| `TELEGRAM_MODE` | `polling` (dev) or `webhook` (prod) | `polling` |
| `AI_API_KEY` | OpenAI or compatible API key | *(Required for live AI)* |
| `AI_MODEL` | AI Model name | `gpt-4o-mini` |
| `DATABASE_URL` | PostgreSQL JDBC Connection URL | `jdbc:postgresql://localhost:5432/telegram_bot_db` |
| `REDIS_URL` | Redis Connection URI | `redis://localhost:6379` |
| `ADMIN_USERNAME` | Admin Dashboard Username | `admin` |
| `ADMIN_PASSWORD` | Admin Dashboard Password | `admin123456` |

---

### 3. Run Locally with Maven

1. Start PostgreSQL and Redis (e.g. with Docker):
   ```bash
   docker compose up -d postgres redis
   ```

2. Build and run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```

3. Open Admin Web Dashboard:
   ```
   http://localhost:8080/admin/index.html
   ```
   *(Credentials: `admin` / `admin123456`)*

---

### 4. Run with Docker Compose

To launch the full stack (App + PostgreSQL + Redis) in one command:

```bash
docker compose up --build -d
```

Check running logs:
```bash
docker compose logs -f app
```

---

## 📱 Telegram Commands Reference

| Command | Description |
|---|---|
| `/start` | Welcome message, registers user, opens interactive menu |
| `/menu` | Displays the rich inline feature menu |
| `/help` | Shows the comprehensive help guide |
| `/chat` | Switches to conversational AI chat mode |
| `/clear` | Resets conversation memory and context |
| `/summary <text>` | Generates a structured summary of the text |
| `/translate <text>` | Translates text to the chosen language |
| `/task <reminder>` | Creates a background reminder (e.g. `/task Remind me tomorrow at 8 AM to study`) |
| `/tasks` | Lists pending reminders with one-tap delete buttons |
| `/settings` | Configure language, conversation memory, and notifications |
| `/admin` | Displays admin dashboard information |

---

## 🛠️ REST API Endpoints

### Telegram Webhook
- `POST /api/telegram/webhook`: Incoming Telegram updates (secured with `X-Telegram-Bot-Api-Secret-Token`).
- `GET /api/telegram/health`: Bot health status and active mode.

### Admin REST API (Secured via HTTP Basic)
- `GET /api/admin/stats`: Real-time dashboard statistics and charts data.
- `GET /api/admin/users`: Paginated list of registered users.
- `PUT /api/admin/users/{id}/status`: Toggle user status (`ACTIVE` / `BLOCKED`).
- `PUT /api/admin/users/{id}/tier`: Update user plan (`FREE`, `PREMIUM`, `UNLIMITED`).
- `POST /api/admin/broadcast`: Asynchronously broadcast announcement to users.

---

## 🧪 Running Tests

Execute the unit and integration test suite:

```bash
mvn clean test
```

---

## 📄 License
MIT License. Built for production deployment.
