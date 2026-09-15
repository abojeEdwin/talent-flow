-- ---------------------------------------------------------------------------
-- V25: attach users to an organization + seed the default organization.
--
-- IMPORTANT (real-baseline reconciliation): a live, populated DB exists and
-- the real baseline (V1__init_auth_schema) ALREADY gives `users.id` its UUID
-- PRIMARY KEY. So this migration does NOT re-add `id` (that was the bug that
-- made a previous V25 crash with "column id of relation users already
-- exists"). It only:
--   (1) seeds ONE default/root organization (idempotent),
--   (2) adds nullable organization_id to users,
--   (3) backfills every existing user row to the default org,
--   (4) then makes the column NOT NULL (satisfies Hibernate validate, whose
--       entities map organization_id with nullable=false).
-- ---------------------------------------------------------------------------

-- 1. Seed the default organization (idempotent). All pre-existing rows get
--    this org so that (a) migration backfills are deterministic and
--    (b) the tenant filter can never silently hide legacy rows.
INSERT INTO organizations (id, name, description, created_at, updated_at)
SELECT gen_random_uuid(), 'Default Organization', 'Auto-seeded root organization for pre-tenant data',
       now(), now()
WHERE NOT EXISTS (SELECT 1 FROM organizations);

-- 2. Add organization_id to users (nullable initially so rows survive)
ALTER TABLE users ADD COLUMN IF NOT EXISTS organization_id UUID;

-- 3. Backfill: every existing user → the seeded default org
UPDATE users SET organization_id = (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

-- 4. Foreign key + index (drop-then-add so a partially-applied older V25 that
--    already left these objects in the live DB can never block a re-run)
ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_users_organization;
ALTER TABLE users ADD CONSTRAINT fk_users_organization
    FOREIGN KEY (organization_id) REFERENCES organizations(id);
DROP INDEX IF EXISTS idx_user_organization_id;
CREATE INDEX idx_user_organization_id ON users(organization_id);

-- 5. Now safe to enforce: NOT NULL matches the entity mapping (nullable=false)
ALTER TABLE users ALTER COLUMN organization_id SET NOT NULL;
