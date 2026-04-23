-- Add type column to chat_conversations if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'chat_conversations' AND column_name = 'type'
    ) THEN
        ALTER TABLE chat_conversations ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'DIRECT'; -- Assuming 'DIRECT' as a reasonable default
    END IF;
END $$;
