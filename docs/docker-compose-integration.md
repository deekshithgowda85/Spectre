# Spectra Docker Compose Integration

## Purpose

The repository can be started from `D:\Spectra` with:

```powershell
docker compose up --build
```

The root `compose.yaml` is the local integration environment. It runs shared infrastructure, all Spring Boot services, the API gateway, and the Next.js dashboard on one Docker network.

## Services

| Service             | Container port |     Host port | Role                                  |
| ------------------- | -------------: | ------------: | ------------------------------------- |
| `postgres`          |           5432 |          5432 | Shared local PostgreSQL database      |
| `kafka`             |           9092 |          9092 | Single-node KRaft Kafka broker        |
| `service-registry`  | Spring default | not published | Registry application process          |
| `auth-service`      |           8081 |          8081 | Authentication and JWT operations     |
| `incident-service`  |           8082 |          8082 | Incident management                   |
| `dashboard-service` |           8083 |          8083 | Dashboard backend data operations     |
| `ping-service`      |           8084 |          8084 | Service availability checks           |
| `anomaly-service`   |           8085 |          8085 | Anomaly detection                     |
| `scraper-service`   |           8086 |          8086 | Metrics scraping                      |
| `api-gateway`       |           8080 |          8080 | Public backend entrypoint and routing |
| `dashboard`         |           3000 |          3000 | Next.js web application               |

Open the application at `http://localhost:3000`. Backend requests go through `http://localhost:8080`.

## How The Integration Works

### 1. Shared infrastructure

Postgres is created from `postgres:16-alpine` with:

- Database: `spectra`
- User: `spectra`
- Password: `spectra`
- Persistent volume: `spectra_postgres-data`

Kafka runs as a single-node KRaft broker from `apache/kafka:3.9.0`. ZooKeeper is not required. The broker is reachable inside Docker as `kafka:9092` and from the host as `localhost:9092`.

Both infrastructure services have health checks. Application services wait for healthy Postgres and Kafka before starting.

### 2. Backend image build

`Dockerfile.backend` is shared by every Spring Boot service. Compose passes the service directory as a build argument:

```yaml
build:
  context: .
  dockerfile: Dockerfile.backend
  args:
    SERVICE_DIR: auth-service
```

The image build then:

1. Uses Maven with Eclipse Temurin JDK 21.
2. Copies only that service's `pom.xml`.
3. Copies only that service's `src` directory.
4. Runs `mvn clean package -DskipTests`.
5. Starts a smaller Eclipse Temurin 21 JRE image.
6. Copies the generated executable Spring Boot JAR to `/app/service.jar`.
7. Runs `java -jar /app/service.jar`.

This avoids maintaining eight nearly identical Dockerfiles.

### 3. Frontend image build

`dashboard/Dockerfile` uses three stages:

1. `dependencies`: installs npm dependencies.
2. `build`: runs `npm run build` with `NEXT_PUBLIC_API_URL=http://localhost:8080`.
3. Runtime: starts the Next standalone server with Node 22 Alpine.

`dashboard/next.config.ts` enables `output: "standalone"`, which makes the runtime image smaller and self-contained.

### 4. Internal service networking

Containers must not use `localhost` to call another container. Compose service names provide DNS records on the default network:

- Gateway to auth: `http://auth-service:8081`
- Gateway to incidents: `http://incident-service:8082`
- Gateway to dashboard backend: `http://dashboard-service:8083`
- Gateway to ping: `http://ping-service:8084`
- Gateway to anomaly: `http://anomaly-service:8085`
- Gateway to scraper: `http://scraper-service:8086`
- Backend services to Postgres: `postgresql://postgres:5432/spectra`
- Backend services to Kafka: `kafka:9092`
- Anomaly to scraper: `http://scraper-service:8086`
- Anomaly to ping: `http://ping-service:8084`

The browser-facing frontend still uses `http://localhost:8080`, because the browser runs outside the Docker network.

### 5. Gateway routing

The existing gateway properties define routes under `/api`:

- `/api/auth/**` -> auth service
- `/api/incidents/**` -> incident service
- `/api/dashboard/**` -> dashboard service
- `/api/anomalies/**` -> anomaly service
- `/api/ping/**` -> ping service
- `/api/scraper/**` -> scraper service

The Compose environment overrides the route target URLs with Docker service names.

## Environment Variables

Local Compose has safe development defaults for:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `DATABASE_SSLMODE`
- `JWT_SECRET`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`

The JWT secret can be overridden from the shell or `.env` file:

```powershell
$env:JWT_SECRET = "use-a-long-local-secret-value"
docker compose up --build
```

For production or external Postgres, replace the local database values and do not use the development JWT secret.

## Disk Usage Explanation

The Docker Desktop project view showed approximately `30.45 GB`. That is Docker's overall storage usage, not the size of the Spectra database.

The measured Docker storage breakdown was:

| Category      |     Size | Reclaimable |
| ------------- | -------: | ----------: |
| Images        | 28.32 GB |    21.74 GB |
| Build cache   | 19.52 GB |    19.52 GB |
| Containers    |  6.93 MB |     6.93 MB |
| Local volumes |   245 MB |    100.5 MB |

The largest unrelated images were:

- `gitnexus-runtime`: 4.01 GB
- `agile-gitnexus-runtime`: 4.01 GB

The Spectra images are approximately:

- Eight Spring Boot images: about 524-621 MB each
- Dashboard image: about 333 MB
- Postgres image: about 419 MB

The Compose volumes are small at present:

- `spectra_postgres-data`: about 48 MB
- `spectra_kafka-data`: approximately 0 B

The main cause of the large number is accumulated Docker image/build cache, not the Compose database.

## Safe Cleanup

To remove stopped Compose containers and this project's unused network while preserving database volumes:

```powershell
docker compose down
```

To also remove this project's Postgres and Kafka data volumes, which deletes local data:

```powershell
docker compose down -v
```

To remove unused build cache:

```powershell
docker builder prune
```

To remove all unused images, stopped containers, networks, and build cache:

```powershell
docker system prune -a
```

Do not use `--volumes` unless you intentionally want Docker to remove unused database volumes as well.

## Validation Commands

Validate Compose without starting containers:

```powershell
docker compose config
```

Build all images:

```powershell
docker compose build
```

Start in the background:

```powershell
docker compose up -d
```

View service state:

```powershell
docker compose ps -a
```

View logs for one service:

```powershell
docker compose logs -f api-gateway
```

Check Docker storage:

```powershell
docker system df -v
```

## Current Runtime Caveat

The application images built successfully. In the current Docker Desktop run, Postgres and Kafka were pulled and the application containers were created, but several services exited afterward. Exit code `137` usually means the container was killed by the runtime, commonly because Docker Desktop memory was exhausted. Exit code `1` indicates an application startup failure and should be diagnosed with:

```powershell
docker compose logs auth-service incident-service anomaly-service dashboard-service
```

Allocate more memory to Docker Desktop, then retry:

```powershell
docker compose down
docker compose up -d
```

The Compose configuration itself validates successfully with `docker compose config --quiet`.
