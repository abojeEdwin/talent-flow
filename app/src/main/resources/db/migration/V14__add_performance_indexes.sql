-- Index for filtering users by role and status (common in Admin searches)
CREATE INDEX idx_users_role_status ON users (role, status);

-- Functional index for case-insensitive email searches
CREATE INDEX idx_users_email_lower ON users (lower(email));

-- Composite functional index for case-insensitive name searches
CREATE INDEX idx_users_names_lower ON users (lower(first_name), lower(last_name));

-- Index on team_members(user_id) to speed up "NOT EXISTS" checks for unallocated interns
CREATE INDEX idx_team_members_user_id ON team_members (user_id);

-- Index on cohort_id and team name (already has unique constraint, but helps with joins/filters)
CREATE INDEX idx_project_teams_cohort_id ON project_teams (cohort_id);
