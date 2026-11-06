import os
from PIL import Image, ImageDraw, ImageFont
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, Image as RLImage
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors

artifact_dir = "C:/Users/JAMES/Documents/GitHub/SkwelaApp/.artifacts/5931fd5c-677b-4130-a6af-3695ce1b4c73"

# =========================================================================
# 1. GENERATE PRECISE HIGH-RESOLUTION PNG DIAGRAMS USING PILLOW
# =========================================================================

def get_font(size, bold=False):
    try:
        font_path = "C:/Windows/Fonts/arialbd.ttf" if bold else "C:/Windows/Fonts/arial.ttf"
        return ImageFont.truetype(font_path, size)
    except:
        return ImageFont.load_default()

font_title = get_font(28, bold=True)
font_header = get_font(20, bold=True)
font_body = get_font(15, bold=False)
font_bold = get_font(15, bold=True)
font_small = get_font(13, bold=False)

# -------------------------------------------------------------------------
# Diagram 1: Clean, Non-Overlapping ERD Diagram (erd_diagram.png)
# -------------------------------------------------------------------------
img_erd = Image.new('RGB', (2400, 1500), color='#F8FAFC')
draw = ImageDraw.Draw(img_erd)

# Title Banner
draw.rectangle([0, 0, 2400, 100], fill='#1D4ED8')
draw.text((60, 30), "ESKWELA - Clean Field-to-Field ERD (Zero Line Crossings)", fill='#FFFFFF', font=font_title)

# Draw Table Box & Return Field Y-Coordinates dictionary
def draw_table_with_fields(x, y, width, title, fields):
    header_height = 45
    row_height = 32
    total_height = header_height + (len(fields) * row_height) + 10

    # Shadow and Box
    draw.rectangle([x+4, y+4, x+width+4, y+total_height+4], fill='#CBD5E1')
    draw.rectangle([x, y, x+width, y+total_height], fill='#FFFFFF', outline='#0F172A', width=3)
    draw.rectangle([x, y, x+width, y+header_height], fill='#1E293B')
    draw.text((x+15, y+10), title, fill='#FFFFFF', font=font_bold)

    field_coords = {}
    curr_y = y + header_height + 10
    for field in fields:
        field_name = field.split()[0]
        field_coords[field_name] = curr_y + 12 # center of row

        color = '#1D4ED8' if "[PK]" in field else ('#D97706' if "[FK]" in field else '#1E293B')
        draw.text((x+15, curr_y), field, fill=color, font=font_small)
        curr_y += row_height

    return field_coords, (x, y, width, total_height)

# Layout:
# Left Column: tblUsers (x=80)
# Middle Column: tblSchedules (x=850, y=150), tblTasks (x=850, y=650), tblFocusSessions (x=850, y=1100)
# Right Column: tblAiQuizzes (x=1600, y=150), tblAnnouncements (x=1600, y=650)

f_users, b_users = draw_table_with_fields(80, 220, 440, "tblUsers (users)", [
    "id VARCHAR(128) [PK]",
    "email VARCHAR(255) [UNIQUE]",
    "fullName VARCHAR(150)",
    "studentId VARCHAR(50) [UNIQUE]",
    "school VARCHAR(200)",
    "course VARCHAR(150)",
    "year VARCHAR(20)",
    "section VARCHAR(50)",
    "role VARCHAR(30)",
    "status VARCHAR(30)",
    "suspensionDuration VARCHAR(50)",
    "suspendedAt BIGINT"
])

f_schedules, b_schedules = draw_table_with_fields(850, 150, 460, "tblSchedules (schedules)", [
    "id VARCHAR(128) [PK]",
    "userId VARCHAR(128) [FK ➔ tblUsers.id]",
    "dateString VARCHAR(20)",
    "className VARCHAR(200)",
    "classCode VARCHAR(50)",
    "school VARCHAR(150)",
    "startTime VARCHAR(30)",
    "endTime VARCHAR(30)",
    "room VARCHAR(50)",
    "instructor VARCHAR(150)",
    "colorType VARCHAR(30)"
])

f_tasks, b_tasks = draw_table_with_fields(850, 650, 460, "tblTasks (tasks)", [
    "id VARCHAR(128) [PK]",
    "userId VARCHAR(128) [FK ➔ tblUsers.id]",
    "scheduleId VARCHAR(128) [FK ➔ tblSchedules.id]",
    "title TEXT",
    "dateString VARCHAR(100)",
    "courseName VARCHAR(150)",
    "priority VARCHAR(30)",
    "completed BOOLEAN"
])

f_focus, b_focus = draw_table_with_fields(850, 1100, 460, "tblFocusSessions (focus_sessions)", [
    "id VARCHAR(128) [PK]",
    "userId VARCHAR(128) [FK ➔ tblUsers.id]",
    "durationMinutes INT",
    "completedAt BIGINT"
])

f_quizzes, b_quizzes = draw_table_with_fields(1600, 150, 460, "tblAiQuizzes (ai_quizzes)", [
    "id VARCHAR(128) [PK]",
    "userId VARCHAR(128) [FK ➔ tblUsers.id]",
    "userEmail VARCHAR(255)",
    "topic VARCHAR(250)",
    "score INT",
    "totalQuestions INT",
    "percentage INT",
    "timestamp BIGINT"
])

f_announcements, b_announcements = draw_table_with_fields(1600, 650, 460, "tblAnnouncements (announcements)", [
    "id VARCHAR(128) [PK]",
    "adminId VARCHAR(128) [FK ➔ tblUsers.id]",
    "message TEXT",
    "author VARCHAR(100)",
    "postedAt BIGINT"
])

# Precise Pointer Generator with Arrowhead
def draw_direct_arrow(start_x, start_y, end_x, end_y, color='#1D4ED8', label='1 : N', bend_offset=0):
    if bend_offset == 0:
        # Simple Orthogonal Step: start_x -> mid_x -> end_y -> end_x
        mid_x = (start_x + end_x) // 2
        points = [(start_x, start_y), (mid_x, start_y), (mid_x, end_y), (end_x, end_y)]
    else:
        # Customized Bend
        points = [(start_x, start_y), (start_x + bend_offset, start_y), (start_x + bend_offset, end_y), (end_x, end_y)]
        mid_x = start_x + bend_offset

    draw.line(points, fill=color, width=4)

    # Arrowhead at (end_x, end_y)
    dir_x = -1 if end_x < points[-2][0] else 1
    dir_y = -1 if end_y < points[-2][1] else 1

    if points[-2][1] == end_y: # Horizontal arrival
        draw.polygon([
            (end_x, end_y),
            (end_x - (14 * dir_x), end_y - 7),
            (end_x - (14 * dir_x), end_y + 7)
        ], fill=color)
    else: # Vertical arrival
        draw.polygon([
            (end_x, end_y),
            (end_x - 7, end_y - (14 * dir_y)),
            (end_x + 7, end_y - (14 * dir_y))
        ], fill=color)

    # Label badge
    lbl_x = (start_x + end_x) // 2
    lbl_y = (start_y + end_y) // 2
    draw.rectangle([lbl_x-35, lbl_y-12, lbl_x+35, lbl_y+12], fill='#FFFFFF', outline=color, width=2)
    draw.text((lbl_x-22, lbl_y-8), label, fill=color, font=font_small)

# 1. tblSchedules.userId [FK] ➔ tblUsers.id [PK]
draw_direct_arrow(850, f_schedules["userId"], 520, f_users["id"], '#1D4ED8', "1 : N", -150)

# 2. tblTasks.userId [FK] ➔ tblUsers.id [PK]
draw_direct_arrow(850, f_tasks["userId"], 520, f_users["id"], '#2563EB', "1 : N", -200)

# 3. tblTasks.scheduleId [FK] ➔ tblSchedules.id [PK] (Direct vertical relation between tasks & schedules!)
draw.line([(850, f_tasks["scheduleId"]), (780, f_tasks["scheduleId"]), (780, f_schedules["id"]), (850, f_schedules["id"])], fill='#7C3AED', width=4)
draw.polygon([(850, f_schedules["id"]), (836, f_schedules["id"]-7), (836, f_schedules["id"]+7)], fill='#7C3AED')
draw.rectangle([730, (f_tasks["scheduleId"]+f_schedules["id"])//2 - 12, 830, (f_tasks["scheduleId"]+f_schedules["id"])//2 + 12], fill='#FFFFFF', outline='#7C3AED', width=2)
draw.text((740, (f_tasks["scheduleId"]+f_schedules["id"])//2 - 8), "1 : N", fill='#7C3AED', font=font_small)

# 4. tblFocusSessions.userId [FK] ➔ tblUsers.id [PK]
draw_direct_arrow(850, f_focus["userId"], 520, f_users["id"], '#059669', "1 : N", -250)

# 5. tblAiQuizzes.userId [FK] ➔ tblUsers.id [PK] (Route above)
draw.line([(1600, f_quizzes["userId"]), (1540, f_quizzes["userId"]), (1540, 180), (520, 180), (520, f_users["id"])], fill='#D97706', width=4)
draw.polygon([(520, f_users["id"]), (520-7, f_users["id"]+14), (520+7, f_users["id"]+14)], fill='#D97706')
draw.rectangle([1000, 168, 1070, 192], fill='#FFFFFF', outline='#D97706', width=2)
draw.text((1010, 172), "1 : N", fill='#D97706', font=font_small)

# 6. tblAnnouncements.adminId [FK] ➔ tblUsers.id [PK] (Route below)
draw.line([(1600, f_announcements["adminId"]), (1540, f_announcements["adminId"]), (1540, 600), (520, 600), (520, f_users["id"])], fill='#DC2626', width=4)
draw.polygon([(520, f_users["id"]), (520-7, f_users["id"]+14), (520+7, f_users["id"]+14)], fill='#DC2626')
draw.rectangle([1000, 588, 1070, 612], fill='#FFFFFF', outline='#DC2626', width=2)
draw.text((1010, 592), "1 : N", fill='#DC2626', font=font_small)

erd_path = os.path.join(artifact_dir, "erd_diagram.png")
img_erd.save(erd_path)


# -------------------------------------------------------------------------
# Diagram 2: System Use Case Diagram (Admin Restricted) (usecase_diagram.png)
# -------------------------------------------------------------------------
img_uc = Image.new('RGB', (2200, 1400), color='#F8FAFC')
draw = ImageDraw.Draw(img_uc)

draw.rectangle([0, 0, 2200, 100], fill='#1D4ED8')
draw.text((60, 30), "ESKWELA - System Use Case Diagram (Role Access Control)", fill='#FFFFFF', font=font_title)

# System Boundary
draw.rectangle([450, 140, 1750, 1320], fill='#FFFFFF', outline='#0F172A', width=4)
draw.text((470, 160), "ESKWELA Mobile & Admin System Boundary", fill='#0F172A', font=font_bold)

# Draw Actors
def draw_actor(x, y, name, color='#1D4ED8'):
    draw.ellipse([x-25, y, x+25, y+50], outline=color, width=4)
    draw.line([x, y+50, x, y+120], fill=color, width=4)
    draw.line([x-40, y+80, x+40, y+80], fill=color, width=4)
    draw.line([x, y+120, x-30, y+180], fill=color, width=4)
    draw.line([x, y+120, x+30, y+180], fill=color, width=4)
    draw.text((x-55, y+195), name, fill='#0F172A', font=font_bold)

draw_actor(200, 550, "Student Actor", '#1D4ED8')
draw_actor(2000, 550, "Admin Actor", '#D97706')

# Use Cases
use_cases = [
    (1100, 220, "UC1: Register & Profile Setup", True, False),
    (1100, 330, "UC2: OCR Study Load Timetable Scanner", True, False),
    (1100, 440, "UC3: Interactive Schedule Calendar (Day/Week/Month)", True, False),
    (1100, 550, "UC4: Schedule Time Conflict Prevention", True, False),
    (1100, 660, "UC5: Task Manager & Priorities (Drop Validation)", True, False),
    (1100, 770, "UC6: Enrolled Study Load AI Quiz Generator", True, False),
    (1100, 880, "UC7: Global Student Quiz Leaderboard Rankings", True, False),
    (1100, 990, "UC8: Pomodoro Focus Timer (2m-60m)", True, False),
    (1100, 1100, "UC9: Admin Control Center & User Inspection", False, True),
    (1100, 1210, "UC10: Modify & Broadcast Official Announcements", False, True)
]

for cx, cy, label, student_acc, admin_acc in use_cases:
    fill_bg = '#FEF3C7' if admin_acc else '#EFF6FF'
    border_col = '#D97706' if admin_acc else '#1D4ED8'

    draw.ellipse([cx-380, cy-32, cx+380, cy+32], fill=fill_bg, outline=border_col, width=3)
    draw.text((cx-280, cy-12), label, fill='#0F172A', font=font_small)

    if student_acc:
        draw.line([250, 620, cx-380, cy], fill='#94A3B8', width=2)
    if admin_acc:
        draw.line([1950, 620, cx+380, cy], fill='#D97706', width=3)

usecase_path = os.path.join(artifact_dir, "usecase_diagram.png")
img_uc.save(usecase_path)


# -------------------------------------------------------------------------
# Diagram 3: High-Resolution Data Flow Diagram (DFD) (dfd_diagram.png)
# -------------------------------------------------------------------------
img_dfd = Image.new('RGB', (2200, 1200), color='#F8FAFC')
draw = ImageDraw.Draw(img_dfd)

draw.rectangle([0, 0, 2200, 100], fill='#1D4ED8')
draw.text((60, 30), "ESKWELA - Data Flow Diagram (DFD Level 1)", fill='#FFFFFF', font=font_title)

# Entities
draw.rectangle([80, 300, 380, 450], fill='#EFF6FF', outline='#1D4ED8', width=3)
draw.text((120, 360), "Student Entity", fill='#0F172A', font=font_bold)

draw.rectangle([80, 700, 380, 850], fill='#FEF3C7', outline='#D97706', width=3)
draw.text((120, 760), "Admin Entity", fill='#0F172A', font=font_bold)

# Processes
def draw_process(cx, cy, num, label):
    draw.ellipse([cx-140, cy-70, cx+140, cy+70], fill='#FFFFFF', outline='#0F172A', width=3)
    draw.line([cx-140, cy-20, cx+140, cy-20], fill='#0F172A', width=2)
    draw.text((cx-30, cy-55), num, fill='#1D4ED8', font=font_bold)
    draw.text((cx-110, cy), label, fill='#0F172A', font=font_small)

draw_process(800, 250, "1.0", "Authentication &\nProfile Sync")
draw_process(800, 550, "2.0", "OCR Scanner &\nSchedule Engine")
draw_process(800, 850, "3.0", "AI Quiz &\nLeaderboard Engine")
draw_process(1500, 550, "4.0", "Admin Control &\nAnnouncement Broadcast")

# Data Stores
def draw_datastore(x, y, name):
    draw.line([x, y, x+320, y], fill='#0F172A', width=3)
    draw.line([x, y+60, x+320, y+60], fill='#0F172A', width=3)
    draw.line([x+60, y, x+60, y+60], fill='#0F172A', width=2)
    draw.text((x+75, y+18), name, fill='#0F172A', font=font_bold)

draw_datastore(1780, 200, "D1: tblUsers")
draw_datastore(1780, 380, "D2: tblSchedules")
draw_datastore(1780, 560, "D3: tblTasks")
draw_datastore(1780, 740, "D4: tblAiQuizzes")
draw_datastore(1780, 920, "D5: tblAnnouncements")

# Arrows
draw.line([380, 375, 660, 250], fill='#64748B', width=3)
draw.line([380, 375, 660, 550], fill='#64748B', width=3)
draw.line([380, 375, 660, 850], fill='#64748B', width=3)

draw.line([380, 775, 1360, 550], fill='#D97706', width=3)

draw.line([940, 250, 1780, 230], fill='#1D4ED8', width=3)
draw.line([940, 550, 1780, 410], fill='#1D4ED8', width=3)
draw.line([940, 550, 1780, 590], fill='#1D4ED8', width=3)
draw.line([940, 850, 1780, 770], fill='#1D4ED8', width=3)
draw.line([1640, 550, 1780, 950], fill='#D97706', width=3)

dfd_path = os.path.join(artifact_dir, "dfd_diagram.png")
img_dfd.save(dfd_path)


# -------------------------------------------------------------------------
# Diagram 4: High-Resolution Sequence Diagram (sequence_diagram.png)
# -------------------------------------------------------------------------
img_seq = Image.new('RGB', (2200, 1100), color='#F8FAFC')
draw = ImageDraw.Draw(img_seq)

draw.rectangle([0, 0, 2200, 100], fill='#1D4ED8')
draw.text((60, 30), "ESKWELA - AI Quiz & Leaderboard Sequence Diagram", fill='#FFFFFF', font=font_title)

# Lifelines
lifelines = [
    (250, "Student"),
    (800, "ESKWELA Mobile App"),
    (1350, "Google ML Kit / AI Engine"),
    (1900, "Firebase Firestore")
]

for x, name in lifelines:
    draw.rectangle([x-120, 140, x+120, 200], fill='#1E293B')
    draw.text((x-80, 160), name, fill='#FFFFFF', font=font_bold)
    draw.line([x, 200, x, 1000], fill='#64748B', width=2)

# Sequence Arrows
def draw_msg(y, x1, x2, text):
    draw.line([x1, y, x2, y], fill='#1D4ED8', width=3)
    direction = 1 if x2 > x1 else -1
    draw.polygon([x2, y, x2 - (15*direction), y-8, x2 - (15*direction), y+8], fill='#1D4ED8')
    mid_x = (x1 + x2) // 2
    draw.text((mid_x-150, y-25), text, fill='#0F172A', font=font_small)

draw_msg(280, 250, 800, "1. Selects Course & Question Count (5-15 Qs)")
draw_msg(360, 800, 1350, "2. Request Smart Multiple-Choice Questions")
draw_msg(440, 1350, 800, "3. Return Course Questions & Explanations")
draw_msg(520, 800, 250, "4. Render Active Quiz Runner")
draw_msg(620, 250, 800, "5. Submits Answer Choices")
draw_msg(700, 800, 800, "6. Calculate Score & Diagnostic Percentage")
draw_msg(780, 800, 1900, "7. Save Attempt Record (tblAiQuizzes)")
draw_msg(860, 1900, 800, "8. Query Top 5 High Scores (ORDER BY percentage DESC)")
draw_msg(940, 800, 250, "9. Render Score Modal & Live Leaderboard Ranks")

seq_path = os.path.join(artifact_dir, "sequence_diagram.png")
img_seq.save(seq_path)


# =========================================================================
# 2. GENERATE PDF DOCUMENT WITH INSTRUCTOR'S EXACT TABLE TEMPLATE & PNGs
# =========================================================================

pdf_path = os.path.join(artifact_dir, "database_documentation.pdf")
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

code_style = ParagraphStyle(
    'CodeStyle',
    parent=styles['Normal'],
    fontName='Courier',
    fontSize=7.5,
    textColor=colors.HexColor('#0F172A'),
    leading=9
)

story = []

# Title & Metadata
story.append(Paragraph("ESKWELA Database Documentation & Technical Specification", title_style))
story.append(Paragraph("<b>Role:</b> Database Manager & Engineer &nbsp;|&nbsp; <b>Backend Architecture:</b> Google Firebase Auth & Firestore / Structured Relational Export", body_style))
story.append(Spacer(1, 10))

# 1. Executive Summary
story.append(Paragraph("1. Executive Summary", h2_style))
story.append(Paragraph("This document provides the complete database technical specifications, instructor-formatted Data Dictionary, field-precise visual ERD diagram, role-restricted Use Case Diagram, DFD, Sequence Diagram, and raw DDL SQL creation script for <b>ESKWELA</b>. Administrators have restricted privilege scoped exclusively to the Admin Control Center, user inspection, and modifying announcements.", body_style))

# 2. Data Dictionary (Instructor Template Format)
story.append(Paragraph("2. Data Dictionary (Instructor Format)", h2_style))

def make_instructor_table(title, rows):
    story.append(Paragraph(f"<b>{title}</b>", body_style))

    table_data = [[
        Paragraph("Field Name", table_header_style),
        Paragraph("Table", table_header_style),
        Paragraph("Database Field Name", table_header_style),
        Paragraph("Data Type", table_header_style),
        Paragraph("Length/Decimal Places", table_header_style),
        Paragraph("Description", table_header_style)
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

    t = Table(table_data, colWidths=[95, 75, 110, 65, 65, 130])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#BFDBFE')),
        ('TEXTCOLOR', (0,0), (-1,0), colors.HexColor('#1E3A8A')),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#93C5FD')),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ]))
    story.append(t)
    story.append(Spacer(1, 10))

# Table 1: tblUsers
make_instructor_table("tblUsers (Users Collection)", [
    ["User ID", "tblUsers", "id", "Text", "128", "Firebase Auth UID / Primary Key"],
    ["Email Address", "tblUsers", "email", "Text", "255", "Unique student or admin login email"],
    ["Full Name", "tblUsers", "fullName", "Text", "150", "Student or administrator full name"],
    ["Student ID", "tblUsers", "studentId", "Text", "50", "Unique student ID card number"],
    ["School Name", "tblUsers", "school", "Text", "200", "University or school name"],
    ["Course Program", "tblUsers", "course", "Text", "150", "Enrolled degree program"],
    ["Year Level", "tblUsers", "year", "Text", "20", "Academic year level (e.g. 4th Year)"],
    ["Class Section", "tblUsers", "section", "Text", "50", "Class section code (e.g. IT41C1)"],
    ["Role Privilege", "tblUsers", "role", "Text", "30", "Access role: 'student' or 'admin'"],
    ["Account Status", "tblUsers", "status", "Text", "30", "Status: active, suspended, deactivated, archived"],
    ["Suspension Duration", "tblUsers", "suspensionDuration", "Text", "50", "Duration if suspended (e.g. 24 Hours)"],
    ["Suspended Timestamp", "tblUsers", "suspendedAt", "Number", "-", "Epoch timestamp when account was suspended"]
])

# Table 2: tblSchedules
make_instructor_table("tblSchedules (Schedules Collection)", [
    ["Schedule ID", "tblSchedules", "id", "Text", "128", "Unique schedule record Primary Key"],
    ["User ID", "tblSchedules", "userId", "Text", "128", "Foreign Key referencing tblUsers(id)"],
    ["Schedule Date", "tblSchedules", "dateString", "Text", "20", "ISO Scheduled date string (YYYY-MM-DD)"],
    ["Class Name", "tblSchedules", "className", "Text", "200", "Course title (e.g. CAPSTONE PROJECT 1)"],
    ["Class Code", "tblSchedules", "classCode", "Text", "50", "Course code (e.g. IT6398)"],
    ["School Name", "tblSchedules", "school", "Text", "150", "Institution name"],
    ["Start Time", "tblSchedules", "startTime", "Text", "30", "Class start time (e.g. 3:00 PM)"],
    ["End Time", "tblSchedules", "endTime", "Text", "30", "Class end time (e.g. 4:30 PM)"],
    ["Room Location", "tblSchedules", "room", "Text", "50", "Room or laboratory number (e.g. 301, SLAB5)"],
    ["Instructor / Units", "tblSchedules", "instructor", "Text", "150", "Instructor name or section/units metadata"],
    ["Color Badge", "tblSchedules", "colorType", "Text", "30", "UI color badge (BLUE, ORANGE, GREEN)"]
])

story.append(PageBreak())

# Table 3: tblTasks
make_instructor_table("tblTasks (Tasks Collection)", [
    ["Task ID", "tblTasks", "id", "Text", "128", "Unique task record Primary Key"],
    ["User ID", "tblTasks", "userId", "Text", "128", "Foreign Key referencing tblUsers(id)"],
    ["Schedule ID", "tblTasks", "scheduleId", "Text", "128", "Optional Foreign Key referencing tblSchedules(id)"],
    ["Task Title", "tblTasks", "title", "Text", "500", "Task description or assignment title"],
    ["Due Date", "tblTasks", "dateString", "Text", "100", "Deadline display string"],
    ["Course Name", "tblTasks", "courseName", "Text", "150", "Associated course name"],
    ["Task Priority", "tblTasks", "priority", "Text", "30", "Priority level: HIGH, MEDIUM, LOW"],
    ["Completion Status", "tblTasks", "completed", "Boolean", "-", "Completion flag: true or false"]
])

# Table 4: tblAiQuizzes
make_instructor_table("tblAiQuizzes (AI Quizzes Collection)", [
    ["Quiz ID", "tblAiQuizzes", "id", "Text", "128", "Unique AI quiz record Primary Key"],
    ["User ID", "tblAiQuizzes", "userId", "Text", "128", "Foreign Key referencing tblUsers(id)"],
    ["User Email", "tblAiQuizzes", "userEmail", "Text", "255", "Email prefix used for Leaderboard ranking"],
    ["Quiz Topic", "tblAiQuizzes", "topic", "Text", "250", "Quiz course topic or subject"],
    ["Correct Score", "tblAiQuizzes", "score", "Number", "-", "Number of correct answers"],
    ["Total Questions", "tblAiQuizzes", "totalQuestions", "Number", "-", "Total questions in quiz (5, 10, or 15)"],
    ["Percentage Score", "tblAiQuizzes", "percentage", "Number", "-", "Calculated score percentage (0 - 100)"],
    ["Completion Timestamp", "tblAiQuizzes", "timestamp", "Number", "-", "Epoch timestamp of quiz completion"]
])

# Table 5: tblAnnouncements
make_instructor_table("tblAnnouncements (Announcements Collection)", [
    ["Announcement ID", "tblAnnouncements", "id", "Text", "128", "Unique announcement Primary Key"],
    ["Admin Author ID", "tblAnnouncements", "adminId", "Text", "128", "Foreign Key referencing tblUsers(id)"],
    ["Announcement Message", "tblAnnouncements", "message", "Text", "1000", "Official broadcast message text"],
    ["Author Tag", "tblAnnouncements", "author", "Text", "100", "Author credential tag (e.g. ADMIN)"],
    ["Posted Timestamp", "tblAnnouncements", "postedAt", "Number", "-", "Epoch timestamp when broadcasted"]
])

# Table 6: tblFocusSessions
make_instructor_table("tblFocusSessions (Focus Sessions Collection)", [
    ["Session ID", "tblFocusSessions", "id", "Text", "128", "Unique focus session Primary Key"],
    ["User ID", "tblFocusSessions", "userId", "Text", "128", "Foreign Key referencing tblUsers(id)"],
    ["Duration Minutes", "tblFocusSessions", "durationMinutes", "Number", "-", "Focus duration completed (2, 5, 10, 15, 25, 45, 60)"],
    ["Completed Timestamp", "tblFocusSessions", "completedAt", "Number", "-", "Epoch timestamp of session completion"]
])

story.append(PageBreak())

# =========================================================================
# 3. HIGH-RESOLUTION VISUAL DIAGRAMS EMBEDDED IN PDF
# =========================================================================

story.append(Paragraph("3. Precise Field-to-Field Visual Entity-Relationship Diagram (ERD)", h2_style))
story.append(Paragraph("Zero-crossing, clean diagram with precise orthogonal arrows pointing directly from Foreign Keys [FK] to Primary Keys [PK]:", body_style))
story.append(RLImage(erd_path, width=540, height=337))
story.append(Spacer(1, 15))

story.append(PageBreak())

# Diagram 2: Use Case Diagram
story.append(Paragraph("4. Visual System Use Case Diagram (Admin Access Scoped)", h2_style))
story.append(Paragraph("High-resolution diagram showing Student use cases and Admin restricted access (Admin Control Center & Announcements ONLY):", body_style))
story.append(RLImage(usecase_path, width=540, height=343))
story.append(Spacer(1, 15))

story.append(PageBreak())

# Diagram 3: Data Flow Diagram (DFD)
story.append(Paragraph("5. Visual Data Flow Diagram (DFD Level 1)", h2_style))
story.append(Paragraph("High-resolution diagram depicting data flow between entities, processes, and data stores:", body_style))
story.append(RLImage(dfd_path, width=540, height=294))
story.append(Spacer(1, 15))

story.append(PageBreak())

# Diagram 4: Sequence Diagram
story.append(Paragraph("6. Visual AI Quiz & Leaderboard Sequence Diagram", h2_style))
story.append(Paragraph("High-resolution diagram depicting the execution sequence for smart AI quiz generation and leaderboard rankings:", body_style))
story.append(RLImage(seq_path, width=540, height=270))
story.append(Spacer(1, 15))

story.append(PageBreak())

# =========================================================================
# 4. RAW SQL DDL SCHEMA CREATION SCRIPT WITH SEED DATA
# =========================================================================
story.append(Paragraph("7. Database Script File (schema.sql)", h2_style))
story.append(Paragraph("Raw DDL creation script and sample seed data:", body_style))

sql_script = """-- =========================================================================
-- ESKWELA DATABASE SCHEMA CREATION SCRIPT (DDL)
-- Backend Engine: Google Firebase Auth & Firestore NoSQL / Relational Export
-- =========================================================================

DROP TABLE IF EXISTS tblFocusSessions CASCADE;
DROP TABLE IF EXISTS tblAnnouncements CASCADE;
DROP TABLE IF EXISTS tblAiQuizzes CASCADE;
DROP TABLE IF EXISTS tblTasks CASCADE;
DROP TABLE IF EXISTS tblSchedules CASCADE;
DROP TABLE IF EXISTS tblUsers CASCADE;

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

CREATE TABLE tblAnnouncements (
    id VARCHAR(128) PRIMARY KEY,
    adminId VARCHAR(128) NOT NULL,
    message TEXT NOT NULL,
    author VARCHAR(100) NOT NULL DEFAULT 'ADMIN',
    postedAt BIGINT NOT NULL,
    FOREIGN KEY (adminId) REFERENCES tblUsers(id) ON DELETE CASCADE
);

CREATE TABLE tblFocusSessions (
    id VARCHAR(128) PRIMARY KEY,
    userId VARCHAR(128) NOT NULL,
    durationMinutes INT NOT NULL,
    completedAt BIGINT NOT NULL,
    FOREIGN KEY (userId) REFERENCES tblUsers(id) ON DELETE CASCADE
);

-- SAMPLE SEED DATA
INSERT INTO tblUsers (id, email, fullName, studentId, school, course, year, section, role, status)
VALUES ('admin-uid-001', 'admin@eskwela.com', 'System Administrator', 'ADM-0001', 'ESKWELA University', 'Administration', 'Admin', 'ADMIN-01', 'admin', 'active');

INSERT INTO tblUsers (id, email, fullName, studentId, school, course, year, section, role, status)
VALUES ('student-uid-101', 'student@eskwela.com', 'Juan Dela Cruz', 'STU-4921', 'University of the Philippines', 'BS Information Technology', '4th Year', 'IT41C1', 'student', 'active');

INSERT INTO tblSchedules (id, userId, dateString, className, classCode, school, startTime, endTime, room, instructor, colorType)
VALUES 
('sch-01', 'student-uid-101', '2025-05-12', 'CAPSTONE PROJECT 1', 'IT6398', 'University', '3:00 PM', '4:30 PM', '301', '3 Units · Sec IT41C1', 'BLUE'),
('sch-02', 'student-uid-101', '2025-05-12', 'LOAD TESTING', 'CS6303', 'University', '12:00 PM', '2:30 PM', 'SLAB5', '3 Units · Sec IT41C2', 'ORANGE'),
('sch-03', 'student-uid-101', '2025-05-12', 'SOFTWARE ENGINEERING', 'CS6209', 'University', '4:30 PM', '5:30 PM', '204', '3 Units · Sec IT41C6', 'GREEN');
"""

for line in sql_script.split('\n'):
    story.append(Paragraph(line, code_style))

doc.build(story)
print("Single PDF Document with Un-Crossed ERD generated successfully.")
