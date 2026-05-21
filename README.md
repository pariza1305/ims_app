# IMS App — Institute Management System

An Android application for managing academic institutions — built with **Kotlin**, **Jetpack Compose**, and **Room** database.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Build](#build)
  - [Run on Device / Emulator](#run-on-device--emulator)
- [Database](#database)
- [Localization](#localization)
- [Settings](#settings)
- [License](#license)

---

## Overview

IMS App is a comprehensive Institute Management System designed for educational institutions. It provides role-based access for administrators and students, supporting day-to-day academic workflows such as attendance tracking, exam management, batch organization, and in-app notifications.

---

## Features

| Module | Description |
|---|---|
| **Authentication** | Secure login with role-based access control |
| **Dashboard** | At-a-glance stats — student counts, attendance summaries, upcoming exams, and news |
| **Academy Management** | Manage courses, batches, subjects, and student transfers |
| **Student Management** | Add/view students with custom categories and unique IDs |
| **Attendance** | Mark attendance by batch & subject, view personal attendance records, generate reports |
| **Examinations** | Create exams, record marks, view results, generate report cards and summary reports |
| **Notifications** | Send and receive in-app notifications; automated alerts tied to attendance thresholds |
| **Graduation** | Track and manage student graduation records |
| **Settings** | Configure grading system (GPA/Marks), auto-ID generation, and app language |
| **Localization** | English, Hindi, and Punjabi language support |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI Toolkit | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation |
| State Management | ViewModel + StateFlow |
| Local Database | Room (SQLite) |
| Async | Kotlin Coroutines |
| Build System | Gradle (Kotlin DSL) |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 34 (Android 14) |

---

## Project Structure

```
ims_app/
├── src/                          # Android project root
│   ├── app/
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       └── java/com/ims/app/
│   │           ├── IMSApplication.kt       # Application class — DB & repo init
│   │           ├── MainActivity.kt         # Entry point — ViewModels + NavGraph
│   │           ├── data/
│   │           │   ├── IMSDatabase.kt      # Room database definition
│   │           │   ├── AppSettingsManager.kt
│   │           │   ├── dao/                # Data Access Objects (10 DAOs)
│   │           │   ├── entity/             # Room entities (12 tables)
│   │           │   ├── repository/         # Repository layer
│   │           │   └── seed/               # Initial seed data
│   │           ├── navigation/
│   │           │   ├── NavGraph.kt         # Full navigation graph
│   │           │   └── Screen.kt           # Sealed class for all routes
│   │           ├── i18n/
│   │           │   └── AppLanguageManager.kt
│   │           └── ui/
│   │               ├── academy/            # Course, batch, student category screens
│   │               ├── attendance/         # Mark, view, and report screens
│   │               ├── dashboard/          # Home, search, settings, profile screens
│   │               ├── examination/        # Exam CRUD, marks, results, report cards
│   │               ├── notification/       # Notification list & send screens
│   │               ├── components/         # Shared reusable composables
│   │               └── theme/              # Material 3 theme definition
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   └── Makefile
└── Codebase Documentation.pdf
```

---

## Architecture

The app follows the **MVVM (Model-View-ViewModel)** pattern with a unidirectional data flow:

```
UI (Compose Screens)
        ↕  observes StateFlow
  ViewModel
        ↕  suspending calls
  Repository
        ↕  Room queries
  DAO / Room Database
```

- **Entities** map directly to SQLite tables via Room annotations.
- **DAOs** expose suspend functions and `Flow`-based reactive queries.
- **Repositories** wrap DAOs and provide a clean API to ViewModels.
- **ViewModels** hold UI state as `StateFlow` / `MutableStateFlow` and expose one-shot events.
- **Screens** are pure Composable functions that observe ViewModel state.

### Database Entities

| Entity | Purpose |
|---|---|
| `User` | Admin/staff accounts |
| `Student` | Student profiles |
| `Course` | Academic programmes |
| `Batch` | Groups of students within a course |
| `Subject` | Subjects taught in a batch |
| `AttendanceRecord` | Per-student per-session attendance entries |
| `Exam` | Exam definitions (date, total marks, etc.) |
| `ExamResult` | Per-student exam scores |
| `NewsArticle` | Dashboard news/announcements |
| `Notification` | In-app notifications |
| `StudentCategory` | Custom student grouping categories |
| `GraduationRecord` | Graduation status records |

---

## Getting Started

### Prerequisites

- **Android Studio Hedgehog (2023.1.1)** or newer
- **JDK 17** (configured via `GRADLE_JAVA_HOME` in the Makefile)
- Android SDK with API level 26–34 installed
- A physical Android device or AVD (API 26+)

### Build

**Using Android Studio:**

1. Clone the repository and open the `src/` directory as the project root in Android Studio.
2. Let Gradle sync complete.
3. Click **Run ▶** or use **Build → Make Project**.

**Using the command line (Makefile):**

```bash
cd src/

# Build a debug APK
make build

# Clean build artifacts
make clean
```

> **Note:** Set `GRADLE_JAVA_HOME` to your JDK 17 path if it differs from the default:
> ```bash
> make build GRADLE_JAVA_HOME=/path/to/jdk-17
> ```

**Using Gradle directly:**

```bash
cd src/
./gradlew assembleDebug
```

The debug APK will be output to:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Run on Device / Emulator

```bash
# Install on connected device/emulator
./gradlew installDebug

# Or use Android Studio's Run button
```

---

## Database

The app uses **Room** with a single database (`ims_database`) containing **12 tables**. The database is initialized in `IMSApplication.onCreate()` and pre-populated with seed data via `seedDatabase()` on first launch.

> **Destructive migration** is enabled — reinstalling the app or incrementing `database version` in `IMSDatabase.kt` will wipe and recreate the database.

Current schema version: **9**

---

## Localization

The app supports three languages configurable from the Settings screen:

| Language | Tag |
|---|---|
| English | `en` |
| Hindi | `hi` |
| Punjabi | `pa` |

Language preference is persisted in `SharedPreferences` (`ims_settings`) and applied at startup via `AppLanguageManager` before any UI is composed.

---

## Settings

User-configurable settings stored in `SharedPreferences`:

| Setting | Key | Default | Description |
|---|---|---|---|
| Grading System | `grading_system` | `GPA` | Switch between GPA and raw marks evaluation |
| Auto Unique ID | `auto_unique_id` | `true` | Automatically generate unique student IDs |
| App Language | `language_tag` | `en` | UI language (English / Hindi / Punjabi) |

---

## License

This project is intended for academic/educational use.  
© 2024 IMS App. All rights reserved.
