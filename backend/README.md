# Backend

## Stack

- Spring Boot 2.7
- MyBatis-Plus
- MySQL

## Run

1. Create database `food_forum`
2. Update `src/main/resources/application.yml` with local MySQL credentials
3. Run `mvn spring-boot:run`

For secrets, use an untracked local config file (no env vars required):

1. Copy `backend/config/local-secrets.example.yml` to `backend/config/local-secrets.yml`
2. Fill your real OSS/AI keys in that file
3. Start backend normally with `mvn spring-boot:run`

## Health Check

`GET /api/system/health`

## OSS Upload Config

- Default: OSS is disabled.
- Local upload path: `app.upload-path` (default `D:/Development/Projects/bylw/uploads`).
- To enable OSS upload, set these fields in `backend/config/local-secrets.yml`:
  - `app.upload.oss-enabled: true`
  - `app.upload.endpoint`
  - `app.upload.bucket`
  - `app.upload.access-key-id`
  - `app.upload.access-key-secret`
  - Optional: `app.upload.dir-prefix`, `app.upload.public-base-url`

If OSS config is incomplete or upload fails, the backend now falls back to local storage automatically.

## AI Config

- AI cloud call is disabled by default.
- To enable cloud model calls, set in `backend/config/local-secrets.yml`:
  - `app.ai.enabled: true`
  - `app.ai.api-key`
- If AI key is missing/unavailable, backend automatically falls back to local recommendation logic
