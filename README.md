# talent-flow

## Deploying the API on Vercel

The API is deployed as a container using [Dockerfile.vercel](Dockerfile.vercel). `vercel.json` enables Fluid compute, and the container binds Spring Boot to Vercel's `PORT` automatically.

1. Install and authenticate the Vercel CLI: `npm i -g vercel`, then `vercel login`.
2. From the repository root, run `vercel link` and select the Vercel project.
3. In **Project Settings > Environment Variables**, add production values for `MONGODB_URI`, `JWT_SECRET`, `PASSWORD_RESET_FRONTEND_URL`, `LOGIN_URL`, `EMAIL_FROM`, `SMTP_USER`, `SMTP_PASSWORD`, `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET`.
4. Run `vercel --prod`.

Keep `ADMIN_SEED_ENABLED=false` unless provisioning an administrator, and never commit credentials to either Spring configuration file. The health endpoint is `/actuator/health`.

## Deploying on Render

This repository includes a Render Blueprint at [render.yaml](render.yaml).

### Steps

1. Push your branch to GitHub.
2. In Render, create a new Blueprint and select this repository.
3. Confirm the web service `talent-flow-api` and database `talent-flow-db`.
4. Set all `sync: false` environment variables in Render before first deploy:
   - `CORS_ALLOWED_ORIGINS`
   - `RESEND_API_KEY`, `EMAIL_FROM`
   - `JWT_SECRET` (32+ characters), optional `JWT_EXPIRATION_MINUTES`
   - `PASSWORD_RESET_FRONTEND_URL`, `LOGIN_URL`
   - `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`
   - `ADMIN_SEED_EMAIL`, `ADMIN_SEED_PASSWORD` (only if `ADMIN_SEED_ENABLED=true`)
5. Deploy.

### Notes

- Production profile is configured in [application-prod.yml](app/src/main/resources/application-prod.yml).
- Health check endpoint is `/actuator/health`.
- Keep `ADMIN_SEED_ENABLED=false` after initial provisioning.
