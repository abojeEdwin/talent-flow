ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role VARCHAR(30);

UPDATE users u
SET role = CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM user_roles ur
                            JOIN roles r ON r.id = ur.role_id
                   WHERE ur.user_id = u.id
                     AND r.name = 'ADMIN'
               ) THEN 'ADMIN'
               WHEN EXISTS (
                   SELECT 1
                   FROM user_roles ur
                            JOIN roles r ON r.id = ur.role_id
                   WHERE ur.user_id = u.id
                     AND r.name IN ('MENTOR', 'INSTRUCTOR')
               ) THEN 'INSTRUCTOR'
               ELSE 'INTERN'
    END
WHERE role IS NULL;

ALTER TABLE users
    ALTER COLUMN role SET DEFAULT 'INTERN';

UPDATE users
SET role = 'INTERN'
WHERE role IS NULL;

ALTER TABLE users
    ALTER COLUMN role SET NOT NULL;
