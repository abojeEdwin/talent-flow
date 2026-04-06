# talent-flow

## Deploying on Render

This repository includes a Render Blueprint at [render.yaml](render.yaml).

### Steps

1. Push your branch to GitHub.
2. In Render, create a new Blueprint and select this repository.
3. Confirm the web service `talent-flow-api` and database `talent-flow-db`.
4. Set all `sync: false` environment variables in Render before first deploy:
   - `CORS_ALLOWED_ORIGINS`
   - `RESEND_API_KEY`, `EMAIL_FROM`
   - `VERIFICATION_TOKEN_FRONTEND_URL`, `PASSWORD_RESET_FRONTEND_URL`, `LOGIN_URL`
   - `S3_BUCKET_ACCESS_KEY`, `S3_BUCKET_SECRET_KEY`, `S3_BUCKET_NAME` (and region if needed)
   - `ADMIN_SEED_EMAIL`, `ADMIN_SEED_PASSWORD` (only if `ADMIN_SEED_ENABLED=true`)
5. Deploy.

### Notes

- Production profile is configured in [application-prod.yml](app/src/main/resources/application-prod.yml).
- Health check endpoint is `/actuator/health`.
- Keep `ADMIN_SEED_ENABLED=false` after initial provisioning.
