CREATE TABLE courses (
    id UUID PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL,
    created_by_user_id UUID NOT NULL,
    published_at TIMESTAMP NULL,
    archived_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_courses_created_by FOREIGN KEY (created_by_user_id) REFERENCES users (id)
);

CREATE TABLE course_instructors (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    instructor_user_id UUID NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_course_instructors_course FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT fk_course_instructors_user FOREIGN KEY (instructor_user_id) REFERENCES users (id),
    CONSTRAINT uq_course_instructor UNIQUE (course_id, instructor_user_id)
);

CREATE TABLE course_enrollments (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    enrolled_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_course_enrollments_course FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT fk_course_enrollments_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_course_enrollment UNIQUE (course_id, user_id)
);

CREATE TABLE course_materials (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    material_type VARCHAR(50) NOT NULL,
    content_url VARCHAR(500) NOT NULL,
    uploaded_by_user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_course_materials_course FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT fk_course_materials_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES users (id)
);

CREATE TABLE assignments (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    instructions TEXT,
    due_at TIMESTAMP NULL,
    max_score DECIMAL(10,2) NULL,
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_assignments_course FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT fk_assignments_created_by FOREIGN KEY (created_by_user_id) REFERENCES users (id)
);

CREATE TABLE assignment_submissions (
    id UUID PRIMARY KEY,
    assignment_id UUID NOT NULL,
    learner_user_id UUID NOT NULL,
    content_url VARCHAR(500) NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    score DECIMAL(10,2) NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_assignment_submissions_assignment FOREIGN KEY (assignment_id) REFERENCES assignments (id),
    CONSTRAINT fk_assignment_submissions_learner FOREIGN KEY (learner_user_id) REFERENCES users (id)
);

CREATE TABLE assignment_feedback (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL,
    instructor_user_id UUID NOT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_assignment_feedback_submission FOREIGN KEY (submission_id) REFERENCES assignment_submissions (id),
    CONSTRAINT fk_assignment_feedback_instructor FOREIGN KEY (instructor_user_id) REFERENCES users (id)
);
