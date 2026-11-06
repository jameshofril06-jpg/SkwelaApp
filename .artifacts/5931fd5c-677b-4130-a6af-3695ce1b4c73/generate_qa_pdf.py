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
    fontSize=20,
    textColor=colors.HexColor('#1D4ED8'),
    spaceAfter=10
)

h2_style = ParagraphStyle(
    'H2Style',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=13,
    textColor=colors.HexColor('#0F172A'),
    spaceBefore=14,
    spaceAfter=8
)

body_style = ParagraphStyle(
    'BodyStyle',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=9,
    textColor=colors.HexColor('#1E293B'),
    spaceAfter=6,
    leading=12
)

table_header_style = ParagraphStyle(
    'TableHeaderStyle',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=8,
    textColor=colors.HexColor('#1E3A8A')
)

table_body_style = ParagraphStyle(
    'TableBodyStyle',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=8,
    textColor=colors.HexColor('#0F172A'),
    leading=10
)

story = []

# Title Banner
story.append(Paragraph("ESKWELA Software Testing & Quality Assurance (QA) Suite", title_style))
story.append(Paragraph("<b>Project:</b> ESKWELA Mobile & Web System &nbsp;|&nbsp; <b>QA Lead:</b> Software Testing & QA Engineer", body_style))
story.append(Paragraph("<b>Code Repository:</b> https://github.com/jameshofril06-jpg/SkwelaApp", body_style))
story.append(Spacer(1, 10))

# 1. Test Plan & Strategy
story.append(Paragraph("1. Test Plan & QA Strategy", h2_style))
story.append(Paragraph("This QA Suite documents the test execution plan, functional test cases, integration API test scripts, User Acceptance Testing (UAT) scenarios, and bug tracking resolution matrix for <b>ESKWELA</b>. Testing covers both Student productivity modules and Administrator moderation capabilities.", body_style))

# 2. Functional Test Cases
story.append(Paragraph("2. Functional Test Cases", h2_style))

def make_qa_table(title, rows):
    story.append(Paragraph(f"<b>{title}</b>", body_style))
    table_data = [[
        Paragraph("TC ID", table_header_style),
        Paragraph("Module", table_header_style),
        Paragraph("Test Condition / Step", table_header_style),
        Paragraph("Expected Result", table_header_style),
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

    t = Table(table_data, colWidths=[55, 95, 185, 145, 60])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#DBEAFE')),
        ('TEXTCOLOR', (0,0), (-1,0), colors.HexColor('#1E3A8A')),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#93C5FD')),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ]))
    story.append(t)
    story.append(Spacer(1, 10))

make_qa_table("Functional Test Suite (Core System Modules)", [
    ["TC-01", "Authentication", "Enter valid email and password in Login Activity", "User authenticated, redirected to DashboardActivity", "PASSED"],
    ["TC-02", "Account Guard", "Attempt login with suspended/deactivated account", "Blocked with restriction notice, logged out", "PASSED"],
    ["TC-03", "Study Load OCR", "Upload study load photo via Camera or Gallery", "ML Kit extracts courses and auto-plots to timetable", "PASSED"],
    ["TC-04", "Schedule Calendar", "Switch between Day, Week, and Month views", "Filters classes accurately by date range", "PASSED"],
    ["TC-05", "Time Conflict", "Add class overlapping existing class time", "Displays ⚠️ Conflict warning with override option", "PASSED"],
    ["TC-06", "Schedule Delete", "Long press class card and enter class code", "Validates input and removes schedule from Firestore", "PASSED"],
    ["TC-07", "Task Manager", "Add new task with priority and course link", "Task saved, rendered in list with badge", "PASSED"],
    ["TC-08", "Task Drop", "Tap drop task button and enter reason", "Prompts confirmation and records drop reason", "PASSED"],
    ["TC-09", "AI Quiz Runner", "Select study load course and complete 5-15 Qs", "Generates questions, tracks score & percentage", "PASSED"],
    ["TC-10", "Leaderboard", "Complete quiz with high score percentage", "Score recorded, updates live Leaderboard rankings", "PASSED"],
    ["TC-11", "Focus Timer", "Select focus preset (2m-60m) and start timer", "Runs countdown timer, saves session upon completion", "PASSED"],
    ["TC-12", "Dark Mode", "Toggle Dark Mode switch in Profile settings", "App appearance instantly flips to dark theme", "PASSED"]
])

story.append(PageBreak())

# 3. Integration & API Test Cases
story.append(Paragraph("3. Integration & API Testing", h2_style))

make_qa_table("Integration & REST API Test Suite", [
    ["INT-01", "Firebase Auth", "Postman POST to identitytoolkit signUp / signIn", "Returns HTTP 200 OK with idToken and refreshToken", "PASSED"],
    ["INT-02", "Firestore Users", "GET document /users/student-uid-101", "Returns user profile JSON fields (fullName, status)", "PASSED"],
    ["INT-03", "Firestore Schedule", "Run query for schedules where userId == studentUid", "Returns JSON array of enrolled classes for date", "PASSED"],
    ["INT-04", "Firestore Quiz", "Run query for ai_quizzes ORDER BY percentage DESC", "Returns top 5 leaderboard scores", "PASSED"],
    ["INT-05", "GitHub OTA API", "GET api.github.com/repos/.../releases/latest", "Returns HTTP 200 OK with browser_download_url", "PASSED"]
])

# 4. User Acceptance Testing (UAT)
story.append(Paragraph("4. User Acceptance Testing (UAT) Scripts", h2_style))

make_qa_table("User Acceptance Testing (UAT) Scenario Scripts", [
    ["UAT-01", "Student Flow", "Student logs in ➔ Scans study load photo ➔ Takes AI quiz ➔ Sets Pomodoro timer", "End-to-end workflow completed seamlessly", "PASSED"],
    ["UAT-02", "Admin Flow", "Admin logs in ➔ Searches student ➔ Applies 24h suspension ➔ Posts announcement", "Suspension countdown rendered, broadcast posted", "PASSED"],
    ["UAT-03", "OTA Update Flow", "User taps 'Check for update (OTA)' in Profile settings", "Displays release notes, downloads APK, opens installer", "PASSED"]
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
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#FECDD3')), # Red light header
        ('TEXTCOLOR', (0,0), (-1,0), colors.HexColor('#881337')),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#FDA4AF')),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ]))
    story.append(t)
    story.append(Spacer(1, 10))

make_bug_table([
    ["BUG-01", "Scanned study load did not reflect on specific day tabs", "HIGH", "Schedules were only seeded for specific dates instead of full weekdays.", "Updated ScheduleActivity to plot study load across all weekdays.", "RESOLVED"],
    ["BUG-02", "OTA update download failed with HTTP 404", "HIGH", "DownloadManager failed on GitHub's /releases/latest/ 302 redirect.", "Switched to GitHub /releases API index 0 and added cache-busting timestamp.", "RESOLVED"],
    ["BUG-03", "Text unreadable when editing profile in Dark Mode", "MEDIUM", "Profile setup layout contained hardcoded dark hex text colors.", "Replaced hardcoded hex colors with dynamic @color/text_primary tokens.", "RESOLVED"],
    ["BUG-04", "Time schedule overlap allowed duplicate class booking", "MEDIUM", "AddClassActivity lacked start/end time overlap validation.", "Implemented parseTimeToMinutes() and overlap check with warning popup.", "RESOLVED"],
    ["BUG-05", "Task deletion occurred accidentally on single tap", "LOW", "No confirmation dialog before removing task records.", "Added AlertDialog requiring confirmation and drop reason prompt.", "RESOLVED"]
])

doc.build(story)
print("QA Suite PDF generated successfully.")
