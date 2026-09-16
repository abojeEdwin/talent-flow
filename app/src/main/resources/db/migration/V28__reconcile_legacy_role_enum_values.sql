-- ---------------------------------------------------------------------------
-- V28: Reconcile legacy users.role string values with the current RoleName
--      enum (ORG_ADMIN, INSTRUCTOR, LEARNER, SUPER_ADMIN).
--
-- V1/V8 wrote the legacy role names ('ADMIN', 'MENTOR', 'INTERN') into the
-- users.role column. The renamed enum does not contain those constants, so
-- Hibernate fails to hydrate any user row that still holds a legacy value:
--   No enum constant com.talentFlow.auth.domain.enums.RoleName.ADMIN
--
-- Mapping (matches the original intent of V8):
--   ADMIN   -> SUPER_ADMIN  (platform administrator)
--   MENTOR  -> INSTRUCTOR   (mentor/instructor role)
--   INTERN  -> LEARNER      (default learner role)
-- Unknown/blank values fall back to LEARNER so NOT NULL by default enums
-- can never block startup.
-- ---------------------------------------------------------------------------

UPDATE users
SET role = CASE role
           WHEN 'ADMIN'  THEN 'SUPER_ADMIN'
           WHEN 'MENTOR' THEN 'INSTRUCTOR'
           WHEN 'INTERN' THEN 'LEARNER'
           ELSE 'LEARNER'
END
WHERE role NOT IN ('ORG_ADMIN', 'INSTRUCTOR', 'LEARNER', 'SUPER_ADMIN');

-- The V8 default ('INTERN') no longer maps to the enum; keep defaults aligned.
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'LEARNER';