# Spectra API Inventory

Phase 0 inventory generated from the current Java controllers, DTOs, and security configuration. Paths below are browser-facing gateway paths unless noted otherwise.

## Gateway

| Route               | Upstream            | Notes                                                                 |
| ------------------- | ------------------- | --------------------------------------------------------------------- |
| `/api/auth/**`      | `auth-service`      | Registered in `api-gateway/src/main/resources/application.properties` |
| `/api/incidents/**` | `incident-service`  | Registered                                                            |
| `/api/dashboard/**` | `dashboard-service` | Registered                                                            |
| `/api/projects/**`  | `dashboard-service` | Registered                                                            |
| `/api/anomalies/**` | `anomaly-service`   | Registered                                                            |
| `/api/ping/**`      | `ping-service`      | Registered                                                            |
| `/api/scraper/**`   | `scraper-service`   | Registered                                                            |

The gateway permits requests and supplies CORS for the configured `DASHBOARD_ORIGIN`. Authentication is enforced by downstream services, not by the gateway.

## auth-service

Controller base path: `/api/auth`

| Method | Path                 | Request                     | Response                                               | Auth     |
| ------ | -------------------- | --------------------------- | ------------------------------------------------------ | -------- |
| POST   | `/api/auth/register` | `{ name, email, password }` | `201 { id, name, email, role }`                        | Public   |
| POST   | `/api/auth/login`    | `{ email, password }`       | `200 { accessToken, user: { id, name, email, role } }` | Public   |
| POST   | `/api/auth/logout`   | None                        | `204`                                                  | Required |
| GET    | `/api/auth/me`       | None                        | `{ id, name, email, role }`                            | Required |

Register validation: name max 120, email max 320 and valid, password 8-72 characters.

## dashboard-service

Controller base paths: `/api/dashboard`, `/api/projects`

| Method | Path                     | Request            | Response                                                                                                                                                   | Auth                   |
| ------ | ------------------------ | ------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------- |
| GET    | `/api/dashboard/summary` | None               | `{ totalIncidents, openIncidents, acknowledgedIncidents, resolvedIncidents, criticalIncidents, activeAlerts, servicesUp, servicesDown, avgMtta, avgMttr }` | Required               |
| GET    | `/api/projects`          | None               | `Project[]`                                                                                                                                                | Required               |
| GET    | `/api/projects/{id}`     | UUID path variable | `Project`                                                                                                                                                  | Required; owner scoped |
| POST   | `/api/projects`          | `{ name, url }`    | `201 Project`                                                                                                                                              | Required; owner scoped |

`Project` is `{ id, name, url, status, createdAt, updatedAt }`. The project URL must use HTTP or HTTPS and have a host.

## incident-service

Controller base path: `/api/incidents`

| Method | Path                              | Request/query                                   | Response                               | Auth     |
| ------ | --------------------------------- | ----------------------------------------------- | -------------------------------------- | -------- |
| GET    | `/api/incidents`                  | Optional `severity`, `status` query parameters  | `Incident[]`                           | Required |
| GET    | `/api/incidents/{id}`             | Long path variable                              | `Incident` including events            | Required |
| GET    | `/api/incidents/{id}/events`      | Long path variable                              | `EventResponse[]`                      | Required |
| POST   | `/api/incidents`                  | `{ title, description, serviceName, severity }` | `201 Incident`                         | Required |
| PATCH  | `/api/incidents/{id}/acknowledge` | None                                            | Updated `Incident`                     | Required |
| PATCH  | `/api/incidents/{id}/resolve`     | None                                            | Updated `Incident`                     | Required |
| PATCH  | `/api/incidents/{id}/reopen`      | None                                            | Updated `Incident`                     | Required |
| GET    | `/api/incidents/metrics/mtta`     | Optional `from`, `to`, `service`                | `{ metric, averageMinutes, from, to }` | Required |
| GET    | `/api/incidents/metrics/mttr`     | Optional `from`, `to`, `service`                | `{ metric, averageMinutes, from, to }` | Required |

`Incident` includes `id`, `title`, `description`, `serviceName`, `severity`, `status`, timestamps, `mtta`, `mttr`, `createdBy`, `linkedAlertIds`, and optional event records. Supported statuses are `OPEN`, `ACKNOWLEDGED`, and `RESOLVED`; supported severities are `LOW`, `MEDIUM`, `HIGH`, and `CRITICAL`.

## ping-service

Controller base path: `/api/ping`

| Method | Path                      | Request         | Response                           | Auth                         |
| ------ | ------------------------- | --------------- | ---------------------------------- | ---------------------------- |
| GET    | `/api/ping`               | None            | `{ status, service, timestamp }`   | Public                       |
| GET    | `/api/ping/status`        | None            | `CheckResponse[]`                  | Required                     |
| GET    | `/api/ping/status/{name}` | Target name     | `{ targetName, current, history }` | Required for dynamic targets |
| POST   | `/api/ping/targets`       | `{ name, url }` | `204`                              | Required                     |

`CheckResponse` is `{ targetName, status, latencyMs, checkedAt }`. Dynamic target registration is owner scoped. The target endpoint intentionally returns no body.

## anomaly-service

Controller base path: `/api/anomalies`

| Method | Path                      | Request/query                            | Response                      | Auth     |
| ------ | ------------------------- | ---------------------------------------- | ----------------------------- | -------- |
| GET    | `/api/anomalies`          | Optional `service`, `severity`, `status` | `Alert[]` sorted newest first | Required |
| GET    | `/api/anomalies/{id}`     | Long path variable                       | `Alert`                       | Required |
| POST   | `/api/anomalies/evaluate` | None                                     | `{ status: "evaluated" }`     | Required |

`Alert` is `{ id, serviceName, type, severity, message, sourceMetric, triggeredAt, resolvedAt }`.

## scraper-service

Controller base path: `/api/scraper`

| Method | Path                          | Request/query                                                        | Response                                             | Auth     |
| ------ | ----------------------------- | -------------------------------------------------------------------- | ---------------------------------------------------- | -------- |
| GET    | `/api/scraper`                | None                                                                 | `{ status, service }`                                | Public   |
| POST   | `/api/scraper/ingest`         | `{ serviceName, timestamp, metrics: [{ metricName, value, unit }] }` | `201 MetricSample[]`                                 | Required |
| GET    | `/api/scraper/metrics`        | Optional `service`, `metric`, `from`, `to`, `page`, `size`           | `{ content, page, size, totalElements, totalPages }` | Required |
| GET    | `/api/scraper/metrics/latest` | Required `service`, optional `size`                                  | `{ serviceName, metrics }`                           | Required |

## service-registry

No REST controller or browser-facing endpoint exists in the current source. The module only contains the Spring Boot application class and is not routed by the gateway.

## Implemented but undocumented in the README

The project and dynamic ping-target endpoints are implemented and routed. They are the backend used by the dashboard's current website monitoring flow.

## README-only or missing domains

The README references `alert-service`, `oncall-service`, `timeline-service`, `payment-service`, and `delivery-service`, but no corresponding service folders/controllers exist in this workspace. There is no `/api/v1/alerts/ingest`, rule-management, on-call, deployment, post-mortem, audit-log, or service-registry REST contract currently implemented.

## Phase 0 findings

1. The existing Create Project flow was failing after the project was created because `POST /api/ping/targets` returned `201` with an empty body while `dashboard/lib/api.ts` unconditionally attempted JSON parsing for every successful response other than `204`.
2. The flow now uses `204 No Content`, and the shared client also accepts any successful empty response.
3. The gateway CORS default is `http://localhost:3000`; non-local dashboard deployments must set `DASHBOARD_ORIGIN`.
4. The root Compose file validates, but it does not define a Kafka container. Services default to `host.docker.internal:9092`, so the documented one-command stack requires an external Kafka broker or a later Compose infrastructure fix.
5. The dashboard currently has no middleware or server-side route protection; authentication is a client-side redirect in `components/Shell.tsx`.
