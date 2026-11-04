-- =========================================================================
-- ESKWELA ENTERPRISE DATABASE SCHEMA (POSTGRESQL / MYSQL)
-- Architected with Normalization, Enums, Foreign Keys, Audit & Soft Deletes
-- =========================================================================

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS focus_sessions CASCADE;
DROP TABLE IF EXISTS ai_quizzes CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS schedules CASCADE;
DROP TABLE IF EXISTS announcements CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Define Enums
CREATE TYPE user_role AS ENUM ('student', 'admin');
CREATE TYPE user_status AS ENUM ('active', 'pending', 'suspended', 'deactivated', 'archived');
CREATE TYPE task_priority AS ENUM ('HIGH', 'MEDIUM', 'LOW');

-- 1. USERS TABLE
CREATE TABLE users (
    id VARCHAR(128) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(150),
    student_id VARCHAR(50) UNIQUE,
    school VARCHAR(200),
    course VARCHAR(150),
    year VARCHAR(20),
    section VARCHAR(50),
    role user_role NOT NULL DEFAULT 'student',
    status user_status NOT NULL DEFAULT 'active',
    suspension_duration VARCHAR(50),
    suspended_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- 2. SCHEDULES TABLE (Study Load Timetable)
CREATE TABLE schedules (
    id VARCHAR(128) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    class_date DATE NOT NULL,
    class_name VARCHAR(200) NOT NULL,
    class_code VARCHAR(50) NOT NULL,
    school VARCHAR(150),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(50),
    instructor VARCHAR(150),
    color_type VARCHAR(30) NOT NULL DEFAULT 'BLUE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. TASKS TABLE (Linked to User & Optional Schedule/Class)
CREATE TABLE tasks (
    id VARCHAR(128) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    schedule_id VARCHAR(128),
    title TEXT NOT NULL,
    due_date TIMESTAMP WITH TIME ZONE NOT NULL,
    course_name VARCHAR(150),
    priority task_priority NOT NULL DEFAULT 'MEDIUM',
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE SET NULL
);

-- 4. AI_QUIZZES TABLE (Normalized - joined with users for email/name)
CREATE TABLE ai_quizzes (
    id VARCHAR(128) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    topic VARCHAR(250) NOT NULL,
    score INT NOT NULL CHECK (score >= 0),
    total_questions INT NOT NULL CHECK (total_questions > 0),
    percentage NUMERIC(5,2) GENERATED ALWAYS AS (ROUND((score::NUMERIC / total_questions::NUMERIC) * 100, 2)) STORED,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. ANNOUNCEMENTS TABLE (Admin broadcasts, optionally scoped by school)
CREATE TABLE announcements (
    id VARCHAR(128) PRIMARY KEY,
    admin_id VARCHAR(128) NOT NULL,
    school_scope VARCHAR(200),
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 6. FOCUS_SESSIONS TABLE (Pomodoro Analytics)
CREATE TABLE focus_sessions (
    id VARCHAR(128) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    duration_minutes INT NOT NULL CHECK (duration_minutes > 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =========================================================================
-- INDEXES FOR HIGH-PERFORMANCE QUERYING
-- =========================================================================
CREATE INDEX idx_schedules_user_date ON schedules(user_id, class_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_tasks_user_status ON tasks(user_id, completed) WHERE deleted_at IS NULL;
CREATE INDEX idx_quizzes_user_percentage ON ai_quizzes(user_id, percentage DESC);

-- =========================================================================
-- SAMPLE SEED DATA
-- =========================================================================

INSERT INTO users (id, email, full_name, student_id, school, course, year, section, role, status)
VALUES ('admin-uid-001', 'admin@eskwela.com', 'System Administrator', 'ADM-0001', 'ESKWELA University', 'Administration', 'Admin', 'ADMIN-01', 'admin', 'active');

INSERT INTO users (id, email, full_name, student_id, school, course, year, section, role, status)
VALUES ('student-uid-101', 'student@eskwela.com', 'Juan Dela Cruz', 'STU-4921', 'University of the Philippines', 'BS Information Technology', '4th Year', 'IT41C1', 'student', 'active');

INSERT INTO schedules (id, user_id, class_date, class_name, class_code, school, start_time, end_time, room, instructor, color_type)
VALUES
('sch-01', 'student-uid-101', '2025-05-12', 'CAPSTONE PROJECT 1', 'IT6398', 'University', '15:00:00', '16:30:00', '301', '3 Units · Sec IT41C1', 'BLUE'),
('sch-02', 'student-uid-101', '2025-05-12', 'LOAD TESTING', 'CS6303', 'University', '12:00:00', '14:30:00', 'SLAB5', '3 Units · Sec IT41C2', 'ORANGE'),
('sch-03', 'student-uid-101', '2025-05-12', 'SOFTWARE ENGINEERING', 'CS6209', 'University', '16:30:00', '17:30:00', '204', '3 Units · Sec IT41C6', 'GREEN');

INSERT INTO tasks (id, user_id, schedule_id, title, due_date, course_name, priority, completed)
VALUES
('tsk-01', 'student-uid-101', 'sch-01', 'Submit Chapter 1 Draft', '2025-05-12 23:59:00+00', 'CAPSTONE PROJECT 1', 'HIGH', FALSE),
('tsk-02', 'student-uid-101', 'sch-02', 'JMeter Load Test Script', '2025-05-13 17:00:00+00', 'LOAD TESTING', 'MEDIUM', FALSE);

INSERT INTO ai_quizzes (id, user_id, topic, score, total_questions)
VALUES
('quiz-01', 'student-uid-101', 'LOAD TESTING', 10, 10);

INSERT INTO announcements (id, admin_id, message)
VALUES
('ann-01', 'admin-uid-001', 'Welcome to ESKWELA! Midterm examinations start next week. Please check your exam schedule in the app.');
