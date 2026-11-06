import os
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak
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
story.append(Paragraph("ESKWELA Firebase Firestore Documentation & Technical Specification", title_style))
story.append(Paragraph("<b>Role:</b> Database Manager & Engineer<br/><b>Backend Architecture:</b> Google Firebase Auth & Firestore NoSQL Document Database", body_style))
story.append(Spacer(1, 10))

# 1. Executive Summary
story.append(Paragraph("1. Executive Summary & Firebase Architecture", h2_style))
story.append(Paragraph("This document provides the technical database architecture, data dictionary, Entity-Relationship Diagram (ERD), UML diagrams, and DDL SQL export script for <b>ESKWELA</b>, strictly structured around <b>Google Firebase Firestore NoSQL</b> document collections and Firebase Auth UID references. The system provides real-time multi-device synchronization, offline local caching, and role-based security rules.", body_style))

# 2. Data Dictionary (Firebase Collections)
story.append(Paragraph("2. Firebase Firestore Data Dictionary", h2_style))

def make_table(data):
    t = Table(data, colWidths=[100, 70, 60, 135, 165])
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

# Collection: users
story.append(Paragraph("<b>Collection 1: users</b> (Document ID: Firebase Auth UID)", body_style))
users_data = [
    ["Field Name", "Type", "Format", "Constraints", "Description"],
    ["id", "String", "UID", "PRIMARY KEY", "Firebase Auth User ID"],
    ["email", "String", "Email", "UNIQUE, NOT NULL", "Login email address"],
    ["fullName", "String", "Text", "NULLABLE", "Student or administrator full name"],
    ["studentId", "String", "Text", "UNIQUE, NULLABLE", "Student ID number (e.g. STU-4921)"],
    ["school", "String", "Text", "NULLABLE", "University name"],
    ["course", "String", "Text", "NULLABLE", "Degree program / Course"],
    ["year", "String", "Text", "NULLABLE", "Year level (e.g. '4th Year')"],
    ["section", "String", "Text", "NULLABLE", "Class section code (e.g. 'IT41C1')"],
    ["role", "String", "Enum", "DEFAULT 'student'", "'student' or 'admin'"],
    ["status", "String", "Enum", "DEFAULT 'active'", "'active', 'suspended', 'deactivated', 'archived'"],
    ["suspensionDuration", "String", "Text", "NULLABLE", "Duration if suspended (e.g. '24 Hours')"],
    ["suspendedAt", "Number", "Epoch ms", "NULLABLE", "Timestamp when account was suspended"]
]
story.append(make_table(users_data))
story.append(Spacer(1, 10))

# Collection: schedules
story.append(Paragraph("<b>Collection 2: schedules</b> (Auto-generated Document ID)", body_style))
schedules_data = [
    ["Field Name", "Type", "Format", "Constraints", "Description"],
    ["id", "String", "UUID", "PRIMARY KEY", "Unique schedule document ID"],
    ["userId", "String", "UID", "FOREIGN KEY", "Owner Firebase Auth UID"],
    ["dateString", "String", "YYYY-MM-DD", "NOT NULL", "Scheduled date string"],
    ["className", "String", "Text", "NOT NULL", "Course title (e.g. 'CAPSTONE PROJECT 1')"],
    ["classCode", "String", "Text", "NULLABLE", "Course code (e.g. 'IT6398')"],
    ["school", "String", "Text", "NULLABLE", "Institution name"],
    ["startTime", "String", "h:mm a", "NOT NULL", "Class start time (e.g. '3:00 PM')"],
    ["endTime", "String", "h:mm a", "NOT NULL", "Class end time (e.g. '4:30 PM')"],
    ["room", "String", "Text", "NULLABLE", "Room location (e.g. '301', 'SLAB5')"],
    ["instructor", "String", "Text", "NULLABLE", "Units and section metadata"],
    ["colorType", "String", "Text", "NOT NULL", "UI color badge ('BLUE', 'ORANGE', 'GREEN')"]
]
story.append(make_table(schedules_data))
story.append(Spacer(1, 10))

story.append(PageBreak())

# Collection: tasks
story.append(Paragraph("<b>Collection 3: tasks</b> (Auto-generated Document ID)", body_style))
tasks_data = [
    ["Field Name", "Type", "Format", "Constraints", "Description"],
    ["id", "String", "UUID", "PRIMARY KEY", "Unique task document ID"],
    ["userId", "String", "UID", "FOREIGN KEY", "Owner Firebase Auth UID"],
    ["title", "String", "Text", "NOT NULL", "Task title / description"],
    ["dateString", "String", "Text", "NOT NULL", "Deadline display string"],
    ["courseName", "String", "Text", "NULLABLE", "Associated course title"],
    ["priority", "String", "Enum", "DEFAULT 'MEDIUM'", "Task priority ('HIGH', 'MEDIUM', 'LOW')"],
    ["completed", "Boolean", "true/false", "DEFAULT false", "Completion status flag"]
]
story.append(make_table(tasks_data))
story.append(Spacer(1, 10))

# Collection: ai_quizzes
story.append(Paragraph("<b>Collection 4: ai_quizzes</b> (Auto-generated Document ID)", body_style))
quizzes_data = [
    ["Field Name", "Type", "Format", "Constraints", "Description"],
    ["id", "String", "UUID", "PRIMARY KEY", "Unique quiz record ID"],
    ["userId", "String", "UID", "FOREIGN KEY", "User who completed the quiz"],
    ["userEmail", "String", "Email", "NULLABLE", "Email prefix used for Leaderboard ranking"],
    ["topic", "String", "Text", "NOT NULL", "Quiz course subject / topic"],
    ["score", "Number", "Integer", "NOT NULL", "Correct answers count"],
    ["totalQuestions", "Number", "Integer", "NOT NULL", "Total questions in quiz (5, 10, 15)"],
    ["percentage", "Number", "Integer", "NOT NULL", "Calculated score percentage (0-100)"],
    ["timestamp", "Number", "Epoch ms", "NOT NULL", "Quiz completion timestamp"]
]
story.append(make_table(quizzes_data))
story.append(Spacer(1, 10))

# Collection: announcements
story.append(Paragraph("<b>Collection 5: announcements</b> (Auto-generated Document ID)", body_style))
ann_data = [
    ["Field Name", "Type", "Format", "Constraints", "Description"],
    ["id", "String", "UUID", "PRIMARY KEY", "Unique announcement ID"],
    ["message", "String", "Text", "NOT NULL", "Official broadcast message"],
    ["author", "String", "Text", "DEFAULT 'ADMIN'", "Author credential tag"],
    ["postedAt", "Number", "Epoch ms", "NOT NULL", "Broadcast timestamp"]
]
story.append(make_table(ann_data))
story.append(Spacer(1, 10))

# Collection: focus_sessions
story.append(Paragraph("<b>Collection 6: focus_sessions</b> (Auto-generated Document ID)", body_style))
focus_data = [
    ["Field Name", "Type", "Format", "Constraints", "Description"],
    ["id", "String", "UUID", "PRIMARY KEY", "Unique session ID"],
    ["userId", "String", "UID", "FOREIGN KEY", "User ID"],
    ["durationMinutes", "Number", "Integer", "NOT NULL", "Duration completed (2, 5, 10, 15, 25, 45, 60)"],
    ["completedAt", "Number", "Epoch ms", "NOT NULL", "Completion timestamp"]
]
story.append(make_table(focus_data))
story.append(Spacer(1, 15))

story.append(PageBreak())

# 3. ERD & Relationships
story.append(Paragraph("3. Firebase Firestore ERD & Document Relationships", h2_style))
story.append(Paragraph("• <b>users (1) ➔ schedules (N)</b>: Linked via <code>userId</code> field.", body_style))
story.append(Paragraph("• <b>users (1) ➔ tasks (N)</b>: Linked via <code>userId</code> field.", body_style))
story.append(Paragraph("• <b>users (1) ➔ ai_quizzes (N)</b>: Linked via <code>userId</code> field (queries indexed on <code>percentage DESC</code> for global Leaderboard).", body_style))
story.append(Paragraph("• <b>users (1) ➔ focus_sessions (N)</b>: Linked via <code>userId</code> field for productivity analytics.", body_style))
story.append(Paragraph("• <b>announcements</b>: Global collection broadcasted by admins and queried in real-time on student home dashboards.", body_style))

# 4. UML & Use Case Specifications
story.append(Paragraph("4. UML Diagrams & Workflow Specifications", h2_style))
story.append(Paragraph("<b>Use Cases:</b><br/>- <b>Student:</b> Register/Login, scan study load via OCR camera/gallery, view schedule (Day/Week/Month), manage tasks & priorities, take smart AI quizzes (5-15 Qs), view live leaderboard rankings, run Pomodoro focus timer, toggle dark theme and online auto-sync.<br/>- <b>Administrator:</b> Authenticate admin account, access Admin Control Center, search students, manage user accounts (timed suspension, activation/deactivation, archiving), broadcast official announcements.", body_style))

# 5. Raw SQL Schema Export Script
story.append(Paragraph("5. Raw SQL Schema Script (schema.sql)", h2_style))
sql_text = """-- Firebase Firestore Structured Relational SQL DDL Export
DROP TABLE IF EXISTS focus_sessions CASCADE;
DROP TABLE IF EXISTS ai_quizzes CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS schedules CASCADE;
DROP TABLE IF EXISTS announcements CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
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

CREATE TABLE schedules (
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
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE tasks (
    id VARCHAR(128) PRIMARY KEY,
    userId VARCHAR(128) NOT NULL,
    title TEXT NOT NULL,
    dateString VARCHAR(100) NOT NULL,
    courseName VARCHAR(150),
    priority VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE ai_quizzes (
    id VARCHAR(128) PRIMARY KEY,
    userId VARCHAR(128) NOT NULL,
    userEmail VARCHAR(255),
    topic VARCHAR(250) NOT NULL,
    score INT NOT NULL,
    totalQuestions INT NOT NULL,
    percentage INT NOT NULL,
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE announcements (
    id VARCHAR(128) PRIMARY KEY,
    message TEXT NOT NULL,
    author VARCHAR(100) NOT NULL DEFAULT 'ADMIN',
    postedAt BIGINT NOT NULL
);

CREATE TABLE focus_sessions (
    id VARCHAR(128) PRIMARY KEY,
    userId VARCHAR(128) NOT NULL,
    durationMinutes INT NOT NULL,
    completedAt BIGINT NOT NULL,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
);"""

for line in sql_text.split('\n'):
    story.append(Paragraph(line, code_style))

doc.build(story)
print("Firebase PDF Documentation generated successfully.")
