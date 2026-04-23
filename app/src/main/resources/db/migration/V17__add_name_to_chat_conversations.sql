-- Add name column to chat_conversations if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'name'
    ) THEN
        ALTER TABLE chat_conversations ADD COLUMN name VARCHAR(120);
    END IF;
END $$;
