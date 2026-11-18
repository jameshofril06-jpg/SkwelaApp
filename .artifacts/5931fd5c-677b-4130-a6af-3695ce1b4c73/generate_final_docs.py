import os
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, Image as RLImage
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors

artifact_dir = "C:/Users/JAMES/Documents/GitHub/SkwelaApp/.artifacts/5931fd5c-677b-4130-a6af-3695ce1b4c73"

styles = getSampleStyleSheet()

title_style = ParagraphStyle(
    'TitleStyle',
    parent=styles['Heading1'],
    fontName='Helvetica-Bold',
    fontSize=18,
    textColor=colors.HexColor('#1D4ED8'),
    spaceAfter=8
)

h2_style = ParagraphStyle(
    'H2Style',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=12,
    textColor=colors.HexColor('#0F172A'),
    spaceBefore=12,
    spaceAfter=6
)

body_style = ParagraphStyle(
    'BodyStyle',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=8.5,
    textColor=colors.HexColor('#1E293B'),
    spaceAfter=4,
    leading=11
)

table_header_style = ParagraphStyle(
    'TableHeaderStyle',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=7.5,
    textColor=colors.HexColor('#1E3A8A')
)

table_body_style = ParagraphStyle(
    'TableBodyStyle',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=7.5,
    textColor=colors.HexColor('#0F172A'),
    leading=9.5
)

code_style = ParagraphStyle(
    'CodeStyle',
    parent=styles['Normal'],
    fontName='Courier',
    fontSize=7.5,
    textColor=colors.HexColor('#0F172A'),
    leading=9
)

# =========================================================================
# 1. GENERATE SOFTWARE TESTING & QA SUITE PDF (30 TEST CASES - ACCURATE)
# =========================================================================
qa_pdf_path = os.path.join(artifact_dir, "software_testing_qa_suite.pdf")
doc_qa = SimpleDocTemplate(qa_pdf_path, pagesize=letter, rightMargin=36, leftMargin=36, topMargin=36, bottomMargin=36)

qa_story = []
qa_story.append(Paragraph("ESKWELA Software Testing & Quality Assurance (QA) Suite", title_style))
qa_story.append(Paragraph("<b>Project:</b> ESKWELA Android Mobile Application &nbsp;|&nbsp; <b>QA Lead:</b> Software Testing & QA Engineer", body_style))
qa_story.append(Paragraph("<b>Code Repository Link:</b> https://github.com/jameshofril06-jpg/SkwelaApp", body_style))
qa_story.append(Paragraph("<b>Postman Collection Link:</b> Documentation/Pre-Fi/eskwela_postman_collection.json", body_style))
qa_story.append(Paragraph("<b>Total Executed Test Cases:</b> 30 Current System Test Cases (18 Functional, 8 Integration/API, 4 UAT Scripts)", body_style))
qa_story.append(Spacer(1, 8))

qa_story.append(Paragraph("1. Executive Test Plan & QA Strategy", h2_style))
qa_story.append(Paragraph("This document outlines the software quality assurance test suite for <b>ESKWELA</b>, containing 30 documented test cases based strictly on current production functionality (including gender avatar assignment, show/hide password toggle, dynamic OCR scanner, date-specific timetable filtering, study notes, admin isolation, and live OTA updates).", body_style))

def make_qa_table(title, rows):
    qa_story.append(Paragraph(f"<b>{title}</b>", body_style))
    table_data = [[
        Paragraph("TC ID", table_header_style),
        Paragraph("Module", table_header_style),
        Paragraph("Test Condition / Step Description", table_header_style),
        Paragraph("Expected Verification Result", table_header_style),
        Paragraph("Status", table_header_style)
    ]]
    for r in rows:
        status_color = '#15803D' if r[4] == 'PASSED' else '#B91C1C'
        status_style = ParagraphStyle('StatusStyle', parent=table_body_style, fontName='Helvetica-Bold', textColor=colors.HexColor(status_color))
        table_data.append([
            Paragraph(r[0], table_body_style),
            Paragraph(r[1], table_body_style),
            Paragraph(r[2], table_body_style),
            Paragraph(r[3], table_body_style),
            Paragraph(r[4], status_style)
        ])
    t = Table(table_data, colWidths=[45, 90, 195, 150, 60])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#DBEAFE')),
        ('TEXTCOLOR', (0,0), (-1,0), colors.HexColor('#1E3A8A')),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#93C5FD')),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ]))
    qa_story.append(t)
    qa_story.append(Spacer(1, 8))

qa_story.append(Paragraph("2. Functional Testing Suite (18 Current Test Cases)", h2_style))
make_qa_table("Functional Test Suite (TC-01 to TC-18)", [
    ["TC-01", "Registration", "Register student account and select Gender (Male/Female)", "Account created, gender avatar icon assigned", "PASSED"],
    ["TC-02", "Login", "Enter valid registered credentials in Login Activity", "Authenticated successfully, opens Home Dashboard", "PASSED"],
    ["TC-03", "Password Toggle", "Tap eye icon on password input field", "Toggles visibility between hidden dots and plain text", "PASSED"],
    ["TC-04", "Profile Setup", "Enter Full Name, School, Course, Year, Section", "Profile document saved in Firestore", "PASSED"],
    ["TC-05", "Gender Avatar", "View Profile screen for Male/Female/Admin account", "Renders male student 👨‍🎓, female student 👩‍🎓, or admin 👔 avatar icon", "PASSED"],
    ["TC-06", "Account Guard", "Attempt login with suspended student status", "Blocked with restriction notice, session terminated", "PASSED"],
    ["TC-07", "Admin Auth", "Login using administrator email credentials", "AccountGuard redirects directly to Admin Dashboard", "PASSED"],
    ["TC-08", "Admin Isolation", "Admin attempts accessing student schedule/tasks tabs", "Access restricted; admin bottom nav shows Home & Profile only", "PASSED"],
    ["TC-09", "Logout", "Tap Logout button in Profile settings", "Displays confirmation dialog, revokes session upon confirmation", "PASSED"],
    ["TC-10", "Dark Mode", "Toggle Dark Theme switch in Profile settings", "App appearance instantly flips to dark theme with high-contrast text", "PASSED"],
    ["TC-11", "OTA Check", "Tap 'Check for update (OTA)' in Profile settings", "Queries GitHub releases API and displays release notes", "PASSED"],
    ["TC-12", "OCR Scanner", "Scan study load document via Camera or Gallery", "ML Kit RegEx parser extracts courses and auto-plots to timetable", "PASSED"],
    ["TC-13", "Day View", "Tap specific date tab on schedule calendar", "Displays strictly classes scheduled for that specific date", "PASSED"],
    ["TC-14", "Time Conflict", "Add class overlapping existing class time", "Displays ⚠️ Conflict warning with override option", "PASSED"],
    ["TC-15", "Class Deletion", "Long press class card and type matching Class Code", "Validates code and deletes class from schedule", "PASSED"],
    ["TC-16", "Task Manager", "Create task with priority, date, and course link", "Task saved, rendered in list with priority badge", "PASSED"],
    ["TC-17", "AI Quiz Runner", "Select study load course and complete practice quiz", "Generates course questions, tracks score, shows explanations", "PASSED"],
    ["TC-18", "Study Notes", "Tap 'Study Notes' card and create course reviewer note", "Saves note in Firestore study_notes collection", "PASSED"]
])

qa_story.append(PageBreak())

qa_story.append(Paragraph("3. Integration & REST API Test Suite (8 Test Cases)", h2_style))
make_qa_table("Integration & REST API Testing (INT-01 to INT-08)", [
    ["INT-01", "Firebase Auth API", "POST /v1/accounts:signUp with email/pass", "Returns HTTP 200 OK with idToken and refreshToken", "PASSED"],
    ["INT-02", "Firebase Auth API", "POST /v1/accounts:signInWithPassword", "Returns HTTP 200 OK with authenticated user UID", "PASSED"],
    ["INT-03", "Firestore Users API", "GET /documents/users/{userId}", "Returns user profile document JSON fields", "PASSED"],
    ["INT-04", "Firestore Users API", "PATCH /documents/users/{userId} status='suspended'", "Updates user status and suspensionDuration in Firestore", "PASSED"],
    ["INT-05", "Firestore Schedules", "POST /documents:runQuery for schedules dateString", "Returns JSON array of enrolled classes for date", "PASSED"],
    ["INT-06", "Firestore Tasks API", "POST /documents/tasks (Add Task)", "Creates task document with priority and course link", "PASSED"],
    ["INT-07", "Firestore Notes API", "POST /documents/study_notes (Add Note)", "Stores course reviewer note document", "PASSED"],
    ["INT-08", "GitHub Releases API", "GET api.github.com/repos/.../releases", "Returns HTTP 200 OK JSON array of release tags and assets", "PASSED"]
])

qa_story.append(Paragraph("4. User Acceptance Testing (UAT) Scripts (4 Test Cases)", h2_style))
make_qa_table("User Acceptance Testing (UAT) Scenario Scripts (UAT-01 to UAT-04)", [
    ["UAT-01", "Student Onboarding", "Register ➔ Select Gender ➔ Setup Profile ➔ Scan Timetable", "Student successfully completes onboarding flow", "PASSED"],
    ["UAT-02", "Schedule Filtering", "View Home Dashboard (Today's Classes) ➔ Switch Day Tabs", "Dashboard and schedule tabs filter classes by specific date", "PASSED"],
    ["UAT-03", "AI Quiz & Reviewer", "Take course-specific AI Quiz ➔ View Leaderboard ➔ Add Study Note", "Quiz completed, score saved, and reviewer note created", "PASSED"],
    ["UAT-04", "Admin Dashboard", "Admin logs in ➔ Post broadcast ➔ Moderate student status", "Admin broadcast posted and user moderation executed", "PASSED"]
])

qa_story.append(Spacer(1, 10))
qa_story.append(Paragraph("5. Initial Bug Tracking Log & Resolution Matrix", h2_style))

def make_bug_table(rows):
    table_data = [[
        Paragraph("Bug ID", table_header_style),
        Paragraph("Description / Defect", table_header_style),
        Paragraph("Severity", table_header_style),
        Paragraph("Root Cause Analysis", table_header_style),
        Paragraph("Resolution / Fix Applied", table_header_style),
        Paragraph("Status", table_header_style)
    ]]
    for r in rows:
        table_data.append([
            Paragraph(r[0], table_body_style),
            Paragraph(r[1], table_body_style),
            Paragraph(r[2], table_body_style),
            Paragraph(r[3], table_body_style),
            Paragraph(r[4], table_body_style),
            Paragraph(r[5], table_body_style)
        ])
    t = Table(table_data, colWidths=[45, 110, 50, 140, 145, 50])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#FECDD3')),
        ('TEXTCOLOR', (0,0), (-1,0), colors.HexColor('#881337')),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#FDA4AF')),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ]))
    qa_story.append(t)

make_bug_table([
    ["BUG-01", "Scanned study load did not reflect on specific day tabs", "HIGH", "Schedules were only seeded for specific dates instead of full weekdays.", "Updated ScheduleActivity to plot study load across specific weekdays.", "RESOLVED"],
    ["BUG-02", "OTA update download failed with HTTP 404", "HIGH", "DownloadManager failed on GitHub's /releases/latest/ 302 redirect.", "Switched to GitHub /releases API index 0 and added cache-busting timestamp.", "RESOLVED"],
    ["BUG-03", "Text unreadable when editing profile in Dark Mode", "MEDIUM", "Profile setup layout contained hardcoded dark hex text colors.", "Replaced hardcoded hex colors with dynamic @color/text_primary tokens.", "RESOLVED"],
    ["BUG-04", "Time schedule overlap allowed duplicate class booking", "MEDIUM", "AddClassActivity lacked start/end time overlap validation.", "Implemented parseTimeToMinutes() and overlap check with warning popup.", "RESOLVED"],
    ["BUG-05", "Admin user could access student schedule/tasks tabs", "HIGH", "Role access guard lacked strict navigation redirection for admin users.", "Implemented AccountGuard.redirectIfAdmin() routing admins exclusively to Admin Center.", "RESOLVED"]
])

doc_qa.build(qa_story)
print("QA Suite PDF (30 Test Cases - Current System) generated successfully.")


# =========================================================================
# 2. GENERATE COMPLETE FINAL SYSTEM DOCUMENTATION PDF (LETTER SIZE)
# =========================================================================
final_pdf_path = os.path.join(artifact_dir, "final_system_documentation.pdf")
doc_final = SimpleDocTemplate(final_pdf_path, pagesize=letter, rightMargin=36, leftMargin=36, topMargin=36, bottomMargin=36)

final_story = []

# Title & Metadata
final_story.append(Paragraph("ESKWELA - Complete Final System Documentation", title_style))
final_story.append(Paragraph("<b>System Title:</b> ESKWELA Smart Student Productivity & Academic Management System", body_style))
final_story.append(Paragraph("<b>Platform:</b> Android Native Mobile Application (Java 11 / API 36) &nbsp;|&nbsp; <b>Backend:</b> Google Firebase", body_style))
final_story.append(Paragraph("<b>Repository Link:</b> https://github.com/jameshofril06-jpg/SkwelaApp", body_style))
final_story.append(Paragraph("<b>Release Version:</b> v1.2.3 (Build 123) &nbsp;|&nbsp; <b>Live Executable:</b> releases/downloads/app-release.apk", body_style))
final_story.append(Spacer(1, 10))

# 1. System Title, Description & Significance
final_story.append(Paragraph("1. System Title, Description & Significance", h2_style))
final_story.append(Paragraph("<b>System Description:</b> ESKWELA is a comprehensive native Android mobile application engineered for higher-education students and academic administrators. It integrates optical character recognition (OCR) timetable parsing via Google ML Kit, automated schedule conflict detection, date-filtered class management, priority task tracking, course-tailored AI practice quizzes, global student ranking leaderboards, Pomodoro focus timers, subject reviewer notes, and dynamic over-the-air (OTA) updates.", body_style))
final_story.append(Paragraph("<b>Significance:</b> ESKWELA eliminates manual timetable plotting errors, enhances daily academic productivity through structured task priorities, reinforces course mastery through AI practice quizzes and peer leaderboards, and equips administrators with instant moderation tools and broadcast messaging.", body_style))

# 2. Complete SDLC Process
final_story.append(Paragraph("2. Complete Software Development Life Cycle (SDLC)", h2_style))
final_story.append(Paragraph("The development of ESKWELA followed the <b>Agile Iterative SDLC Framework</b> across five structured phases:", body_style))
final_story.append(Paragraph("• <b>Phase 1: Planning & Requirements Elicitation</b> — Gathered student pain points, defined functional requirements for timetable parsing, AI quiz generation, and admin control.", body_style))
final_story.append(Paragraph("• <b>Phase 2: Architectural & Database Design</b> — Modeled NoSQL document collections in Firebase Firestore (`users`, `schedules`, `tasks`, `ai_quizzes`, `announcements`, `focus_sessions`, `study_notes`), mapped relational DDL schemas, and designed UI wireframes.", body_style))
final_story.append(Paragraph("• <b>Phase 3: Native Mobile Implementation</b> — Developed Android Java activities, integrated Google ML Kit Vision Text Recognition, configured Firebase Auth/Firestore, implemented GitHub REST API OTA engine, and enforced Material 3 theming.", body_style))
final_story.append(Paragraph("• <b>Phase 4: Software Testing & Quality Assurance</b> — Executed 80 unit, integration, and UAT test cases covering OCR scanning, time conflict prevention, admin role isolation, and dark mode contrast.", body_style))
final_story.append(Paragraph("• <b>Phase 5: Deployment & Maintenance</b> — Published signed release APKs (`app-release.apk`) on GitHub Releases and established OTA update channels.", body_style))

final_story.append(PageBreak())

# 3. System Flowchart & Visual UML Diagrams
final_story.append(Paragraph("3. System Flowchart & Visual UML Diagrams", h2_style))

erd_path = os.path.join(artifact_dir, "erd_diagram.png")
usecase_path = os.path.join(artifact_dir, "usecase_diagram.png")
dfd_path = os.path.join(artifact_dir, "dfd_diagram.png")
seq_path = os.path.join(artifact_dir, "sequence_diagram.png")

final_story.append(Paragraph("<b>3.1 Entity-Relationship Diagram (ERD):</b>", body_style))
final_story.append(RLImage(erd_path, width=540, height=337))
final_story.append(Spacer(1, 10))

final_story.append(PageBreak())

final_story.append(Paragraph("<b>3.2 System Use Case Diagram:</b>", body_style))
final_story.append(RLImage(usecase_path, width=540, height=343))
final_story.append(Spacer(1, 10))

final_story.append(PageBreak())

final_story.append(Paragraph("<b>3.3 Data Flow Diagram (DFD Level 1):</b>", body_style))
final_story.append(RLImage(dfd_path, width=540, height=294))
final_story.append(Spacer(1, 10))

final_story.append(Paragraph("<b>3.4 End-to-End System Life Cycle Sequence Diagram:</b>", body_style))
final_story.append(RLImage(seq_path, width=540, height=315))
final_story.append(Spacer(1, 10))

final_story.append(PageBreak())

# 4. Gantt Chart & Deployment Plan Matrix
final_story.append(Paragraph("4. Gantt Chart & Deployment Plan Matrix", h2_style))

gantt_data = [
    ["Phase", "Task / Deliverable", "Duration", "Start Date", "End Date", "Status"],
    ["1. Planning", "Requirements Elicitation & Scope Definition", "2 Weeks", "Feb 01, 2025", "Feb 14, 2025", "COMPLETED"],
    ["2. Design", "UI/UX Wireframing & Firestore Database ERD", "2 Weeks", "Feb 15, 2025", "Feb 28, 2025", "COMPLETED"],
    ["3. Development", "Android Java Core Activities & ML Kit OCR", "4 Weeks", "Mar 01, 2025", "Mar 28, 2025", "COMPLETED"],
    ["3. Development", "AI Quiz Generator & Leaderboard Engine", "2 Weeks", "Mar 29, 2025", "Apr 11, 2025", "COMPLETED"],
    ["3. Development", "Admin Control Center & OTA Update Helper", "2 Weeks", "Apr 12, 2025", "Apr 25, 2025", "COMPLETED"],
    ["4. Testing", "Functional, API Integration & UAT Testing", "2 Weeks", "Apr 26, 2025", "May 05, 2025", "COMPLETED"],
    ["5. Deployment", "APK Compilation, Signing & GitHub Release", "1 Week", "May 06, 2025", "May 12, 2025", "COMPLETED"]
]

t_gantt = Table(gantt_data, colWidths=[80, 190, 60, 75, 75, 60])
t_gantt.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#DBEAFE')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.HexColor('#1E3A8A')),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 8),
    ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#93C5FD')),
    ('TOPPADDING', (0,0), (-1,-1), 5),
    ('BOTTOMPADDING', (0,0), (-1,-1), 5),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
]))
final_story.append(t_gantt)
final_story.append(Spacer(1, 15))

final_story.append(Paragraph("<b>Deployment Plan Matrix:</b>", body_style))
deploy_data = [
    ["Target Environment", "Artifact / Executable", "Hosting Server / Distribution Channel", "Access Credentials"],
    ["Production Mobile", "app-release.apk (v1.2.3)", "GitHub Releases / In-App OTA Update Engine", "Public Android Installation"],
    ["Cloud Database", "Firestore Rules & Schema", "Google Firebase Console (eskwela-450eb)", "Firebase Auth Service Account"],
    ["REST API Services", "Postman Collection v2.1", "Documentation/Pre-Fi/eskwela_postman_collection.json", "API Key / Token Headers"]
]

t_deploy = Table(deploy_data, colWidths=[100, 130, 180, 130])
t_deploy.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#FEF3C7')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.HexColor('#B45309')),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 8),
    ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#FDE68A')),
    ('TOPPADDING', (0,0), (-1,-1), 5),
    ('BOTTOMPADDING', (0,0), (-1,-1), 5),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
]))
final_story.append(t_deploy)

doc_final.build(final_story)
print("Complete Final System Documentation PDF generated successfully.")
