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

### Running RabbitMQ

The project uses RabbitMQ for eventing. A `rabbitmq` service has been added to `docker-compose.yml` so you can start it together with the other infrastructure services.

1. Start RabbitMQ with Docker Compose (recommended):

```bash
docker compose up -d rabbitmq
```

Or start all infrastructure services (MySQL, MongoDB, Redis, RabbitMQ):

```bash
docker compose up -d
```

2. Quick alternative: run RabbitMQ with Docker (includes management UI on port 15672):

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

After either option:

- AMQP endpoint: `amqp://localhost:5672`
- Management UI: `http://localhost:15672/` (default user/password: `guest` / `guest` when used locally)

3. Verify connection

```bash
# check container is running
```

# open management UI in a browser: http://localhost:15672/

````

If your services expect a specific exchange/queue configuration the application will typically declare those bindings on startup; otherwise create them in the management UI or via a startup script.

**Get User by Email (Cached):**

```bash
GET http://localhost:8081/rest/userByEmail?email=john@example.com
````

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
- Maven 3.8+ (optional — the project includes the Maven wrapper so a system Maven install is not required)
- Docker Desktop (Windows) / Docker & Docker Compose (for running infrastructure services). On Windows you must start Docker Desktop before running `docker compose` so the Docker daemon is available.

### Step 1: Start Infrastructure Services

Before running Docker Compose on Windows make sure Docker Desktop is running (open the Docker Desktop app and wait until it shows "Running").

Run Docker Compose to start MySQL, MongoDB, and Redis:

```bash
docker compose up -d
```

**Services Started:**

- MySQL (Port 3306)
- MongoDB (Port 27017)
- Redis (Port 6379)
- RabbitMQ (Port 5672) - _if configured in `docker-compose.yml` or started separately_

Note: If you use the older `docker-compose` binary the command `docker-compose up -d` still works, but Compose V2 (`docker compose`) ignores the top-level `version:` field in the compose file and will log a warning — this is expected.

### Running RabbitMQ

The project uses RabbitMQ for eventing. RabbitMQ is not included by default in the existing `docker-compose.yml` in this repository, so you can either add it to your compose file or run it separately.

1. Quick: run RabbitMQ with Docker (includes management UI on port 15672):

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

After this command:

- AMQP endpoint: `amqp://localhost:5672`
- Management UI: `http://localhost:15672/` (default user/password: `guest` / `guest` when used locally)

2. Or add this service to your `docker-compose.yml` (example snippet):

```yaml
rabbitmq:
  image: rabbitmq:3-management
  container_name: rabbitmq
  ports:
    - "5672:5672"
    - "15672:15672"
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
```

Then run:

```bash
docker compose up -d rabbitmq
```

3. Verify connection

```bash
# check container is running
docker ps --filter "name=rabbitmq"
# open management UI in a browser: http://localhost:15672/
```

If your services expect a specific exchange/queue configuration the application will typically declare those bindings on startup; otherwise create them in the management UI or via a startup script.

### Step 2: Build the Project

From the root directory you can either use your system `mvn` (if installed) or the included Maven wrapper. The wrapper is recommended because it ensures a consistent Maven version and doesn't require installing Maven system-wide.

Using the wrapper (Windows PowerShell):

```powershell
.\mvnw.cmd clean install
```

Or using system Maven:

```bash
mvn clean install
```

This builds all modules: `user`, `notification`, and `log-service`.

### Step 3: Run Individual Services

#### Option A: Using Maven / Maven Wrapper

On Windows use the Maven wrapper included in the repo (avoids needing a system Maven install). From the project root:

```powershell
# User service
.\user\mvnw.cmd spring-boot:run

# Notification service
.\notification\mvnw.cmd spring-boot:run

# Log service
.\log-service\mvnw.cmd spring-boot:run
```

If you have system Maven and prefer it, replace the wrapper commands with `mvn spring-boot:run` executed inside each module directory.

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

If you used `docker compose up -d` and saw errors earlier, note that those were typically caused by Docker Desktop not running; starting Docker Desktop resolved the named-pipe/daemon connection error on Windows.

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
