-- Ensure older chat_conversations tables have scope columns used by Conversation entity
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'cohort_id'
    ) THEN
        ALTER TABLE chat_conversations ADD COLUMN cohort_id UUID;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'team_id'
    ) THEN
        ALTER TABLE chat_conversations ADD COLUMN team_id UUID;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'chat_conversations' AND constraint_name = 'fk_chat_conversations_cohort'
    ) THEN
        ALTER TABLE chat_conversations
            ADD CONSTRAINT fk_chat_conversations_cohort
            FOREIGN KEY (cohort_id) REFERENCES cohorts(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'chat_conversations' AND constraint_name = 'fk_chat_conversations_team'
    ) THEN
        ALTER TABLE chat_conversations
            ADD CONSTRAINT fk_chat_conversations_team
            FOREIGN KEY (team_id) REFERENCES project_teams(id);
    END IF;
END $$;
