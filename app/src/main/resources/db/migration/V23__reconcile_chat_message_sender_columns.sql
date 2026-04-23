-- Reconcile legacy sender_user_id column with current sender_id mapping
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_messages' AND column_name = 'sender_user_id'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'chat_messages' AND column_name = 'sender_id'
        ) THEN
            UPDATE chat_messages
            SET sender_user_id = sender_id
            WHERE sender_user_id IS NULL AND sender_id IS NOT NULL;

            UPDATE chat_messages
            SET sender_id = sender_user_id
            WHERE sender_id IS NULL AND sender_user_id IS NOT NULL;
        END IF;

        -- Entity now writes sender_id; legacy column should not block inserts
        ALTER TABLE chat_messages
            ALTER COLUMN sender_user_id DROP NOT NULL;
    END IF;
END $$;
