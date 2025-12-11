# Feature Flag Manager

A full-stack feature flag management system with targeting rules, percentage rollouts, and real-time analytics.

**[Live Demo](https://glorious-consideration-production.up.railway.app/)** | **[GitHub](https://github.com/Kausha3/featureflag-manager)**

## What it does

Control feature releases without deploying code. Turn features on/off instantly, roll out to a percentage of users, or target specific segments like beta testers or premium customers. Track how flags perform with built-in analytics.

## Tech Stack

**Backend**
- Java 21 + Spring Boot 3.2
- PostgreSQL for flag storage
- Redis for fast lookups
- Flyway migrations

**Frontend**
- React 18 with TypeScript
- Tailwind CSS
- Recharts for analytics

## Features

- **Instant Toggles**: Enable/disable features without deploys
- **Targeting Rules**: Target by user ID, email domain, or country
- **Percentage Rollouts**: Gradual rollouts with consistent user assignment
- **Real-time Analytics**: Track evaluation counts over time
- **Flag Tester**: Test any user context before going live
- **Redis Caching**: Sub-millisecond flag lookups

## Architecture

```
┌─────────────────┐     ┌─────────────────┐
│  React Frontend │────▶│  Spring Boot    │
│  (TypeScript)   │     │  Backend API    │
└─────────────────┘     └────────┬────────┘
                                 │
                    ┌────────────┼────────────┐
                    ▼                         ▼
            ┌─────────────┐           ┌─────────────┐
            │ PostgreSQL  │           │    Redis    │
            │ (Flags/Rules)│           │  (Cache)    │
            └─────────────┘           └─────────────┘
```

## Getting Started

### Quick Start with Docker

```bash
docker-compose up -d
```

### Development Setup

```bash
# Start databases
docker-compose -f docker-compose.dev.yml up -d

# Run backend
cd backend
./mvnw spring-boot:run

# Run frontend
cd frontend
npm install
npm run dev
```

## API Reference

### Flags

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/flags` | List all flags |
| POST | `/api/flags` | Create a flag |
| PUT | `/api/flags/{id}` | Update a flag |
| PATCH | `/api/flags/{id}/toggle` | Toggle on/off |
| DELETE | `/api/flags/{id}` | Delete a flag |

### Rules

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/flags/{id}/rules` | Add targeting rule |
| DELETE | `/api/flags/rules/{id}` | Remove rule |

### Evaluation

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/flags/evaluate` | Evaluate flags for user |

### Create a Flag

```bash
curl -X POST http://localhost:8080/api/flags \
  -H "Content-Type: application/json" \
  -d '{
    "name": "new_checkout",
    "description": "New checkout flow",
    "enabled": true,
    "rolloutPercentage": 25
  }'
```

### Evaluate Flags

```bash
curl -X POST http://localhost:8080/api/flags/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "userEmail": "dev@company.com",
    "country": "US"
  }'
```

Response:
```json
{
  "flags": {
    "new_checkout": true,
    "dark_mode": false
  },
  "details": {
    "new_checkout": {
      "result": true,
      "reason": "RULE_MATCH",
      "explanation": "Matched rule: EMAIL_DOMAIN = @company.com"
    }
  }
}
```

## How Evaluation Works

1. Check if flag is globally enabled
2. Check targeting rules in priority order
3. Fall back to percentage rollout (hash-based for consistency)

The same user always gets the same result for the same flag - no randomness between sessions.

## Rule Types

| Type | Description | Example |
|------|-------------|---------|
| USER_ID | Exact user match | `user123` |
| EMAIL_DOMAIN | Email suffix | `@company.com` |
| EMAIL_EXACT | Exact email | `admin@company.com` |
| COUNTRY | Country code | `US` |

## Project Structure

```
featureflag-manager/
├── backend/
│   └── src/main/java/com/featureflag/
│       ├── controller/
│       ├── service/
│       ├── entity/
│       └── repository/
├── frontend/
│   └── src/
│       ├── components/
│       └── api/
└── docker-compose.yml
```

## License

MIT
