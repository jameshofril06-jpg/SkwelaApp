import os
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, Preformatted
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors

pdf_path = "C:/Users/JAMES/Documents/GitHub/SkwelaApp/.artifacts/5931fd5c-677b-4130-a6af-3695ce1b4c73/database_documentation.pdf"
doc = SimpleDocTemplate(pdf_path, pagesize=letter, rightMargin=40, leftMargin=40, topMargin=40, bottomMargin=40)
styles = getSampleStyleSheet()

title_style = ParagraphStyle(
    'TitleStyle',
    parent=styles['Heading1'],
    fontName='Helvetica-Bold',
    fontSize=22,
    textColor=colors.HexColor('#1D4ED8'),
    spaceAfter=12
)

h2_style = ParagraphStyle(
    'H2Style',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=14,
    textColor=colors.HexColor('#0F172A'),
    spaceBefore=16,
    spaceAfter=8
)

body_style = ParagraphStyle(
    'BodyStyle',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=10,
    textColor=colors.HexColor('#1E293B'),
    spaceAfter=8,
    leading=14
)

code_style = ParagraphStyle(
    'CodeStyle',
    parent=styles['Normal'],
    fontName='Courier',
    fontSize=8,
    textColor=colors.HexColor('#0F172A'),
    spaceAfter=4,
    leading=10
)

story = []

# Title
story.append(Paragraph("ESKWELA Database Documentation & Technical Specification", title_style))
story.append(Paragraph("<b>Role:</b> Database Manager & Engineer<br/><b>Project:</b> ESKWELA – Smart Student Productivity & Academic Management System", body_style))
story.append(Spacer(1, 10))

# 1. Executive Summary
story.append(Paragraph("1. Executive Summary", h2_style))
story.append(Paragraph("This document provides the complete database architecture, data dictionary, entity-relationship models, UML design specifications, and DDL SQL schema for ESKWELA. The database is designed for high performance, offline-first Firestore persistence, secure role-based access control, and scalable academic tracking.", body_style))

# 2. Data Dictionary
story.append(Paragraph("2. Data Dictionary", h2_style))

def make_table(data):
    t = Table(data, colWidths=[90, 80, 50, 145, 165])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#F1F5F9')),
        ('TEXTCOLOR', (0,0), (-1,0), colors.HexColor('#0F172A')),
        ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
        ('FONTSIZE', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 6),
        ('TOPPADDING', (0,0), (-1,-1), 6),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#CBD5E1')),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ]))
    return t

# Users table
story.append(Paragraph("<b>Table 1: users</b>", body_style))
users_data = [
    ["Column", "Type", "Length", "Constraints", "Description"],
    ["id", "VARCHAR", "128", "PRIMARY KEY", "Firebase Auth UID"],
    ["email", "VARCHAR", "255", "UNIQUE, NOT NULL", "Login email address"],
    ["full_name", "VARCHAR", "150", "NULLABLE", "Student/admin full name"],
    ["student_id", "VARCHAR", "50", "UNIQUE", "Student ID card number"],
    ["school", "VARCHAR", "200", "NULLABLE", "University name"],
    ["course", "VARCHAR", "150", "NULLABLE", "Degree program"],
    ["role", "user_role", "-", "DEFAULT 'student'", "Role: student or admin"],
    ["status", "user_status", "-", "DEFAULT 'active'", "Status: active, suspended, etc."]
]
story.append(make_table(users_data))
story.append(Spacer(1, 10))

# Schedules table
story.append(Paragraph("<b>Table 2: schedules</b>", body_style))
schedules_data = [
    ["Column", "Type", "Length", "Constraints", "Description"],
    ["id", "VARCHAR", "128", "PRIMARY KEY", "Unique schedule ID"],
    ["user_id", "VARCHAR", "128", "FOREIGN KEY", "Owner user ID"],
    ["class_date", "DATE", "-", "NOT NULL", "Scheduled date"],
    ["class_name", "VARCHAR", "200", "NOT NULL", "Course title"],
    ["class_code", "VARCHAR", "50", "NOT NULL", "Course code (e.g. IT6398)"],
    ["start_time", "TIME", "-", "NOT NULL", "Start time"],
    ["end_time", "TIME", "-", "NOT NULL", "End time"],
    ["room", "VARCHAR", "50", "NULLABLE", "Room / Lab number"]
]
story.append(make_table(schedules_data))
story.append(Spacer(1, 10))

story.append(PageBreak())

# Tasks table
story.append(Paragraph("<b>Table 3: tasks</b>", body_style))
tasks_data = [
    ["Column", "Type", "Length", "Constraints", "Description"],
    ["id", "VARCHAR", "128", "PRIMARY KEY", "Unique task ID"],
    ["user_id", "VARCHAR", "128", "FOREIGN KEY", "Owner user ID"],
    ["schedule_id", "VARCHAR", "128", "FOREIGN KEY", "Linked class schedule"],
    ["title", "TEXT", "-", "NOT NULL", "Task title"],
    ["due_date", "TIMESTAMPTZ", "-", "NOT NULL", "Deadline"],
    ["priority", "task_priority", "-", "DEFAULT 'MEDIUM'", "HIGH, MEDIUM, LOW"],
    ["completed", "BOOLEAN", "-", "DEFAULT FALSE", "Completion flag"]
]
story.append(make_table(tasks_data))
story.append(Spacer(1, 10))

# AI Quizzes table
story.append(Paragraph("<b>Table 4: ai_quizzes</b>", body_style))
quizzes_data = [
    ["Column", "Type", "Length", "Constraints", "Description"],
    ["id", "VARCHAR", "128", "PRIMARY KEY", "Unique quiz attempt ID"],
    ["user_id", "VARCHAR", "128", "FOREIGN KEY", "User ID"],
    ["topic", "VARCHAR", "250", "NOT NULL", "Quiz topic / course"],
    ["score", "INT", "-", "NOT NULL", "Correct answers count"],
    ["total_questions", "INT", "-", "NOT NULL", "Total questions (5-15)"],
    ["percentage", "NUMERIC", "5,2", "GENERATED STORED", "Score percentage"]
]
story.append(make_table(quizzes_data))
story.append(Spacer(1, 10))

# Announcements table
story.append(Paragraph("<b>Table 5: announcements</b>", body_style))
ann_data = [
    ["Column", "Type", "Length", "Constraints", "Description"],
    ["id", "VARCHAR", "128", "PRIMARY KEY", "Unique announcement ID"],
    ["admin_id", "VARCHAR", "128", "FOREIGN KEY", "Admin author ID"],
    ["message", "TEXT", "-", "NOT NULL", "Broadcast message"],
    ["created_at", "TIMESTAMPTZ", "-", "DEFAULT NOW()", "Timestamp posted"]
]
story.append(make_table(ann_data))
story.append(Spacer(1, 10))

# Focus Sessions table
story.append(Paragraph("<b>Table 6: focus_sessions</b>", body_style))
focus_data = [
    ["Column", "Type", "Length", "Constraints", "Description"],
    ["id", "VARCHAR", "128", "PRIMARY KEY", "Unique session ID"],
    ["user_id", "VARCHAR", "128", "FOREIGN KEY", "User ID"],
    ["duration_minutes", "INT", "-", "NOT NULL", "Duration (15, 25, 45, 60)"],
    ["created_at", "TIMESTAMPTZ", "-", "DEFAULT NOW()", "Completion timestamp"]
]
story.append(make_table(focus_data))
story.append(Spacer(1, 15))

story.append(PageBreak())

# 3. ERD & Relationships
story.append(Paragraph("3. Entity-Relationship Diagram (ERD) & Cardinality", h2_style))
story.append(Paragraph("• <b>USERS to SCHEDULES</b> (1 : N): One user has zero or many scheduled classes.", body_style))
story.append(Paragraph("• <b>USERS to TASKS</b> (1 : N): One user manages zero or many tasks.", body_style))
story.append(Paragraph("• <b>USERS to AI_QUIZZES</b> (1 : N): One user takes zero or many AI quizzes, contributing to global leaderboard rankings.", body_style))
story.append(Paragraph("• <b>USERS to FOCUS_SESSIONS</b> (1 : N): One user records zero or many Pomodoro focus sessions.", body_style))
story.append(Paragraph("• <b>ANNOUNCEMENTS</b>: Standalone entity managed exclusively by admin users for global student broadcast.", body_style))

# 4. UML & Use Case
story.append(Paragraph("4. UML & Use Case Specifications", h2_style))
story.append(Paragraph("<b>Use Cases:</b><br/>- <b>Student:</b> Register/Login, scan study load via OCR camera/gallery, view schedule (Day/Week/Month), manage tasks, take smart AI quizzes (5-15 Qs), view live leaderboard rankings, run focus timer, toggle dark theme and online auto-sync.<br/>- <b>Administrator:</b> Authenticate admin account, access Admin Control Center, search students, manage user accounts (timed suspension, activation/deactivation, archiving), broadcast official announcements.", body_style))

# 5. SQL DDL Script
story.append(Paragraph("5. Raw SQL Schema Script (schema.sql)", h2_style))
sql_text = """CREATE TYPE user_role AS ENUM ('student', 'admin');
CREATE TYPE user_status AS ENUM ('active', 'pending', 'suspended', 'deactivated', 'archived');
CREATE TYPE task_priority AS ENUM ('HIGH', 'MEDIUM', 'LOW');

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
    suspended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ
);

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
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE tasks (
    id VARCHAR(128) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    schedule_id VARCHAR(128),
    title TEXT NOT NULL,
    due_date TIMESTAMPTZ NOT NULL,
    course_name VARCHAR(150),
    priority task_priority NOT NULL DEFAULT 'MEDIUM',
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE SET NULL
);

CREATE TABLE ai_quizzes (
    id VARCHAR(128) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    topic VARCHAR(250) NOT NULL,
    score INT NOT NULL CHECK (score >= 0),
    total_questions INT NOT NULL CHECK (total_questions > 0),
    percentage NUMERIC(5,2) GENERATED ALWAYS AS (ROUND((score::NUMERIC / total_questions::NUMERIC) * 100, 2)) STORED,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE announcements (
    id VARCHAR(128) PRIMARY KEY,
    admin_id VARCHAR(128) NOT NULL,
    school_scope VARCHAR(200),
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE focus_sessions (
    id VARCHAR(128) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    duration_minutes INT NOT NULL CHECK (duration_minutes > 0),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);"""

for line in sql_text.split('\n'):
    story.append(Paragraph(line, code_style))

doc.build(story)
print("PDF generated successfully.")
