-- Reconcile legacy created_by_user_id column with current created_by mapping
DO $$
BEGIN
    -- If old column exists, keep it in sync and prevent it from blocking inserts
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'created_by_user_id'
    ) THEN
        -- Backfill legacy column from current mapped column
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'chat_conversations' AND column_name = 'created_by'
        ) THEN
            UPDATE chat_conversations
            SET created_by_user_id = created_by
            WHERE created_by_user_id IS NULL AND created_by IS NOT NULL;

            UPDATE chat_conversations
            SET created_by = created_by_user_id
            WHERE created_by IS NULL AND created_by_user_id IS NOT NULL;
        END IF;

        -- Entity writes created_by, so legacy column must not be NOT NULL
        ALTER TABLE chat_conversations
            ALTER COLUMN created_by_user_id DROP NOT NULL;
    END IF;
END $$;
