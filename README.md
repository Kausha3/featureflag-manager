# FeatureFlag Manager

A full-stack feature flag management system with targeting rules, percentage rollouts, and real-time analytics.

## Features

- **Flag Management**: Create, update, enable/disable feature flags
- **Targeting Rules**: Target specific users by ID, email domain, or country
- **Percentage Rollouts**: Gradual rollouts with deterministic hashing
- **Real-time Analytics**: Track flag evaluations with time-series data
- **Flag Tester**: Test flag evaluation for any user context
- **Redis Caching**: Fast flag lookups with automatic cache refresh

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

## Tech Stack

### Backend
- Java 21, Spring Boot 3.2
- PostgreSQL 16 with Flyway migrations
- Redis with Redisson for caching
- JPA/Hibernate

### Frontend
- React 18 with TypeScript
- Tailwind CSS
- Recharts for analytics
- React Router, Axios

## Quick Start

### Development

```bash
# Start PostgreSQL and Redis
docker-compose -f docker-compose.dev.yml up -d

# Start backend
cd backend
./mvnw spring-boot:run

# Start frontend (in another terminal)
cd frontend
npm install
npm run dev
```

### Production

```bash
docker-compose up --build -d
```

## API Endpoints

### Flags

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/flags` | Create flag |
| GET | `/api/flags` | List all flags |
| GET | `/api/flags/{id}` | Get flag details |
| PUT | `/api/flags/{id}` | Update flag |
| PATCH | `/api/flags/{id}/toggle` | Toggle flag on/off |
| DELETE | `/api/flags/{id}` | Delete flag |

### Rules

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/flags/{id}/rules` | Add targeting rule |
| GET | `/api/flags/{id}/rules` | List rules |
| PATCH | `/api/flags/rules/{id}/toggle` | Toggle rule |
| DELETE | `/api/flags/rules/{id}` | Delete rule |

### Evaluation

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/flags/evaluate` | Evaluate all flags for user |
| GET | `/api/flags/evaluate?userId=X` | Quick evaluation |

### Analytics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/flags/{id}/analytics` | Get flag analytics |

## Usage Examples

### Create a Feature Flag

```bash
curl -X POST http://localhost:8080/api/flags \
  -H "Content-Type: application/json" \
  -d '{
    "name": "new_checkout",
    "description": "New checkout flow with one-click purchase",
    "enabled": true,
    "rolloutPercentage": 25
  }'
```

### Add Targeting Rule

```bash
curl -X POST http://localhost:8080/api/flags/{flagId}/rules \
  -H "Content-Type: application/json" \
  -d '{
    "ruleType": "EMAIL_DOMAIN",
    "ruleValue": "@company.com",
    "enabled": true,
    "priority": 10
  }'
```

### Evaluate Flags

```bash
curl -X POST http://localhost:8080/api/flags/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "userEmail": "test@company.com",
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

## Evaluation Logic

```
1. If flag is globally disabled → return false

2. Check targeting rules (in priority order):
   - USER_ID: exact match
   - EMAIL_DOMAIN: suffix match (e.g., @company.com)
   - EMAIL_EXACT: exact email match
   - COUNTRY: exact country code match
   → If any rule matches → return that rule's result

3. Fall back to percentage rollout:
   - hash = abs((flagName + ":" + userId).hashCode())
   - bucket = hash % 100
   - return bucket < rolloutPercentage
```

The hash-based rollout ensures:
- Same user always gets same result for same flag
- Consistent experience across sessions
- No database lookup needed for rollout calculation

## Rule Types

| Type | Description | Example |
|------|-------------|---------|
| USER_ID | Exact user ID match | `user123` |
| EMAIL_DOMAIN | Email suffix match | `@google.com` |
| EMAIL_EXACT | Exact email match | `admin@company.com` |
| COUNTRY | Country code match | `US` |

## License

MIT
