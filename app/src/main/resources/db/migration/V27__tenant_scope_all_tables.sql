-- ---------------------------------------------------------------------------
-- V27: Tenant-scope ALL remaining tenant-owned tables with an
--      organization_id column (cohorts, project_teams, team_members,
--      course_modules, lessons, course_enrollments, course_instructors,
--      course_materials, assignments, assignment_submissions,
--      assignment_feedback, lesson_progress, notifications,
--      admin_audit_logs, media_upload_jobs, chat_conversations,
--      chat_conversation_participants, chat_messages,
--      chat_message_read_receipts).
--
-- Chain backfill follows the existing relationships (e.g. lessons ->
-- course_modules -> courses -> organization_id). Every chain gets a
-- FALLBACK to the default organization seeded by V25, so a row whose
-- relationship chain is incomplete can NEVER make SET NOT NULL fail.
-- ---------------------------------------------------------------------------

-- Local default org (seeded by V25):
--   SELECT id FROM organizations ORDER BY created_at LIMIT 1

-- ---------------------------------------------------------------------------
-- 1. cohorts
-- ---------------------------------------------------------------------------
ALTER TABLE cohorts ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE cohorts c
SET organization_id = u.organization_id
FROM team_members tm
         JOIN project_teams pt ON pt.id = tm.team_id
         JOIN users u ON u.id = tm.user_id
WHERE pt.cohort_id = c.id
  AND u.organization_id IS NOT NULL;

UPDATE cohorts SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE cohorts ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE cohorts ADD CONSTRAINT fk_cohorts_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_cohort_organization_id ON cohorts (organization_id);
CREATE INDEX idx_cohort_org_backfill ON cohorts (organization_id);

-- ---------------------------------------------------------------------------
-- 2. project_teams
-- ---------------------------------------------------------------------------
ALTER TABLE project_teams ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE project_teams pt
SET organization_id = c.organization_id
FROM cohorts c
WHERE c.id = pt.cohort_id;

UPDATE project_teams SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE project_teams ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE project_teams ADD CONSTRAINT fk_project_teams_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_project_team_organization_id ON project_teams (organization_id);

-- ---------------------------------------------------------------------------
-- 3. team_members
-- ---------------------------------------------------------------------------
ALTER TABLE team_members ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE team_members tm
SET organization_id = u.organization_id
FROM users u
WHERE u.id = tm.user_id;

UPDATE team_members SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE team_members ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE team_members ADD CONSTRAINT fk_team_members_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_team_member_organization_id ON team_members (organization_id);

-- ---------------------------------------------------------------------------
-- 4. course_modules
-- ---------------------------------------------------------------------------
ALTER TABLE course_modules ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE course_modules cm
SET organization_id = c.organization_id
FROM courses c
WHERE c.id = cm.course_id;

UPDATE course_modules SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE course_modules ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE course_modules ADD CONSTRAINT fk_course_modules_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_course_module_organization_id ON course_modules (organization_id);

-- ---------------------------------------------------------------------------
-- 5. lessons
-- ---------------------------------------------------------------------------
ALTER TABLE lessons ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE lessons l
SET organization_id = cm.organization_id
FROM course_modules cm
WHERE cm.id = l.module_id;

UPDATE lessons SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE lessons ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE lessons ADD CONSTRAINT fk_lessons_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_lesson_organization_id ON lessons (organization_id);

-- ---------------------------------------------------------------------------
-- 6. course_enrollments
-- ---------------------------------------------------------------------------
ALTER TABLE course_enrollments ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE course_enrollments ce
SET organization_id = c.organization_id
FROM courses c
WHERE c.id = ce.course_id;

UPDATE course_enrollments SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE course_enrollments ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE course_enrollments ADD CONSTRAINT fk_course_enrollments_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_course_enrollment_organization_id ON course_enrollments (organization_id);

-- ---------------------------------------------------------------------------
-- 7. course_instructors
-- ---------------------------------------------------------------------------
ALTER TABLE course_instructors ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE course_instructors ci
SET organization_id = c.organization_id
FROM courses c
WHERE c.id = ci.course_id;

UPDATE course_instructors SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE course_instructors ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE course_instructors ADD CONSTRAINT fk_course_instructors_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_course_instructor_organization_id ON course_instructors (organization_id);

-- ---------------------------------------------------------------------------
-- 8. course_materials
-- ---------------------------------------------------------------------------
ALTER TABLE course_materials ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE course_materials cm
SET organization_id = c.organization_id
FROM courses c
WHERE c.id = cm.course_id;

UPDATE course_materials SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE course_materials ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE course_materials ADD CONSTRAINT fk_course_materials_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_course_material_organization_id ON course_materials (organization_id);

-- ---------------------------------------------------------------------------
-- 9. assignments
-- ---------------------------------------------------------------------------
ALTER TABLE assignments ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE assignments a
SET organization_id = c.organization_id
FROM courses c
WHERE c.id = a.course_id;

UPDATE assignments SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE assignments ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE assignments ADD CONSTRAINT fk_assignments_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_assignment_organization_id ON assignments (organization_id);

-- ---------------------------------------------------------------------------
-- 10. assignment_submissions
-- ---------------------------------------------------------------------------
ALTER TABLE assignment_submissions ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE assignment_submissions asub
SET organization_id = a.organization_id
FROM assignments a
WHERE a.id = asub.assignment_id;

UPDATE assignment_submissions SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE assignment_submissions ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE assignment_submissions ADD CONSTRAINT fk_assignment_submissions_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_assignment_submission_organization_id ON assignment_submissions (organization_id);

-- ---------------------------------------------------------------------------
-- 11. assignment_feedback
-- ---------------------------------------------------------------------------
ALTER TABLE assignment_feedback ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE assignment_feedback af
SET organization_id = asub.organization_id
FROM assignment_submissions asub
WHERE asub.id = af.submission_id;

UPDATE assignment_feedback SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE assignment_feedback ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE assignment_feedback ADD CONSTRAINT fk_assignment_feedback_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_assignment_feedback_organization_id ON assignment_feedback (organization_id);

-- ---------------------------------------------------------------------------
-- 12. lesson_progress
-- ---------------------------------------------------------------------------
ALTER TABLE lesson_progress ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE lesson_progress lp
SET organization_id = u.organization_id
FROM users u
WHERE u.id = lp.user_id;

UPDATE lesson_progress SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE lesson_progress ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE lesson_progress ADD CONSTRAINT fk_lesson_progress_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_lesson_progress_organization_id ON lesson_progress (organization_id);

-- ---------------------------------------------------------------------------
-- 13. notifications
-- ---------------------------------------------------------------------------
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE notifications n
SET organization_id = u.organization_id
FROM users u
WHERE u.id = n.user_id;

UPDATE notifications SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE notifications ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_notification_organization_id ON notifications (organization_id);

-- ---------------------------------------------------------------------------
-- 14. admin_audit_logs
-- ---------------------------------------------------------------------------
ALTER TABLE admin_audit_logs ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE admin_audit_logs aal
SET organization_id = u.organization_id
FROM users u
WHERE u.id = aal.actor_user_id;

UPDATE admin_audit_logs SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE admin_audit_logs ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE admin_audit_logs ADD CONSTRAINT fk_admin_audit_logs_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_admin_audit_log_organization_id ON admin_audit_logs (organization_id);

-- ---------------------------------------------------------------------------
-- 15. media_upload_jobs
-- ---------------------------------------------------------------------------
ALTER TABLE media_upload_jobs ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE media_upload_jobs muj
SET organization_id = u.organization_id
FROM users u
WHERE u.id = muj.initiated_by_user_id;

UPDATE media_upload_jobs SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE media_upload_jobs ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE media_upload_jobs ADD CONSTRAINT fk_media_upload_jobs_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_media_upload_job_organization_id ON media_upload_jobs (organization_id);

-- ---------------------------------------------------------------------------
-- 16. chat_conversations
-- ---------------------------------------------------------------------------
ALTER TABLE chat_conversations ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE chat_conversations cc
SET organization_id = u.organization_id
FROM users u
WHERE u.id = cc.created_by;

UPDATE chat_conversations SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE chat_conversations ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE chat_conversations ADD CONSTRAINT fk_chat_conversations_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_chat_conversation_organization_id ON chat_conversations (organization_id);

-- ---------------------------------------------------------------------------
-- 17. chat_conversation_participants
-- ---------------------------------------------------------------------------
ALTER TABLE chat_conversation_participants ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE chat_conversation_participants ccp
SET organization_id = u.organization_id
FROM users u
WHERE u.id = ccp.user_id;

UPDATE chat_conversation_participants SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE chat_conversation_participants ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE chat_conversation_participants ADD CONSTRAINT fk_chat_conversation_participants_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_chat_participant_organization_id ON chat_conversation_participants (organization_id);

-- ---------------------------------------------------------------------------
-- 18. chat_messages
-- ---------------------------------------------------------------------------
ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE chat_messages cm
SET organization_id = cc.organization_id
FROM chat_conversations cc
WHERE cc.id = cm.conversation_id;

UPDATE chat_messages SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE chat_messages ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE chat_messages ADD CONSTRAINT fk_chat_messages_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_chat_message_organization_id ON chat_messages (organization_id);

-- ---------------------------------------------------------------------------
-- 19. chat_message_read_receipts
-- ---------------------------------------------------------------------------
ALTER TABLE chat_message_read_receipts ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE chat_message_read_receipts cmrr
SET organization_id = u.organization_id
FROM users u
WHERE u.id = cmrr.user_id;

UPDATE chat_message_read_receipts SET organization_id =
    (SELECT id FROM organizations ORDER BY created_at LIMIT 1)
WHERE organization_id IS NULL;

ALTER TABLE chat_message_read_receipts ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE chat_message_read_receipts ADD CONSTRAINT fk_chat_message_read_receipts_organization
    FOREIGN KEY (organization_id) REFERENCES organizations (id);
CREATE INDEX idx_chat_message_read_receipt_organization_id ON chat_message_read_receipts (organization_id);

-- ---------------------------------------------------------------------------
-- users + courses propagate BEFORE this migration via V25/V26, so all
-- === DONE: every tenant-owned table now has a NOT NULL organization_id. ===
-- ---------------------------------------------------------------------------
