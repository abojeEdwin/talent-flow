-- Reconcile legacy chat schema with current JPA mappings

-- 1) Ensure chat_conversations has all required columns and constraints
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'type'
    ) THEN
        ALTER TABLE chat_conversations ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'DIRECT';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'name'
    ) THEN
        ALTER TABLE chat_conversations ADD COLUMN name VARCHAR(120);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'cohort_id'
    ) THEN
        ALTER TABLE chat_conversations ADD COLUMN cohort_id UUID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'team_id'
    ) THEN
        ALTER TABLE chat_conversations ADD COLUMN team_id UUID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE chat_conversations ADD COLUMN created_by UUID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE chat_conversations ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE chat_conversations ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

DO $$
DECLARE fallback_user_id UUID;
BEGIN
    SELECT id INTO fallback_user_id
    FROM users
    ORDER BY created_at NULLS LAST, id
    LIMIT 1;

    IF fallback_user_id IS NOT NULL THEN
        UPDATE chat_conversations
        SET created_by = fallback_user_id
        WHERE created_by IS NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'chat_conversations' AND constraint_name = 'fk_chat_conversations_cohort'
    ) THEN
        ALTER TABLE chat_conversations
            ADD CONSTRAINT fk_chat_conversations_cohort
            FOREIGN KEY (cohort_id) REFERENCES cohorts(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'chat_conversations' AND constraint_name = 'fk_chat_conversations_team'
    ) THEN
        ALTER TABLE chat_conversations
            ADD CONSTRAINT fk_chat_conversations_team
            FOREIGN KEY (team_id) REFERENCES project_teams(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'chat_conversations' AND constraint_name = 'fk_chat_conversations_created_by'
    ) THEN
        ALTER TABLE chat_conversations
            ADD CONSTRAINT fk_chat_conversations_created_by
            FOREIGN KEY (created_by) REFERENCES users(id);
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'created_by' AND is_nullable = 'YES'
    ) AND NOT EXISTS (
        SELECT 1 FROM chat_conversations WHERE created_by IS NULL
    ) THEN
        ALTER TABLE chat_conversations ALTER COLUMN created_by SET NOT NULL;
    END IF;
END $$;

-- 2) Align read receipt table name with entity mapping
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'message_read_receipts'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'chat_message_read_receipts'
    ) THEN
        ALTER TABLE message_read_receipts RENAME TO chat_message_read_receipts;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS chat_message_read_receipts (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL REFERENCES chat_messages(id),
    user_id UUID NOT NULL REFERENCES users(id),
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(message_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_chat_message_read_receipts_message_id ON chat_message_read_receipts(message_id);
CREATE INDEX IF NOT EXISTS idx_chat_message_read_receipts_user_id ON chat_message_read_receipts(user_id);
