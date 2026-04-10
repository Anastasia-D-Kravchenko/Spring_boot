# ⚡ EventFlow — AI-Powered Event Management Platform

A full-stack **Spring Boot 3** application for managing events with three user roles, an AI assistant powered by **Claude (Anthropic)**, and a clean dark editorial UI.

---

## 🏗️ Architecture Overview

```
eventflow/
├── src/main/java/com/eventflow/
│   ├── EventFlowApplication.java       # @SpringBootApplication entry point
│   ├── aspect/
│   │   └── LoggingAspect.java          # @Around AOP — logs all service calls
│   ├── config/
│   │   ├── AppConfig.java              # WebClient (Anthropic), ObjectMapper beans
│   │   ├── AnthropicProperties.java    # @ConfigurationProperties for AI config
│   │   ├── EventFlowProperties.java    # @ConfigurationProperties for app config
│   │   └── DataInitializer.java        # CommandLineRunner — seeds admin + sample data
│   ├── controller/
│   │   ├── PublicController.java        # / , /events, /events/{id}, registration
│   │   ├── AuthController.java          # /auth/login, /auth/register
│   │   ├── AdminController.java         # /admin/** — ROLE_ADMIN only
│   │   ├── ManageController.java        # /manage/** — ROLE_DATA_ADDER + ADMIN
│   │   └── GlobalExceptionHandler.java  # @ControllerAdvice error handling
│   ├── dto/
│   │   └── EventFlowDtos.java           # All DTOs (EventCreate, UserRegister, AiChat…)
│   ├── model/
│   │   ├── User.java                    # @Entity with Role enum (USER/DATA_ADDER/ADMIN)
│   │   ├── Event.java                   # @Entity with Category, Status enums
│   │   ├── Registration.java            # @Entity — join User ↔ Event
│   │   └── AiConversation.java          # @Entity — chat history log
│   ├── repository/
│   │   ├── UserRepository.java          # JpaRepository + derived + @Query methods
│   │   ├── EventRepository.java         # Upcoming, search, date-range, category stats
│   │   ├── RegistrationRepository.java  # Confirmed registrations per user/event
│   │   └── AiConversationRepository.java
│   ├── security/
│   │   ├── SecurityConfig.java          # Spring Security filter chain + roles
│   │   └── SecurityUtils.java           # Helper to get current authenticated user
│   └── service/
│       ├── UserService.java             # User CRUD, promotion, profile management
│       ├── EventService.java            # Event CRUD, search, stats
│       ├── RegistrationService.java     # Register/cancel for events
│       ├── AnthropicService.java        # HTTP call to claude API → parse JSON
│       └── AiCommandService.java        # Orchestrates AI: parse intent → execute action
└── src/main/resources/
    ├── application.yaml                 # H2 dev config + AI config
    ├── application-prod.yaml            # PostgreSQL prod config
    ├── static/css/main.css              # Full CSS (dark editorial theme)
    └── templates/                       # Thymeleaf templates
        ├── index.html                   # Public home
        ├── events/list.html             # Events browse + search + filter
        ├── events/detail.html           # Event detail + register button
        ├── auth/{login,register}.html   # Auth pages
        ├── user/dashboard.html          # User: my registrations
        ├── admin/{dashboard,users,events,new-data-adder}.html
        └── manage/{dashboard,events,event-form,ai-chat,registrations}.html
```

---

## 🚀 Quick Start

### Prerequisites
- **Java 21** (OpenJDK or Temurin)
- **Maven 3.8+**
- **Anthropic API key** — get one at https://console.anthropic.com

### 1. Clone / open the project

```bash
# If you have the zip:
unzip eventflow.zip && cd eventflow

# Or copy to your workspace
```

### 2. Set your API key

```bash
export ANTHROPIC_API_KEY=sk-ant-your-key-here
```

Or add it to `application.yaml`:
```yaml
anthropic:
  api:
    key: sk-ant-your-key-here
```

### 3. Build and run

```bash
mvn spring-boot:run
```

The app starts on **http://localhost:8080**

---

## 🔑 Demo Credentials

| Role | Email | Password | Access |
|------|-------|----------|--------|
| **Admin** | admin@eventflow.com | Admin@1234 | Full admin panel, user management |
| **Data Adder** | manager@eventflow.com | Manager@1234 | Event management + AI assistant |
| **User** | user@eventflow.com | User@1234 | Browse and register for events |

---

## 📱 Pages & Features

### Public (no login required)
| URL | Description |
|-----|-------------|
| `/` | Home page with featured events and category grid |
| `/events` | Browse all events with search, category filter, pagination |
| `/events/{id}` | Event detail with registration button |
| `/auth/login` | Login page |
| `/auth/register` | Self-registration (creates USER role) |

### User Dashboard (any logged-in user)
| URL | Description |
|-----|-------------|
| `/dashboard` | Routes to correct dashboard by role |
| `/user/dashboard` | My registrations, upcoming events, profile |
| `/register-event/{id}` | POST — register for an event |
| `/cancel-event/{id}` | POST — cancel registration |

### Data Adder Panel (ROLE_DATA_ADDER + ROLE_ADMIN)
| URL | Description |
|-----|-------------|
| `/manage/dashboard` | Overview: my events, AI history |
| `/manage/events` | My events list with edit/delete |
| `/manage/events/new` | Manual event creation form |
| `/manage/events/{id}/edit` | Edit event |
| `/manage/events/{id}/registrations` | View attendees list |
| `/manage/ai` | **AI Chat interface** |
| `/manage/ai/chat` | POST API — send message to Claude |

### Admin Panel (ROLE_ADMIN only)
| URL | Description |
|-----|-------------|
| `/admin/dashboard` | System stats: users, events, registrations, category chart |
| `/admin/users` | Full user table with promote/demote/disable/delete |
| `/admin/data-adders/new` | Create a new Data Adder account |
| `/admin/events` | All events with cancel/delete controls |

---

## 🤖 AI Assistant — How It Works

The AI assistant (at `/manage/ai`) allows Data Adders to manage events using plain English.

### Flow
```
User types message
       ↓
AiCommandService.handleMessage()
       ↓
AnthropicService.chat() — calls Claude API with:
   • System prompt (tells Claude to return JSON with action/message/data)
   • User's message
   • Context (current date, user's event list with IDs)
       ↓
Claude returns structured JSON:
  { "action": "CREATE", "message": "...", "data": { ... } }
       ↓
AiCommandService executes the action:
  CREATE  → EventService.createEventFromMap()
  DELETE  → EventService.deleteEvent()
  UPDATE  → EventService partial update
  LIST    → EventService.findUpcoming() / findByCategory()
  SEARCH  → EventService.searchEvents()
  STATS   → counts from repositories
       ↓
Response sent back as JSON → JavaScript renders in chat UI
Conversation saved to ai_conversations table
```

### Example AI Commands
```
"Create a jazz concert next Saturday at 8pm at the Blue Note club, max 120 people, tickets €25"
"Show me all my tech events"
"Delete the yoga retreat event"
"How many registrations do my events have?"
"Search for events in Warsaw"
"Create a free community barbecue on Sunday at noon in the park, space for 50"
"List all events this month"
```

---

## 🔒 Security

Implemented with **Spring Security 6**:

| Path Pattern | Required Role |
|---|---|
| `/`, `/events/**`, `/auth/**` | Public |
| `/register-event/**`, `/dashboard`, `/user/**` | Any authenticated user |
| `/manage/**`, `/ai/**` | ROLE_DATA_ADDER or ROLE_ADMIN |
| `/admin/**` | ROLE_ADMIN only |

- Passwords hashed with **BCrypt**
- Remember-me support (7 days)
- CSRF protection enabled (disabled for `/api/**` and H2 console)
- Method-level security with `@PreAuthorize`

---

## 🗄️ Database

**Development**: H2 in-memory (auto-configured)
- Console at: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:eventflowdb`
- Username: `sa` | Password: _(empty)_

**Production**: PostgreSQL
```bash
# Activate prod profile:
java -jar eventflow.jar --spring.profiles.active=prod \
  --DB_URL=jdbc:postgresql://localhost:5432/eventflow \
  --DB_USER=eventflow \
  --DB_PASS=yourpassword \
  --ANTHROPIC_API_KEY=sk-ant-...
```

### Entity Relationships
```
User ──< Registration >── Event
User ──< AiConversation
Event >── User (createdBy)
```

---

## 🧪 Running Tests

```bash
mvn test
```

Tests cover:
- Context loads
- Public routes accessible without auth
- Protected routes redirect unauthenticated users
- User registration success
- Duplicate email rejection
- Password mismatch rejection

---

## 📦 Key Spring Boot Concepts Used

| Concept | Where Used |
|---------|-----------|
| `@SpringBootApplication` | `EventFlowApplication` |
| `IoC / Constructor DI` | All services and controllers |
| `@ConfigurationProperties` | `AnthropicProperties`, `EventFlowProperties` |
| `@Component`, `@Service`, `@Repository` | Full layered architecture |
| `@Entity`, `@OneToMany`, `@ManyToOne` | User, Event, Registration |
| `JpaRepository` + derived queries | All repositories |
| `@Query` (JPQL) | Search, upcoming, date-range queries |
| `@Transactional` | All write operations in services |
| `@Aspect` + `@Around` | `LoggingAspect` on all service methods |
| `@ControllerAdvice` | `GlobalExceptionHandler` |
| `@PreAuthorize` | Admin/DataAdder method-level security |
| `@Profile` | `application-prod.yaml` for PostgreSQL |
| `CommandLineRunner` | `DataInitializer` seed data |
| `WebClient` (WebFlux) | Anthropic API calls |
| Spring Security 6 | Auth, role-based access, BCrypt |
| Thymeleaf | All server-rendered HTML templates |

---

## 🎨 Changing the AI Model

In `application.yaml`:
```yaml
anthropic:
  api:
    model: claude-opus-4-20250514   # most capable
    # model: claude-sonnet-4-20250514  # faster, cheaper
    max-tokens: 2048
```

## ✏️ Customizing the AI System Prompt

In `application.yaml`, edit the `eventflow.ai.system-prompt` multiline string to change how the AI behaves and what actions it supports.

---

## 🤝 Adding a New Role

1. Add the value to `User.Role` enum
2. Add a `@PreAuthorize` or `requestMatchers` rule in `SecurityConfig`
3. Handle routing in `PublicController.dashboard()`
4. Create templates in `src/main/resources/templates/`
