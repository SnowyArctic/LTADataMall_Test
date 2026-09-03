# Spring Boot backend proxying the **LTA DataMall Bus Arrival API (v3)** for bus stop **83139**, with **PostgreSQL** persistence.

## Endpoint

### `GET /api/transit-feed`

Server-side fetch of the LTA DataMall Bus Arrival API
(`https://datamall2.mytransport.sg/ltaodataservice/v3/BusArrival?BusStopCode=83139`)
using the `AccountKey` header. The JSON payload is parsed and only the
incoming bus arrival timestamps are returned (flattened across `NextBus`–`NextBus3`,
sorted by arrival time). Every fetch is persisted to the database.

> Note: LTA deprecated the old `/BusArrivalv2` path (it now returns 404);
> v3 is the current endpoint.

**Example response:**

```json
{
  "busStopCode": "83139",
  "arrivals": [
    { "serviceNo": "964", "estimatedArrival": "2026-08-30T12:34:56+08:00", "load": "SEA" }
  ]
}
```

### `GET /health`

Readiness probe: `{"status":"ok","busStopCode":"83139"}`

## Stack

- **Java 17 + Spring Boot 3.5** (Web, Data JPA, Validation)
- **PostgreSQL** in production (managed, e.g. Tiger Cloud) via standard
  `SPRING_DATASOURCE_*` env vars; in-memory **H2** fallback for local dev
- LTA DataMall REST API via Spring `RestClient`

## Setup

1. Get a free **AccountKey** from [LTA DataMall](https://datamall.lta.gov.sg).
2. Configure environment variables:

   | Variable | Required | Description |
   | --- | --- | --- |
   | `LTA_ACCOUNT_KEY` | Yes | LTA DataMall AccountKey |
   | `SPRING_DATASOURCE_URL` | No | JDBC URL; defaults to in-memory H2 |
   | `SPRING_DATASOURCE_USERNAME` | No | DB user (H2 default `sa`) |
   | `SPRING_DATASOURCE_PASSWORD` | No | DB password |
   | `LTA_API_BASE_URL` | No | Override LTA endpoint (for tests) |
   | `LTA_BUS_STOP_CODE` | No | Defaults to `83139` |
   | `PORT` | No | Server port (default `8080`) |
   | `TRANSIT_PERSISTENCE_ENABLED` | No | Persist snapshots (default `true`) |
   | `TRANSIT_RETENTION_MINUTES` | No | Snapshot retention (default `60`) |

3. Build and run:

   ```bash
   mvn spring-boot:run
   ```

4. Test:

   ```bash
   curl http://localhost:8080/api/transit-feed
   ```

## Notes

- Without `LTA_ACCOUNT_KEY`, `/api/transit-feed` returns `500` with a clear error.
- LTA rejects missing keys with `404` and invalid keys with `401`; both are
  surfaced as `502` with a message telling you to check the key. Connection
  failures to LTA also return `502`.
- The schema is auto-created (`ddl-auto: update`) — a `arrival_snapshots` table
  stores one row per incoming bus with its fetched timestamp; rows older than
  the retention window are pruned on each fetch.
- `mvn test` runs the parser unit tests.
