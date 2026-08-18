-- Add organization_id to courses table
ALTER TABLE courses ADD COLUMN organization_id UUID;

-- Add foreign key constraint
ALTER TABLE courses ADD CONSTRAINT fk_courses_organization
    FOREIGN KEY (organization_id) REFERENCES organizations(id);

-- Create index for faster lookups
CREATE INDEX idx_course_organization_id ON courses(organization_id);

-- Make the organization_id column not nullable after backfilling
ALTER TABLE courses ALTER COLUMN organization_id SET NOT NULL;
