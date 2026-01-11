# Fundoland - Microservices Project Guide

## Project Overview

Fundoland is a Spring Boot microservices-based application designed as a distributed system. It consists of three main services that communicate asynchronously through RabbitMQ for event-driven architecture.

**Architecture Stack:**

- **Framework:** Spring Boot 3.5.5
- **Language:** Java 21
- **Build Tool:** Maven
- **Messaging:** RabbitMQ
- **Databases:** MySQL, MongoDB, Redis
- **Containerization:** Docker Compose

---

## Services Overview

### 1. **User Service** (User Registration & Management)

**Port:** `8081`

#### Capabilities

- User registration and management
- Redis-based caching for improved performance
- Event publishing for user-related activities
- Soft delete functionality
- Cache invalidation on data updates

#### Database

- **Type:** MySQL
- **Connection:** `jdbc:mysql://localhost:3306/fundoland_db`
- **Credentials:** `fundoland_user` / `userpassword`

#### Key Features

- Email-based user lookup with caching
- Automatic cache eviction on updates
- Publishes `UserCreatedEvent` and `UserCreationFailedEvent` to RabbitMQ
- Supports Redis caching for frequently accessed user data

#### APIs

| Method | Endpoint              | Parameters                                            | Description                                    |
| ------ | --------------------- | ----------------------------------------------------- | ---------------------------------------------- |
| POST   | `/rest/add`           | `name` (String), `email` (String)                     | Add a new user. Returns error if email exists. |
| GET    | `/rest/users`         | -                                                     | Retrieve all non-deleted users                 |
| GET    | `/rest/userByEmail`   | `email` (String)                                      | Get user by email (cached in Redis)            |
| POST   | `/rest/update`        | `id` (Integer), `name` (optional), `email` (optional) | Update user information                        |
| POST   | `/rest/delete`        | `id` (Integer)                                        | Soft delete a user by ID                       |
| GET    | `/rest/deleted-users` | -                                                     | Fetch all soft-deleted users                   |

#### Example Requests

**Add User:**

```bash
POST http://localhost:8081/rest/add?name=John%20Doe&email=john@example.com
```

**Get All Users:**

```bash
GET http://localhost:8081/rest/users
```

**Get User by Email (Cached):**

```bash
GET http://localhost:8081/rest/userByEmail?email=john@example.com
```

**Update User:**

```bash
POST http://localhost:8081/rest/update?id=1&name=Jane%20Doe&email=jane@example.com
```

**Delete User:**

```bash
POST http://localhost:8081/rest/delete?id=1
```

#### Configuration

File: `user/src/main/resources/application.properties`

- Server Port: `8081`
- MySQL Connection with JPA/Hibernate
- Redis Cache enabled
- RabbitMQ for async messaging
- Actuator monitoring enabled

---

### 2. **Notification Service** (Event Consumer)

**Port:** `8082`

#### Capabilities

- Consumes user-related events from RabbitMQ
- Handles user creation notifications
- Manages user creation failure notifications
- Asynchronous event processing

#### Message Queue Configuration

- **Exchange:** `user.exchange`
- **Queue:** `user.created.queue`
- **Routing Key:** `user.created`

#### Supported Events

1. **UserCreatedEvent** - Triggered when a new user is successfully registered
2. **UserCreationFailedEvent** - Triggered when user creation fails (e.g., email already exists)

#### No Direct APIs

The Notification Service doesn't expose REST endpoints. It operates as a background service consuming events from RabbitMQ queues.

#### Event Flow

```
User Service (POST /rest/add)
    ↓
Event Published to RabbitMQ
    ↓
Notification Service (Consumer)
    ↓
Process Event (Send Notification, Log, etc.)
```

#### Configuration

File: `notification/src/main/resources/application.properties`

- Server Port: `8082`
- RabbitMQ Host: `localhost:5672`
- Queue binding to `user.exchange` with routing key `user.created`

---

### 3. **Log Service** (Logging & Auditing)

**Port:** `8083` (inferred - not explicitly configured in properties)

#### Capabilities

- Store application logs and audit trails
- Query logs by service name
- MongoDB-based persistent storage
- Real-time timestamp recording

#### Database

- **Type:** MongoDB
- **Connection:** `mongodb://root:rootpassword@localhost:27017/logs_db?authSource=admin`
- **Database Name:** `logs_db`

#### APIs

| Method | Endpoint     | Parameters                                           | Description                             |
| ------ | ------------ | ---------------------------------------------------- | --------------------------------------- |
| POST   | `/logs/add`  | JSON Body: `{ "service": "user", "message": "..." }` | Add a new log entry                     |
| GET    | `/logs/read` | `service` (optional String)                          | Read all logs or filter by service name |

#### Example Requests

**Add Log Entry:**

```bash
POST http://localhost:8083/logs/add
Content-Type: application/json

{
  "service": "user",
  "message": "User created with ID 123"
}
```

**Read All Logs:**

```bash
GET http://localhost:8083/logs/read
```

**Read Logs by Service:**

```bash
GET http://localhost:8083/logs/read?service=user
```

#### Configuration

File: `log-service/src/main/resources/application.properties`

- MongoDB URI connection configured
- Database: `logs_db`

---

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker & Docker Compose (for running services)

### Step 1: Start Infrastructure Services

Run Docker Compose to start MySQL, MongoDB, and Redis:

```bash
docker-compose up -d
```

**Services Started:**

- MySQL (Port 3306)
- MongoDB (Port 27017)
- Redis (Port 6379)
- RabbitMQ (Port 5672) - _if configured in docker-compose_

### Step 2: Build the Project

From the root directory:

```bash
mvn clean install
```

This builds all modules: `user`, `notification`, and `log-service`.

### Step 3: Run Individual Services

#### Option A: Using Maven

**User Service:**

```bash
cd user
mvn spring-boot:run
```

**Notification Service:**

```bash
cd notification
mvn spring-boot:run
```

**Log Service:**

```bash
cd log-service
mvn spring-boot:run
```

#### Option B: Run JAR Files

After building:

```bash
java -jar user/target/user-0.0.1-SNAPSHOT.jar
java -jar notification/target/notification-service-0.0.1-SNAPSHOT.jar
java -jar log-service/target/log-service-0.0.1-SNAPSHOT.jar
```

#### Option C: From PowerShell (Windows)

```powershell
# Start User Service
cd user; mvn spring-boot:run

# In another terminal
cd notification; mvn spring-boot:run

# In another terminal
cd log-service; mvn spring-boot:run
```

### Step 4: Verify Services

Check health endpoints:

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

---

## Configuration Files

### Root POM

- **File:** `pom.xml`
- **Purpose:** Parent POM with dependency management for all modules
- **Java Version:** 21
- **Spring Boot Version:** 3.5.5

### User Service Configuration

- **POM:** `user/pom.xml`
- **Properties:** `user/src/main/resources/application.properties`
- **Port:** 8081
- **Database:** MySQL (fundoland_db)
- **Cache:** Redis

### Notification Service Configuration

- **POM:** `notification/pom.xml`
- **Properties:** `notification/src/main/resources/application.properties`
- **Port:** 8082
- **Purpose:** Event consumer from RabbitMQ

### Log Service Configuration

- **POM:** `log-service/pom.xml`
- **Properties:** `log-service/src/main/resources/application.properties`
- **Database:** MongoDB (logs_db)

### Docker Compose

- **File:** `docker-compose.yml`
- **Services:** MySQL, MongoDB, Redis

---

## Monitoring & Actuator Endpoints

The User Service exposes actuator endpoints for monitoring:

```
http://localhost:8081/actuator/health       - Health status
http://localhost:8081/actuator/info         - Application info
http://localhost:8081/actuator/metrics      - Application metrics
http://localhost:8081/actuator/beans        - Spring beans
http://localhost:8081/actuator/env          - Environment properties
http://localhost:8081/actuator/mappings     - Request mappings
```

---

## Event-Driven Architecture

### Message Flow

1. **User Creation:**

   ```
   POST /rest/add → User Service
   → Publishes UserCreatedEvent to user.exchange
   → Notification Service consumes event
   → Process notification/logging
   ```

2. **User Creation Failure:**
   ```
   POST /rest/add (duplicate email) → User Service
   → Publishes UserCreationFailedEvent to user.exchange
   → Notification Service consumes event
   → Handle failure (log, alert, etc.)
   ```

---

## Caching Strategy

### Redis Caching (User Service)

- **Cache Key:** `userByEmail` - Caches individual users by email
- **Cache Invalidation:** Automatically evicted when:
  - User is updated via `/rest/update`
  - User is deleted via `/rest/delete`
- **TTL:** Configurable via Spring Cache properties

### Cache Operations

| Operation         | Endpoint              | Cache Action            |
| ----------------- | --------------------- | ----------------------- |
| Add User          | POST /rest/add        | Cache Put (new user)    |
| Get User by Email | GET /rest/userByEmail | Cache Lookup / Populate |
| Update User       | POST /rest/update     | Cache Evict             |
| Delete User       | POST /rest/delete     | Cache Evict             |

---

## Database Schemas

### MySQL (User Service)

**Table: user**

- `id` (Integer, Primary Key)
- `name` (String)
- `email` (String, Unique)
- `deleted` (Boolean, Default: false)
- Auto-generated timestamps (JPA Auditing)

### MongoDB (Log Service)

**Collection: LogEntry**

- `_id` (ObjectId)
- `service` (String)
- `message` (String)
- `timestamp` (LocalDateTime)

---

## Troubleshooting

### Port Already in Use

If a port is already in use, modify the `application.properties`:

```properties
server.port=8084  # Change port
```

### Database Connection Issues

Verify Docker containers are running:

```bash
docker-compose ps
```

### RabbitMQ Connection

Ensure RabbitMQ is running and accessible at `localhost:5672`

### Redis Connection

Test Redis connection:

```bash
docker exec redis redis-cli ping
```

---

## Development Notes

- **Soft Deletes:** Users are soft-deleted (marked as deleted, not removed)
- **Async Processing:** Event-driven architecture reduces coupling between services
- **Caching:** Redis cache improves performance for repeated queries
- **Monitoring:** Spring Actuator provides real-time application metrics
- **Timeouts:** Request timeout set to 10 seconds

---

## Project Structure

```
fundoland/
├── user/                           # User Service
│   ├── src/main/java/...
│   ├── src/main/resources/
│   └── pom.xml
├── notification/                   # Notification Service
│   ├── src/main/java/...
│   ├── src/main/resources/
│   └── pom.xml
├── log-service/                    # Log Service
│   ├── src/main/java/...
│   ├── src/main/resources/
│   └── pom.xml
├── docker-compose.yml              # Infrastructure setup
├── pom.xml                         # Parent POM
└── guide.md                        # This file
```

---

## Quick Start Summary

```bash
# 1. Start infrastructure
docker-compose up -d

# 2. Build project
mvn clean install

# 3. Run services (in separate terminals)
cd user && mvn spring-boot:run
cd notification && mvn spring-boot:run
cd log-service && mvn spring-boot:run

# 4. Test User Service API
curl "http://localhost:8081/rest/add?name=John&email=john@example.com"
curl "http://localhost:8081/rest/users"
```

---

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [Redis Documentation](https://redis.io/documentation)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
