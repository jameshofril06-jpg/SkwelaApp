import os
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors

artifact_dir = "C:/Users/JAMES/Documents/GitHub/SkwelaApp/.artifacts/5931fd5c-677b-4130-a6af-3695ce1b4c73"
pdf_path = os.path.join(artifact_dir, "software_testing_qa_suite.pdf")

doc = SimpleDocTemplate(pdf_path, pagesize=letter, rightMargin=36, leftMargin=36, topMargin=36, bottomMargin=36)
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

story = []

# Title Banner
story.append(Paragraph("ESKWELA Software Testing & Quality Assurance (QA) Suite", title_style))
story.append(Paragraph("<b>Project:</b> ESKWELA Android Mobile Application &nbsp;|&nbsp; <b>QA Lead:</b> Software Testing & QA Engineer", body_style))
story.append(Paragraph("<b>Code Repository Link:</b> https://github.com/jameshofril06-jpg/SkwelaApp", body_style))
story.append(Paragraph("<b>Postman Collection Link:</b> Documentation/Pre-Fi/eskwela_postman_collection.json", body_style))
story.append(Paragraph("<b>Total Executed Test Cases:</b> 30 Sample Test Cases (18 Functional, 8 Integration/API, 4 UAT Scripts)", body_style))
story.append(Spacer(1, 8))

# 1. QA Strategy & Test Environment
story.append(Paragraph("1. Executive Test Plan & QA Strategy", h2_style))
story.append(Paragraph("This Pre-Final QA document outlines the software quality assurance test suite for <b>ESKWELA</b>, containing 30 documented test cases, integration API test scripts, User Acceptance Testing (UAT) scenarios, and an initial bug tracking resolution matrix. Testing verifies functional correctness across Student and Administrator workflows.", body_style))

# 2. Functional Test Cases (18 Test Cases)
story.append(Paragraph("2. Functional Testing Suite (18 Test Cases)", h2_style))

def make_qa_table(title, rows):
    story.append(Paragraph(f"<b>{title}</b>", body_style))
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
    story.append(t)
    story.append(Spacer(1, 8))

make_qa_table("Functional Test Suite (TC-01 to TC-18)", [
    ["TC-01", "Registration", "Register with valid student credentials and password", "Account created, redirected to Profile Setup", "PASSED"],
    ["TC-02", "Login", "Enter valid registered student credentials", "Authenticated successfully, opens Home Dashboard", "PASSED"],
    ["TC-03", "Login", "Tap 'Forgot Password' link and submit email", "Password reset email dispatched via Firebase", "PASSED"],
    ["TC-04", "Profile Setup", "Enter Full Name, School, Course, Year, Section", "Profile stored in Firestore document", "PASSED"],
    ["TC-05", "Profile Picture", "Tap avatar circle to pick custom profile picture", "Image selected from gallery and saved to profile", "PASSED"],
    ["TC-06", "Password Toggle", "Tap eye icon on password input field", "Toggles visibility between hidden dots and plain text", "PASSED"],
    ["TC-07", "Account Guard", "Attempt login with suspended student status", "Blocked with restriction notice, session terminated", "PASSED"],
    ["TC-08", "Admin Auth", "Login using administrator email credentials", "AccountGuard redirects directly to Admin Center", "PASSED"],
    ["TC-09", "Admin Isolation", "Admin attempts opening student schedule tab", "Access restricted, redirects to Admin Center (Home & Profile only)", "PASSED"],
    ["TC-10", "Logout", "Tap Logout button in Profile settings", "Displays confirmation dialog, revokes session upon confirmation", "PASSED"],
    ["TC-11", "Dark Mode", "Toggle Dark Theme switch in Profile settings", "App appearance instantly flips to dark theme with high-contrast text", "PASSED"],
    ["TC-12", "OTA Check", "Tap 'Check for update (OTA)' in Profile settings", "Queries GitHub releases API and displays release notes", "PASSED"],
    ["TC-13", "OCR Timetable", "Scan study load document via Camera or Gallery", "ML Kit extracts courses and plots across weekdays", "PASSED"],
    ["TC-14", "Day View", "Tap specific day tab on schedule calendar", "Displays strictly classes scheduled for that specific date", "PASSED"],
    ["TC-15", "Time Conflict", "Add class overlapping existing class time", "Displays ⚠️ Conflict warning with override option", "PASSED"],
    ["TC-16", "Class Deletion", "Long press class card and type matching Class Code", "Validates code and deletes class from schedule", "PASSED"],
    ["TC-17", "Task Management", "Create task with priority, due date, and course link", "Task saved, rendered in task list with priority badge", "PASSED"],
    ["TC-18", "AI Quiz Generator", "Select study load course and complete practice quiz", "Generates course questions, tracks score, shows explanations", "PASSED"]
])

story.append(PageBreak())

# 3. Integration & REST API Test Cases (8 Test Cases)
story.append(Paragraph("3. Integration & REST API Test Suite (8 Test Cases)", h2_style))

make_qa_table("Integration & REST API Testing (INT-01 to INT-08)", [
    ["INT-01", "Firebase Auth API", "POST /v1/accounts:signUp with email/pass", "Returns HTTP 200 OK with idToken and refreshToken", "PASSED"],
    ["INT-02", "Firebase Auth API", "POST /v1/accounts:signInWithPassword", "Returns HTTP 200 OK with authenticated user UID", "PASSED"],
    ["INT-03", "Firestore Users API", "GET /documents/users/{userId}", "Returns user profile document JSON fields", "PASSED"],
    ["INT-04", "Firestore Users API", "PATCH /documents/users/{userId} status='suspended'", "Updates user status and suspensionDuration in Firestore", "PASSED"],
    ["INT-05", "Firestore Schedules", "POST /documents:runQuery for schedules dateString", "Returns JSON array of enrolled classes for date", "PASSED"],
    ["INT-06", "Firestore Tasks API", "POST /documents/tasks (Add Task)", "Creates task document with priority and course link", "PASSED"],
    ["INT-07", "Firestore Quiz API", "POST /documents/ai_quizzes (Save Quiz Attempt)", "Saves score, topic, percentage, timestamp", "PASSED"],
    ["INT-08", "GitHub Releases API", "GET api.github.com/repos/.../releases", "Returns HTTP 200 OK JSON array of release tags and assets", "PASSED"]
])

# 4. User Acceptance Testing (4 Test Cases)
story.append(Paragraph("4. User Acceptance Testing (UAT) Scripts (4 Test Cases)", h2_style))

make_qa_table("User Acceptance Testing (UAT) Scenario Scripts (UAT-01 to UAT-04)", [
    ["UAT-01", "Student Onboarding", "Register ➔ Setup Profile & Avatar ➔ Scan Timetable", "Student successfully completes onboarding flow", "PASSED"],
    ["UAT-02", "Study Load & Schedule", "View Home Dashboard (Today's Classes) ➔ Switch Day Tabs", "Dashboard and schedule tabs filter classes by specific date", "PASSED"],
    ["UAT-03", "AI Quiz & Leaderboard", "Take course-specific AI Quiz ➔ View Leaderboard rankings", "Quiz completed, score saved, and leaderboard rendered", "PASSED"],
    ["UAT-04", "Admin Moderation", "Admin logs in ➔ Inspect user ➔ Suspend account ➔ Broadcast announcement", "Admin moderation executed and broadcasted successfully", "PASSED"]
])

story.append(PageBreak())

# 5. Initial Bug Tracking Log & Resolution Matrix
story.append(Paragraph("5. Initial Bug Tracking Log & Resolution Matrix", h2_style))

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
    story.append(t)
    story.append(Spacer(1, 10))

make_bug_table([
    ["BUG-01", "Scanned study load did not reflect on specific day tabs", "HIGH", "Schedules were only seeded for specific dates instead of full weekdays.", "Updated ScheduleActivity to plot study load across specific weekdays.", "RESOLVED"],
    ["BUG-02", "OTA update download failed with HTTP 404", "HIGH", "DownloadManager failed on GitHub's /releases/latest/ 302 redirect.", "Switched to GitHub /releases API index 0 and added cache-busting timestamp.", "RESOLVED"],
    ["BUG-03", "Text unreadable when editing profile in Dark Mode", "MEDIUM", "Profile setup layout contained hardcoded dark hex text colors.", "Replaced hardcoded hex colors with dynamic @color/text_primary tokens.", "RESOLVED"],
    ["BUG-04", "Time schedule overlap allowed duplicate class booking", "MEDIUM", "AddClassActivity lacked start/end time overlap validation.", "Implemented parseTimeToMinutes() and overlap check with warning popup.", "RESOLVED"],
    ["BUG-05", "Admin user could access student schedule/tasks tabs", "HIGH", "Role access guard lacked strict navigation redirection for admin users.", "Implemented AccountGuard.redirectIfAdmin() routing admins exclusively to Admin Center.", "RESOLVED"]
])

doc.build(story)
print("Pre-Final QA Suite PDF with 30 Test Cases generated successfully.")
