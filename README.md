# SKWELA APP - SYSTEM DOCUMENTATION
## Capstone Project Specification (v1.0)

---

### Project Overview
**Skwela App** is an offline-first, mobile schedule manager and productivity tool designed to help students track classes, manage daily routines, and keep on track via an automated, local rule-based "Study Buddy" feature.

---

### System Architecture
The application implements a **Client-Side Cache + Cloud Master Sync** model. This provides full offline capability without requiring any operational or hosting budget.

*   **Frontend UI Layer:** Built with React Native & Expo (TypeScript).
*   **Local Storage Layer (Offline):** Driven by an embedded SQLite / WatermelonDB instance residing natively on the user's mobile device.
*   **Cloud Database Layer (Online):** Powered by a free-tier Supabase PostgreSQL instance.
*   **Sync Pipeline:** Driven by PowerSync to handle automatic delta replication and collision handling when an active internet network is re-established.

---

### Core Functional Requirements

#### 1. Schedule & Routine Management
*   **Class Tracking:** Allows input of course codes, descriptions, locations, faculty details, and recurring timetables (e.g., MWF 09:00 AM).
*   **Routine Logs:** Tracks non-academic activities including exercises, homework windows, study slots, and rest cycles.
*   **Conflict Prevention:** The local SQL database engine blocks the insertion of overlapping time blocks within the same date scope.

#### 2. Local Push Notifications
*   **Network Autonomy:** Reminders are managed via the device's hardware alarm configuration utilizing `expo-notifications`.
*   **Notification Rule:** System triggers an alarm exactly 15 minutes before any scheduled entry. It remains fully operational without cellular service or active Wi-Fi routing.

#### 3. Delta Synchronization Protocol
*   **Local Write Pipeline:** User updates are written locally to SQLite instantly to guarantee zero latency.
*   **Sync Matching:** Table rows utilize tracking fields (`updated_at`, `is_synced`). When network states change to online, only rows containing un-replicated tags are transmitted to Supabase.

#### 4. The Rule-Based "Study Buddy" Feature
*   **Design Framework:** To maintain zero server costs, the Study Buddy runs entirely as a client-side algorithmic companion utilizing vector graphics or localized assets.
*   **State Machine Configurations:**
    *   **Happy State:** Triggered when the user logs an assignment or routine completion rate of >80% for the current week.
    *   **Anxious State:** Activates automatically when a scheduled deadline or exam is within a 15-minute window.
    *   **Sleeping State:** Matches the user's pre-configured rest schedule to visually reinforce healthy lifestyle routines.

---

### Team Project Role Distribution (Team of 4)

*   **Developer 1: Database & Synchronization Specialist (Backend)**
    *   Manages Supabase schemas, configures database tables, and designs local-to-cloud synchronization policies.
*   **Developer 2: Interface & State Management Engineer (Frontend)**
    *   Constructs calendar views, routine dashboard elements, and wires UI views directly to local SQLite data providers.
*   **Developer 3: Identity & Profile Management Engineer (Frontend)**
    *   Integrates onboarding workflows, secure device authorization using Supabase Auth, and profile fields.
*   **Developer 4: Local Engines, Quality Assurance & DevOps (Fullstack)**
    *   Configures device notifications, coordinates testing matrices, and acts as the repository gatekeeper for Git Pull Requests.

---

### Recommended Git Workflow Strategy
To eliminate repository merge conflicts within a four-person workspace, the team should enforce a **Feature Branching Strategy**:

*   **`main` Branch:** Stores stable, fully deployable iterations of the project. Developers never commit code directly to `main`.
*   **`develop` Branch:** Acts as the primary integration space where all verified component functions are consolidated.
*   **Feature Branches (`feature/your-feature-name`):** Each team member creates an isolated branch off `develop` to build their respective module.
*   **Integration Rule:** Code is merged back into `develop` exclusively through formal Pull Requests (PRs) after a peer code review.

