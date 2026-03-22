CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

CREATE TABLE admin_audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id UUID NULL,
    details TEXT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_admin_audit_actor_user FOREIGN KEY (actor_user_id) REFERENCES users (id)
);

INSERT INTO permissions (id, name, description, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'USER_MANAGE', 'Manage users and role assignments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000102', 'COURSE_MANAGE', 'Manage course lifecycle and assignments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000103', 'PROGRAM_MANAGE', 'Manage programs and cohorts', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000104', 'REPORT_VIEW', 'View and generate platform reports', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO role_permissions (role_id, permission_id)
VALUES
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000101'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000102'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000103'),
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000104');
