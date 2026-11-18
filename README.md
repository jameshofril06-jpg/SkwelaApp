# 🎓 ESKWELA - Smart Student Productivity & Academic Management System

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Language](https://img.shields.io/badge/Language-Java%2011-orange.svg)
![Database](https://img.shields.io/badge/Backend-Google%20Firebase-FFCA28.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

**ESKWELA** is a modern, native Android mobile application designed to streamline student productivity, course timetable management, academic review, and administrative moderation. It features Google ML Kit OCR timetable scanning, AI-assisted practice quizzes, Pomodoro focus timer sessions, course study notes, and real-time over-the-air (OTA) updates.

---

## 🌟 Key Features

### 👨‍🎓 Student Features
* **📷 OCR Study Load Scanner**: Instantly scans timetable documents using Google ML Kit Vision OCR and automatically plots course schedules across weekdays.
* **📅 Date-Specific Timetable Calendar**: Interactive Day/Week/Month views with strict date-specific filtering and schedule overlap conflict prevention.
* **📝 Task Manager & Priority Badges**: Create assignments, set priority levels (`HIGH`, `MEDIUM`, `LOW`), filter tasks, and record drop reasons with confirmation dialogs.
* **🤖 Smart AI Practice Quizzes**: Generates course-specific multiple-choice practice quizzes (5, 10, or 15 questions) with step-by-step answer explanations.
* **🏆 Global Leaderboard Rankings**: Live student rankings (🥇 1st Place, 🥈 2nd Place, 🥉 3rd Place) tracking top score percentages.
* **⏱️ Pomodoro Focus Timer**: Customizable focus session countdown timer with presets (2m, 5m, 10m, 15m, 25m, 45m, 60m) and session tracking.
* **📚 Subject Study Notes & Reviewers**: Create, view, search, and manage course reviewer notes.
* **🌙 Global Dark Mode**: Instant theme switching with high-contrast text rendering.
* **👁️ Show/Hide Password Toggle**: Interactive eye icon on password fields.
* **👨‍🎓 / 👩‍🎓 Gender-Based Student Avatars**: Automatic male and female student profile icons.

### 🛡️ Administrator Features
* **📢 Admin Dashboard**: Dedicated broadcast announcement portal for posting and managing official student news.
* **👥 Admin Control Center**: Student directory search with live status filters (`All`, `Active`, `Suspended`, `Deactivated`, `Archived`).
* **⏳ Real-Time Suspension Tracking**: Moderation panel displaying exact remaining suspension countdowns (e.g. `SUSPENDED (6h 42m remaining)`).
* **🔒 Strict Access Isolation**: Admins are restricted exclusively to Home Dashboard and Profile tabs (`admin_bottom_nav_menu.xml`).

---

## 🚀 Over-The-Air (OTA) Updates
ESKWELA includes a dynamic in-app OTA update engine (`UpdateHelper.java`):
- Queries GitHub's REST API (`api.github.com/repos/jameshofril06-jpg/SkwelaApp/releases`) in real-time.
- Compares installed app version with remote release tags.
- Features a visual checking indicator, detailed release notes dialog, live download progress percentage bar, and native package installer prompt via Android `FileProvider`.

---

## 🛠️ Tech Stack & Dependencies

* **Language**: Java 11 (Android SDK)
* **UI Framework**: Android XML View System + Material Design 3 Components
* **Backend Database**: Google Cloud Firestore (NoSQL Document Database)
* **Authentication**: Google Firebase Authentication API
* **On-Device ML**: Google ML Kit Text Recognition API (`com.google.mlkit:text-recognition`)
* **OTA Network API**: GitHub REST API via Java `HttpURLConnection` and Android `DownloadManager`
* **Build System**: Gradle (Kotlin DSL `build.gradle.kts`)

---

## 💻 Build & Installation Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- JDK 11 / JDK 17
- Android SDK Platform 34 / 36 (Minimum SDK: 24, Target SDK: 36)

### Building from Source
1. Clone the repository:
   ```bash
   git clone https://github.com/jameshofril06-jpg/SkwelaApp.git
   cd SkwelaApp
   ```
2. Open the project in Android Studio.
3. Build Debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
4. Build Signed Release APK:
   ```bash
   ./gradlew assembleRelease
   ```
5. Built APKs will be located at:
   - `app/build/outputs/apk/debug/app-debug.apk`
   - `app/build/outputs/apk/release/app-release.apk`
   - `releases/downloads/app-release.apk`

---

## 📄 Documentation Deliverables

All system documentation files are located in the `Documentation/` directory:
- **Final System Documentation (PDF)**: `Documentation/Final/final_system_documentation.pdf`
- **Software Testing & QA Suite (PDF)**: `Documentation/Pre-Fi/software_testing_qa_suite.pdf`
- **Postman API Collection (JSON)**: `Documentation/Pre-Fi/eskwela_postman_collection.json`
- **Database Architecture Documentation (PDF)**: `Documentation/Midterm/database_documentation.pdf`
- **Relational SQL DDL Script (.sql)**: `Documentation/Midterm/schema.sql`

---

## 📜 License & Copyright
© 2025 ESKWELA Team. All rights reserved.
