# SportFlow RabbitMQ Events Contract

This document defines the asynchronous messaging contract for SportFlow using RabbitMQ.

---

## 1. Exchange Configuration

- **Exchange Name:** `sportflow.events`
- **Exchange Type:** `topic`
- **Durability:** Durable (`durable = true`)
- **Auto-Delete:** No (`auto_delete = false`)

---

## 2. Queue Configuration

- **Queue Name:** `notification.queue`
- **Durability:** Durable (`durable = true`)
- **Auto-Delete:** No (`auto_delete = false`)
- **Bindings:**
  - `registration.*`
  - `competition.*`
  - `match.*`

---

## 3. General Message Guidelines

- **Content Type:** `application/json`
- **Content Encoding:** `UTF-8`
- **Timestamp Standard:** ISO-8601 in UTC (e.g., `2026-11-27T10:15:30Z`)

---

## 4. Routing Keys, Producers, and Schemas

### 4.1. `registration.created`
- **Producer:** `competition-service`
- **Consumer(s):** `notification-stats-service`
- **Description:** Emitted when a new athlete registration is initiated/created.
- **Payload Schema:**
  - `registrationId` (string, UUID): Identifier of the registration record.
  - `athleteId` (string, UUID): Identifier of the registered athlete.
  - `competitionId` (string, UUID): Identifier of the target competition.
  - `status` (string): Status of registration (e.g., `"PENDING"`).
  - `timestamp` (string, ISO-8601 UTC): Event creation timestamp.

**Example Payload:**
```json
{
  "registrationId": "reg-9821-4b12-a2df",
  "athleteId": "ath-1102-45e1-8899",
  "competitionId": "comp-5501-44bb-9901",
  "status": "PENDING",
  "timestamp": "2026-11-27T10:15:30Z"
}
```

---

### 4.2. `registration.rejected`
- **Producer:** `competition-service`
- **Consumer(s):** `notification-stats-service`
- **Description:** Emitted when an athlete registration is rejected (e.g., failed eligibility check or license verification).
- **Payload Schema:**
  - `registrationId` (string, UUID): Identifier of the registration record.
  - `athleteId` (string, UUID): Identifier of the rejected athlete.
  - `competitionId` (string, UUID): Identifier of the target competition.
  - `reason` (string): Rejection reason (e.g., `"WEIGHT_OUT_OF_RANGE"`, `"LICENSE_EXPIRED"`).
  - `timestamp` (string, ISO-8601 UTC): Event creation timestamp.

**Example Payload:**
```json
{
  "registrationId": "reg-9821-4b12-a2df",
  "athleteId": "ath-1102-45e1-8899",
  "competitionId": "comp-5501-44bb-9901",
  "reason": "WEIGHT_OUT_OF_RANGE",
  "timestamp": "2026-11-27T10:15:35Z"
}
```

---

### 4.3. `competition.created`
- **Producer:** `competition-service`
- **Consumer(s):** `notification-stats-service`
- **Description:** Emitted when an organizer creates a new sporting competition.
- **Payload Schema:**
  - `competitionId` (string, UUID): Identifier of the created competition.
  - `name` (string): Human-readable name of the competition.
  - `date` (string, ISO-8601 UTC date/datetime): Scheduled competition date.
  - `timestamp` (string, ISO-8601 UTC): Event creation timestamp.

**Example Payload:**
```json
{
  "competitionId": "comp-5501-44bb-9901",
  "name": "Tunis Judo Open 2026",
  "date": "2026-12-15T09:00:00Z",
  "timestamp": "2026-11-27T10:15:40Z"
}
```

---

### 4.4. `match.finished`
- **Producer:** `live-service`
- **Consumer(s):** `notification-stats-service`
- **Description:** Emitted when a live match or heat ends with final scores.
- **Payload Schema:**
  - `competitionId` (string, UUID): Identifier of the competition.
  - `scoreA` (integer): Final score of Athlete A.
  - `scoreB` (integer): Final score of Athlete B.
  - `winner` (string): Identifier or name of the winner (or `"DRAW"`).
  - `timestamp` (string, ISO-8601 UTC): Event creation timestamp.

**Example Payload:**
```json
{
  "competitionId": "comp-5501-44bb-9901",
  "scoreA": 10,
  "scoreB": 0,
  "winner": "ath-1102-45e1-8899",
  "timestamp": "2026-11-27T11:45:10Z"
}
```
