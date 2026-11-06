-- =========================================================================
-- ESKWELA DATABASE SCHEMA CREATION SCRIPT (DDL)
-- Backend Engine: Google Firebase Auth & Firestore NoSQL / Relational Export
-- =========================================================================

DROP TABLE IF EXISTS tblFocusSessions CASCADE;
DROP TABLE IF EXISTS tblAnnouncements CASCADE;
DROP TABLE IF EXISTS tblAiQuizzes CASCADE;
DROP TABLE IF EXISTS tblTasks CASCADE;
DROP TABLE IF EXISTS tblSchedules CASCADE;
DROP TABLE IF EXISTS tblUsers CASCADE;

-- 1. USERS TABLE
CREATE TABLE tblUsers (
    id VARCHAR(128) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    fullName VARCHAR(150),
    studentId VARCHAR(50) UNIQUE,
    school VARCHAR(200),
    course VARCHAR(150),
    year VARCHAR(20),
    section VARCHAR(50),
    role VARCHAR(30) NOT NULL DEFAULT 'student',
    status VARCHAR(30) NOT NULL DEFAULT 'active',
    suspensionDuration VARCHAR(50),
    suspendedAt BIGINT
);

-- 2. SCHEDULES TABLE
CREATE TABLE tblSchedules (
    id VARCHAR(128) PRIMARY KEY,
    userId VARCHAR(128) NOT NULL,
    dateString VARCHAR(20) NOT NULL,
    className VARCHAR(200) NOT NULL,
    classCode VARCHAR(50),
    school VARCHAR(150),
    startTime VARCHAR(30) NOT NULL,
    endTime VARCHAR(30) NOT NULL,
    room VARCHAR(50),
    instructor VARCHAR(150),
    colorType VARCHAR(30) NOT NULL,
    FOREIGN KEY (userId) REFERENCES tblUsers(id) ON DELETE CASCADE
);

-- 3. TASKS TABLE
CREATE TABLE tblTasks (
    id VARCHAR(128) PRIMARY KEY,
    userId VARCHAR(128) NOT NULL,
    scheduleId VARCHAR(128),
    title TEXT NOT NULL,
    dateString VARCHAR(100) NOT NULL,
    courseName VARCHAR(150),
    priority VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (userId) REFERENCES tblUsers(id) ON DELETE CASCADE,
    FOREIGN KEY (scheduleId) REFERENCES tblSchedules(id) ON DELETE SET NULL
);

-- 4. AI QUIZZES TABLE
CREATE TABLE tblAiQuizzes (
    id VARCHAR(128) PRIMARY KEY,
    userId VARCHAR(128) NOT NULL,
    userEmail VARCHAR(255),
    topic VARCHAR(250) NOT NULL,
    score INT NOT NULL,
    totalQuestions INT NOT NULL,
    percentage INT NOT NULL,
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (userId) REFERENCES tblUsers(id) ON DELETE CASCADE
);

-- 5. ANNOUNCEMENTS TABLE
CREATE TABLE tblAnnouncements (
    id VARCHAR(128) PRIMARY KEY,
    adminId VARCHAR(128) NOT NULL,
    message TEXT NOT NULL,
    author VARCHAR(100) NOT NULL DEFAULT 'ADMIN',
    postedAt BIGINT NOT NULL,
    FOREIGN KEY (adminId) REFERENCES tblUsers(id) ON DELETE CASCADE
);

-- 6. FOCUS SESSIONS TABLE
CREATE TABLE tblFocusSessions (
    id VARCHAR(128) PRIMARY KEY,
    userId VARCHAR(128) NOT NULL,
    durationMinutes INT NOT NULL,
    completedAt BIGINT NOT NULL,
    FOREIGN KEY (userId) REFERENCES tblUsers(id) ON DELETE CASCADE
);

-- =========================================================================
-- SAMPLE SEED DATA
-- =========================================================================

INSERT INTO tblUsers (id, email, fullName, studentId, school, course, year, section, role, status)
VALUES ('admin-uid-001', 'admin@eskwela.com', 'System Administrator', 'ADM-0001', 'ESKWELA University', 'Administration', 'Admin', 'ADMIN-01', 'admin', 'active');

INSERT INTO tblUsers (id, email, fullName, studentId, school, course, year, section, role, status)
VALUES ('student-uid-101', 'student@eskwela.com', 'Juan Dela Cruz', 'STU-4921', 'University of the Philippines', 'BS Information Technology', '4th Year', 'IT41C1', 'student', 'active');

INSERT INTO tblSchedules (id, userId, dateString, className, classCode, school, startTime, endTime, room, instructor, colorType)
VALUES
('sch-01', 'student-uid-101', '2025-05-12', 'CAPSTONE PROJECT 1', 'IT6398', 'University', '3:00 PM', '4:30 PM', '301', '3 Units · Sec IT41C1', 'BLUE'),
('sch-02', 'student-uid-101', '2025-05-12', 'LOAD TESTING', 'CS6303', 'University', '12:00 PM', '2:30 PM', 'SLAB5', '3 Units · Sec IT41C2', 'ORANGE'),
('sch-03', 'student-uid-101', '2025-05-12', 'SOFTWARE ENGINEERING', 'CS6209', 'University', '4:30 PM', '5:30 PM', '204', '3 Units · Sec IT41C6', 'GREEN');

INSERT INTO tblTasks (id, userId, scheduleId, title, dateString, courseName, priority, completed)
VALUES
('tsk-01', 'student-uid-101', 'sch-01', 'Submit Chapter 1 Draft', 'Today · 11:59 PM', 'CAPSTONE PROJECT 1', 'HIGH', FALSE),
('tsk-02', 'student-uid-101', 'sch-02', 'JMeter Load Test Script', 'Tomorrow · 5:00 PM', 'LOAD TESTING', 'MEDIUM', FALSE);

INSERT INTO tblAiQuizzes (id, userId, userEmail, topic, score, totalQuestions, percentage, timestamp)
VALUES
('quiz-01', 'student-uid-101', 'student@eskwela.com', 'LOAD TESTING', 10, 10, 100, 1715000000000);

INSERT INTO tblAnnouncements (id, adminId, message, author, postedAt)
VALUES
('ann-01', 'admin-uid-001', 'Welcome to ESKWELA! Midterm examinations start next week. Please check your exam schedule in the app.', 'ADMIN', 1715000000000);
