-- Reconcile legacy chat message/participant schema with current JPA mappings

-- 1) Ensure chat_messages has all required columns
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_messages' AND column_name = 'conversation_id'
    ) THEN
        ALTER TABLE chat_messages ADD COLUMN conversation_id UUID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_messages' AND column_name = 'sender_id'
    ) THEN
        ALTER TABLE chat_messages ADD COLUMN sender_id UUID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_messages' AND column_name = 'content'
    ) THEN
        ALTER TABLE chat_messages ADD COLUMN content TEXT;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_messages' AND column_name = 'message_type'
    ) THEN
        ALTER TABLE chat_messages ADD COLUMN message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_messages' AND column_name = 'reply_to_message_id'
    ) THEN
        ALTER TABLE chat_messages ADD COLUMN reply_to_message_id UUID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_messages' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE chat_messages ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_messages' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE chat_messages ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'chat_messages' AND constraint_name = 'fk_chat_messages_conversation'
    ) THEN
        ALTER TABLE chat_messages
            ADD CONSTRAINT fk_chat_messages_conversation
            FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'chat_messages' AND constraint_name = 'fk_chat_messages_sender'
    ) THEN
        ALTER TABLE chat_messages
            ADD CONSTRAINT fk_chat_messages_sender
            FOREIGN KEY (sender_id) REFERENCES users(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'chat_messages' AND constraint_name = 'fk_chat_messages_reply_to_message'
    ) THEN
        ALTER TABLE chat_messages
            ADD CONSTRAINT fk_chat_messages_reply_to_message
            FOREIGN KEY (reply_to_message_id) REFERENCES chat_messages(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_chat_messages_conversation_id ON chat_messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_id ON chat_messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_reply_to_message_id ON chat_messages(reply_to_message_id);

-- 2) Ensure chat_conversation_participants has required columns
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversation_participants' AND column_name = 'conversation_id'
    ) THEN
        ALTER TABLE chat_conversation_participants ADD COLUMN conversation_id UUID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversation_participants' AND column_name = 'user_id'
    ) THEN
        ALTER TABLE chat_conversation_participants ADD COLUMN user_id UUID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversation_participants' AND column_name = 'role'
    ) THEN
        ALTER TABLE chat_conversation_participants ADD COLUMN role VARCHAR(20);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversation_participants' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE chat_conversation_participants ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'chat_conversation_participants' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE chat_conversation_participants ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'chat_conversation_participants' AND constraint_name = 'fk_chat_participants_conversation'
    ) THEN
        ALTER TABLE chat_conversation_participants
            ADD CONSTRAINT fk_chat_participants_conversation
            FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'chat_conversation_participants' AND constraint_name = 'fk_chat_participants_user'
    ) THEN
        ALTER TABLE chat_conversation_participants
            ADD CONSTRAINT fk_chat_participants_user
            FOREIGN KEY (user_id) REFERENCES users(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_chat_conversation_participants_conversation_id ON chat_conversation_participants(conversation_id);
CREATE INDEX IF NOT EXISTS idx_chat_conversation_participants_user_id ON chat_conversation_participants(user_id);
