CREATE TABLE course_modules (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_course_modules_course FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT uq_course_modules_position UNIQUE (course_id, position)
);

CREATE TABLE lessons (
    id UUID PRIMARY KEY,
    module_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    lesson_type VARCHAR(30) NOT NULL,
    content_url VARCHAR(500),
    content_text TEXT,
    position INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_lessons_module FOREIGN KEY (module_id) REFERENCES course_modules (id),
    CONSTRAINT uq_lessons_position UNIQUE (module_id, position)
);

CREATE TABLE lesson_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    lesson_id UUID NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_lesson_progress_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_lesson_progress_lesson FOREIGN KEY (lesson_id) REFERENCES lessons (id),
    CONSTRAINT uq_lesson_progress_user_lesson UNIQUE (user_id, lesson_id)
);

ALTER TABLE course_enrollments
    ADD COLUMN progress_pct NUMERIC(5,2) NOT NULL DEFAULT 0,
    ADD COLUMN completed_at TIMESTAMP NULL;
