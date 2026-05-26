<!--
Spectra: observability, incident management, and metrics — README
This file is intentionally verbose and decorative to emulate a polished, famous open-source repo.
-->

![Spectra](https://raw.githubusercontent.com/your-org/spectra/main/assets/logo.png)

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/your-org/spectra/actions)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Kafka](https://img.shields.io/badge/Kafka-%20event--sourcing-orange.svg)](https://kafka.apache.org)
[![Redis](https://img.shields.io/badge/Redis-in-memory-red.svg)](https://redis.io)

# Spectra — Observability & Incident Management

Spectra is a **microservice architecture** for monitoring, alerting, and incident response.

Spectra is a self-hosted observability platform that collects performance metrics, ingests alerts, and drives an event-sourced incident workflow to reduce MTTA/MTTR. It combines metrics ingestion, alert routing, on-call orchestration, and automated post-mortems — all designed to run behind your infrastructure and integrate with Kafka, Redis, and your CI/CD.

For a dedicated architecture breakdown, see [docs/architecture.md](docs/architecture.md).

Features at a glance

- Metrics: latency (p50/ p95 / p99), throughput, error rates, CPU/memory, host-level metrics
- Alerts: webhook ingestion (Grafana, Prometheus, custom), rule-based triggers, deduplication
- Incident Management: event-sourced incidents, timeline, state machine (OPEN → ACK → RESOLVE)
- Deployment Correlation: auto-tag incidents with recent deployments and commits
- Background jobs: BullMQ workers for SLA checks, post-mortem drafts, weekly digests
- Developer ergonomics: Docker Compose, modular services, replayable Kafka topics

---

Table of contents

1. Quick links
2. Project architecture (diagram)
3. Services & responsibilities
4. Data flow & event model
5. UrbanEats incident (real-world narrative)
6. Quickstart (Docker Compose)
7. API contracts (examples)
8. Deployment & production notes
9. Contributing & code standards
10. License

---

Quick links

- Dashboard: [dashboard](dashboard)
- Alert service example: [alert-service compose](alert-service/compose.yaml)
- Ping service: [ping-service compose](ping-service/compose.yaml)
- Full repo services: anomaly-service, api-gateway, auth-service, dashboard-service, incident-service, oncall-service, payment-service, delivery-service

Architecture (high level)

```mermaid
flowchart LR
	subgraph Ingest
		Grafana[Grafana / Prometheus] -->|webhook| AlertService[alert-service]
	end

	AlertService -->|publish| Kafka((Kafka))
	Kafka --> IncidentService[incident-service]
	IncidentService -->|publish| Kafka
	IncidentService --> Postgres[(Postgres)]
	IncidentService --> TimelineService[timeline-service]
	TimelineService -->|writes| Redis[(Redis)]
	IncidentService --> OncallService[oncall-service]
	OncallService -->|pages| Slack[Slack / SMS]
	BullMQ[BullMQ workers] -->|jobs| IncidentService
	Dashboard[dashboard] -->|reads| Redis
	Dashboard -->|reads| Kafka
	CI[GitHub Actions] -->|deploy| Services
```

Services & responsibilities

- `alert-service` — Accepts HTTP webhooks (Grafana, Prometheus), normalizes payloads, publishes `alert.fired` to Kafka.
- `incident-service` — Consumes `alert.fired`, applies deduplication/state machine, stores incident records in Postgres, publishes `incident.*` events.
- `oncall-service` — Computes rotation, triggers paging (Slack/SMS), schedules acknowledgement timers (BullMQ).
- `timeline-service` — Writes short-lived, sub-ms timeline entries to Redis for dashboard reads.
- `dashboard-service` — React/Next.js frontend: incident view, metrics, timeline, and control actions (ack/resolve).
- `auth-service`, `service-registry`, `scraper-service`, `ping-service`, etc. — supporting infra.

Data flow & event model

- Events are the source of truth. Key topics: `alert.fired`, `incident.created`, `incident.updated`, `timeline.append`, `sla.checked`.
- Every user action (comment, ack, resolve) becomes an immutable Kafka event for replay and audit.

UrbanEats — Example incident (concise)

> At 02:47:33, Grafana rules detect `payment-service` error rate spiking to 34%.

- `alert-service` ingests webhook → publishes `alert.fired` to Kafka.
- `incident-service` creates `INC-2847` (CRITICAL) → publishes `incident.created`.
- `oncall-service` pages Rahul (L1) + starts 5-minute ack timer (BullMQ).
- Rahul acknowledges in 1m39s, inspects the dashboard — Spectra auto-tags a deployment 28 minutes earlier.
- Rahul rolls back, resolves incident (MTTR 27m19s). BullMQ kicks off post-mortem generator.

Quickstart — local (Docker Compose)

1. Copy environment template and edit values:

```bash
cp .env.example .env
# edit .env to point Kafka, Redis, Postgres, Slack webhook, etc.
```

2. Start core services (developer/dev-only quickstart):

```bash
docker compose -f compose.yaml up --build -d
```

3. Wait for services to initialize (Kafka, Postgres, Redis). Then open the dashboard:

```bash
open http://localhost:3000
```

4. Configure Grafana alerting to POST to the alert ingestion endpoint:

```
POST http://<alert-service-host>/api/v1/alerts/ingest
```

Environment variables (representative)

- `KAFKA_BROKERS` — kafka:9092
- `DATABASE_URL` — postgres://user:pass@postgres:5432/spectra
- `REDIS_URL` — redis://redis:6379
- `SLACK_WEBHOOK_URL` — for pages
- `SMTP_URL` — for weekly digest emails

API Contracts (examples)

Alert ingestion

Request

```
POST /api/v1/alerts/ingest
Content-Type: application/json

{
	"source": "grafana",
	"severity": "CRITICAL",
	"message": "payment-service error rate 34%",
	"service": "payment-service",
	"timestamp": "2026-05-26T02:47:33Z",
	"details": {...}
}
```

Response

```
200 {
	"ok": true,
	"data": { "eventId": "evt_01F..." }
}
```

Ack incident

```
PATCH /api/v1/incidents/:id/ack
Response: 200 { "ok": true, "data": { "incidentId": "INC-2847", "status": "ACKNOWLEDGED" }}
```

Operational & production notes

- Kafka: run with at least 3 brokers for durability; enable topic replication for `alert.*` and `incident.*` topics.
- Redis: use persistence/AOF or replication for timeline durability if you require longer retention than memory allows.
- Backups: nightly Postgres backups and Kafka retention policies that suit replay needs.
- Security: run behind an authenticated reverse proxy, enable TLS, and restrict internal APIs to the cluster network.

Contributing

1. Fork → branch → PR.
2. Run linters and tests before opening PRs.
3. Write a short PR description, link related issues, and include screenshots for UI changes.

Suggested development commands

```bash
# build and run a single service (example: alert-service)
cd alert-service
./mvnw spring-boot:run

# run the dashboard (next.js)
cd dashboard
pnpm install
pnpm dev
```

Troubleshooting

- If the dashboard shows no timeline entries, verify `timeline-service` connectivity to Redis and that `incident.*` events are being published to Kafka.
- If pages are not sent, confirm `SLACK_WEBHOOK_URL` and that `oncall-service` can reach external endpoints.

Acknowledgements

This README draws inspiration from several polished observability projects and incident-routing platforms. Designed to be friendly for startups who want to self-host their incident stack.

License

This project is MIT licensed—see [LICENSE](LICENSE).

---

If you'd like, I can also:

- add `.env.example`,
- generate a `docs/architecture.md` with an expanded Mermaid diagram, or
- produce API OpenAPI (Swagger) YAML for the HTTP endpoints.

— Spectra maintainers
