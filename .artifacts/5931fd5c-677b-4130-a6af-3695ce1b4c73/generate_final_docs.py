import os
from PIL import Image, ImageDraw, ImageFont
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, Image as RLImage
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors

artifact_dir = "C:/Users/JAMES/Documents/GitHub/SkwelaApp/.artifacts/5931fd5c-677b-4130-a6af-3695ce1b4c73"

def get_font(size, bold=False):
    try:
        font_path = "C:/Windows/Fonts/arialbd.ttf" if bold else "C:/Windows/Fonts/arial.ttf"
        return ImageFont.truetype(font_path, size)
    except:
        return ImageFont.load_default()

font_title = get_font(28, bold=True)
font_header = get_font(20, bold=True)
font_bold = get_font(15, bold=True)
font_small = get_font(13, bold=False)

# =========================================================================
# 1. GENERATE VISUAL GANTT CHART & DEPLOYMENT PLAN PNG DIAGRAMS
# =========================================================================

# -------------------------------------------------------------------------
# Diagram 1: Excel-style Visual Gantt Chart (Aug 2026 - Nov 26, 2026)
# -------------------------------------------------------------------------
img_gantt = Image.new('RGB', (2400, 1100), color='#F8FAFC')
draw = ImageDraw.Draw(img_gantt)

# Title Banner
draw.rectangle([0, 0, 2400, 90], fill='#1D4ED8')
draw.text((60, 25), "ESKWELA - SDLC Project Gantt Chart (August 2026 – November 26, 2026)", fill='#FFFFFF', font=font_title)

# Header Row (Timeline Weeks)
col_start_x = 650
col_width = 105
weeks = ["Aug W1", "Aug W2", "Aug W3", "Aug W4", "Sep W1", "Sep W2", "Sep W3", "Sep W4", "Oct W1", "Oct W2", "Oct W3", "Oct W4", "Nov W1", "Nov W2", "Nov W3", "Nov W4"]

# Timeline Header Background
draw.rectangle([0, 90, 2400, 150], fill='#1E293B')
draw.text((40, 110), "SDLC Task / Phase Deliverable", fill='#FFFFFF', font=font_bold)

for i, w in enumerate(weeks):
    wx = col_start_x + (i * col_width)
    draw.text((wx+10, 110), w, fill='#CBD5E1', font=get_font(12, bold=True))
    draw.line([wx, 150, wx, 1050], fill='#E2E8F0', width=2)

# Tasks Data (August 2026 - November 26, 2026)
gantt_tasks = [
    ("1. Planning & Scope Definition", 0, 1, "#1D4ED8"),
    ("2. Database ERD & Schema Design", 2, 3, "#059669"),
    ("3. Android Java & ML Kit OCR Scanner", 4, 7, "#D97706"),
    ("4. AI Quiz & Leaderboard Engine", 8, 9, "#7C3AED"),
    ("5. Admin Center & OTA Update Engine", 10, 11, "#4F46E5"),
    ("6. QA Suite Testing & UAT Execution", 12, 13, "#DB2777"),
    ("7. Signed APK Release & Deployment", 14, 15, "#0D9488")
]

start_y = 170
row_h = 120

for idx, (task_title, start_w, end_w, bar_color) in enumerate(gantt_tasks):
    curr_y = start_y + (idx * row_h)

    # Alternating row background
    bg_col = '#F1F5F9' if idx % 2 == 0 else '#FFFFFF'
    draw.rectangle([20, curr_y-10, 2380, curr_y+70], fill=bg_col)

    # Task Label
    draw.text((40, curr_y+15), task_title, fill='#0F172A', font=font_bold)

    # Timeline Bar
    bx1 = col_start_x + (start_w * col_width) + 5
    bx2 = col_start_x + ((end_w + 1) * col_width) - 5
    by1 = curr_y + 10
    by2 = curr_y + 50

    # Bar Shadow & Fill
    draw.rectangle([bx1+3, by1+3, bx2+3, by2+3], fill='#94A3B8')
    draw.rectangle([bx1, by1, bx2, by2], fill=bar_color)
    draw.text((bx1+15, by1+10), "100% COMPLETED", fill='#FFFFFF', font=get_font(12, bold=True))

gantt_path = os.path.join(artifact_dir, "gantt_chart.png")
img_gantt.save(gantt_path)


# -------------------------------------------------------------------------
# Diagram 2: Visual Deployment Architecture Plan (deployment_plan_chart.png)
# -------------------------------------------------------------------------
img_dep = Image.new('RGB', (2200, 1000), color='#F8FAFC')
draw = ImageDraw.Draw(img_dep)

draw.rectangle([0, 0, 2200, 90], fill='#1D4ED8')
draw.text((60, 25), "ESKWELA - System Environment Deployment Architecture", fill='#FFFFFF', font=font_title)

# Environment Cards
def draw_dep_card(x, y, width, height, title, subtitle, badge, items):
    draw.rectangle([x+5, y+5, x+width+5, y+height+5], fill='#CBD5E1')
    draw.rectangle([x, y, x+width, y+height], fill='#FFFFFF', outline='#0F172A', width=3)
    draw.rectangle([x, y, x+width, y+50], fill='#1E293B')

    draw.text((x+15, y+12), title, fill='#FFFFFF', font=font_bold)

    # Status Badge
    draw.rectangle([x+width-150, y+10, x+width-15, y+40], fill='#15803D')
    draw.text((x+width-140, y+16), badge, fill='#FFFFFF', font=get_font(12, bold=True))

    draw.text((x+15, y+65), subtitle, fill='#1D4ED8', font=get_font(13, bold=True))

    cy = y + 100
    for it in items:
        draw.text((x+15, cy), "• " + it, fill='#1E293B', font=font_small)
        cy += 32

draw_dep_card(100, 160, 950, 360, "1. Android Mobile Devices (Client App)", "Target Environment: Native Android Devices (API 24+)", "LIVE / READY", [
    "Executable Artifact: app-release.apk (v1.2.3)",
    "Target SDK: Android 36 (Java 11 Runtime Engine)",
    "Storage & Cache: Local SharedPreferences & Offline Firestore",
    "Permissions: Camera (OCR), Storage Access & Package Installer"
])

draw_dep_card(1150, 160, 950, 360, "2. Google Firebase Cloud Infrastructure", "Backend Environment: Cloud Firestore & Firebase Auth", "LIVE / DEPLOYED", [
    "Auth Service: Email/Password Session Tokens & Account Guard",
    "NoSQL Database: Firestore (users, schedules, tasks, ai_quizzes, etc.)",
    "Security Rules: Role-based document access controls",
    "Analytics: Live Leaderboard ranking queries"
])

draw_dep_card(100, 560, 950, 360, "3. GitHub Releases & OTA Server", "Distribution Channel: GitHub CDN (jameshofril06-jpg/SkwelaApp)", "LIVE / PUBLIC", [
    "Release Tag: v1.2.3 (Build 123)",
    "OTA Endpoint: api.github.com/repos/.../releases",
    "Payload Asset: app-release.apk (50.0 MB)",
    "Update Mechanism: DownloadManager & FileProvider installer"
])

draw_dep_card(1150, 560, 950, 360, "4. Postman API Test Suite & Docs", "API Testing Suite: Postman Collection v2.1", "VERIFIED", [
    "Exported JSON: Documentation/Pre-Fi/eskwela_postman_collection.json",
    "Endpoints Covered: Firebase Auth, Firestore REST API & GitHub API",
    "QA Test Suite: 80 Executed Test Cases (software_testing_qa_suite.pdf)",
    "Documentation: Single PDF & DDL SQL creation script (schema.sql)"
])

draw.line([1050, 340, 1150, 340], fill='#1D4ED8', width=4)
draw.line([600, 520, 600, 560], fill='#1D4ED8', width=4)
draw.line([1620, 520, 1620, 560], fill='#1D4ED8', width=4)

dep_path = os.path.join(artifact_dir, "deployment_plan_chart.png")
img_dep.save(dep_path)


# =========================================================================
# 2. REPORTLAB PDF BUILDER WITH UPDATED AUGUST-NOVEMBER 2026 GANTT CHART
# =========================================================================
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
final_story.append(Paragraph("The development of ESKWELA was initiated in <b>August 2026</b> and completed on <b>November 26, 2026</b>, following the <b>Agile Iterative SDLC Framework</b> across seven structured phases:", body_style))
final_story.append(Paragraph("• <b>Phase 1: Planning & Requirements Elicitation</b> (Aug 01, 2026 – Aug 15, 2026) — Gathered student pain points, defined functional requirements for timetable parsing, AI quiz generation, and admin control.", body_style))
final_story.append(Paragraph("• <b>Phase 2: Architectural & Database Design</b> (Aug 16, 2026 – Aug 31, 2026) — Modeled NoSQL document collections in Firebase Firestore (`users`, `schedules`, `tasks`, `ai_quizzes`, `announcements`, `focus_sessions`, `study_notes`), mapped relational DDL schemas, and designed UI wireframes.", body_style))
final_story.append(Paragraph("• <b>Phase 3: Android Java & ML Kit OCR Implementation</b> (Sep 01, 2026 – Sep 30, 2026) — Developed Android Java activities, integrated Google ML Kit Vision Text Recognition, configured Firebase Auth/Firestore, implemented RegEx OCR parser.", body_style))
final_story.append(Paragraph("• <b>Phase 4: AI Quiz & Leaderboard Engine</b> (Oct 01, 2026 – Oct 15, 2026) — Developed course practice quiz generator, step-by-step answer explanations, and live student ranking leaderboard.", body_style))
final_story.append(Paragraph("• <b>Phase 5: Admin Control Center & OTA Update</b> (Oct 16, 2026 – Oct 31, 2026) — Built Admin Dashboard, student directory search, status filters, timed suspension countdowns, and GitHub REST API update checker.", body_style))
final_story.append(Paragraph("• <b>Phase 6: QA Suite Testing & UAT Execution</b> (Nov 01, 2026 – Nov 15, 2026) — Executed 80 unit, integration, and UAT test cases covering OCR scanning, time conflict prevention, admin role isolation, and dark mode contrast.", body_style))
final_story.append(Paragraph("• <b>Phase 7: Signed APK Release & Final Deployment</b> (Nov 16, 2026 – Nov 26, 2026) — Published signed release APKs (`app-release.apk`) on GitHub Releases and established OTA update channels.", body_style))

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

# 4. Visual Gantt Chart & Deployment Plan Architecture
final_story.append(Paragraph("4. Visual Gantt Chart & Deployment Plan Graphics", h2_style))
final_story.append(Paragraph("<b>4.1 SDLC Project Timeline Visual Gantt Chart (August 2026 – November 26, 2026):</b>", body_style))
final_story.append(RLImage(gantt_path, width=540, height=247))
final_story.append(Spacer(1, 10))

gantt_data = [
    ["Phase", "Task / Deliverable", "Duration", "Start Date", "End Date", "Status"],
    ["1. Planning", "Requirements Elicitation & Scope Definition", "2 Weeks", "Aug 01, 2026", "Aug 15, 2026", "COMPLETED"],
    ["2. Design", "UI/UX Wireframing & Firestore Database ERD", "2 Weeks", "Aug 16, 2026", "Aug 31, 2026", "COMPLETED"],
    ["3. Development", "Android Java Core Activities & ML Kit OCR", "4 Weeks", "Sep 01, 2026", "Sep 30, 2026", "COMPLETED"],
    ["3. Development", "AI Quiz Generator & Leaderboard Engine", "2 Weeks", "Oct 01, 2026", "Oct 15, 2026", "COMPLETED"],
    ["3. Development", "Admin Control Center & OTA Update Engine", "2 Weeks", "Oct 16, 2026", "Oct 31, 2026", "COMPLETED"],
    ["4. Testing", "Functional, API Integration & UAT Testing", "2 Weeks", "Nov 01, 2026", "Nov 15, 2026", "COMPLETED"],
    ["5. Deployment", "Signed APK Release & Final Deployment", "2 Weeks", "Nov 16, 2026", "Nov 26, 2026", "COMPLETED"]
]

t_gantt = Table(gantt_data, colWidths=[80, 190, 60, 75, 75, 60])
t_gantt.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#DBEAFE')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.HexColor('#1E3A8A')),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 7.5),
    ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#93C5FD')),
    ('TOPPADDING', (0,0), (-1,-1), 4),
    ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
]))
final_story.append(t_gantt)
final_story.append(Spacer(1, 10))

final_story.append(Paragraph("<b>4.2 Visual System Deployment Architecture:</b>", body_style))
final_story.append(RLImage(dep_path, width=540, height=245))

doc_final.build(final_story)
print("Complete Final System Documentation PDF with August 2026 - November 26, 2026 Gantt Chart generated successfully.")
