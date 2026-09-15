-- ---------------------------------------------------------------------------
-- V26: add organization_id to courses (+ org FK + index).
--
-- Safe on a POPULATED live DB: courses rows keep working, and since the
-- default organization was seeded by V25, every row is backfilled BEFORE the
-- column is made NOT NULL (so SET NOT NULL can never reject an orphan).
-- ---------------------------------------------------------------------------

ALTER TABLE courses ADD COLUMN IF NOT EXISTS organization_id UUID;

-- Backfill existing rows to the default (first-seeded) organization
UPDATE courses
SET organization_id = (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE courses ADD CONSTRAINT fk_courses_organization
    FOREIGN KEY (organization_id) REFERENCES organizations(id);

CREATE INDEX idx_course_organization_id ON courses(organization_id);

-- Everything is backfilled above => safe to enforce NOT NULL
ALTER TABLE courses ALTER COLUMN organization_id SET NOT NULL;
