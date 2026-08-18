-- Add id as the primary key
ALTER TABLE users ADD COLUMN id UUID;
UPDATE users SET id = gen_random_uuid();
ALTER TABLE users ADD PRIMARY KEY (id);

-- Add organization_id to users table
ALTER TABLE users ADD COLUMN organization_id UUID;

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_users_organization
    FOREIGN KEY (organization_id) REFERENCES organizations(id);

-- Create index for faster lookups
CREATE INDEX idx_user_organization_id ON users(organization_id);

-- Make the organization_id column not nullable after backfilling
ALTER TABLE users ALTER COLUMN organization_id SET NOT NULL;
