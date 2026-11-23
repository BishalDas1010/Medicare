# 💊 Madicare - Medicine Tracker & Reminder App

**Madicare** is a modern Android application built to help users manage their daily medications, track dose schedules, monitor remaining pill counts, and set up medicine reminders.

---

## 📱 Features

- **📅 Daily Schedule & Calendar**: View your medicine schedule by day and date.
- **✅ Quick Dose Tracking**: Mark medicines as "Taken" with a single click.
- **💊 My Medicines List**: Manage all stored medicines with status filters (**All**, **Active**, **Completed**).
- **➕ Add & Edit Medicine**: Save detailed information such as medicine name, dose quantity, frequency, times of day, pill colors, and pack size.
- **📊 Detailed Medicine View**: View comprehensive medicine information, remaining pill count progress, schedule details, and delete or update entries.
- **🎨 Modern Material 3 UI**: Clean, intuitive interface with full Jetpack Compose declarative layouts.

---

## 🏗️ How the Code Works (Simple & Easy to Understand)

Madicare follows Android's recommended **MVVM (Model-View-ViewModel)** architecture and **Unidirectional Data Flow (UDF)**. 

### 🔄 Data Flow Architecture

```
┌────────────────────────────────────────────────────────┐
│                        User UI                         │
│               (Jetpack Compose Screens)                │
└───────────────────────────▲────────────────────────────┘
                            │  Observes UI State (StateFlow)
                            │  Sends Actions (e.g. click)
┌───────────────────────────┴────────────────────────────┐
│                       ViewModel                        │
│          (Manages UI State & Business Logic)           │
└───────────────────────────▲────────────────────────────┘
                            │  Calls repository methods
                            │  Receives data updates (Flow)
┌───────────────────────────┴────────────────────────────┐
│                   Medicare Repository                  │
│               (Single Source of Truth)                 │
└───────────────────────────▲────────────────────────────┘
                            │  Interacts with DAO
                            │
┌───────────────────────────┴────────────────────────────┐
│                    Room Local Database                 │
│              (SQLite DB / Entities / DAO)              │
└────────────────────────────────────────────────────────┘
```

---

## ⚡ Understanding Hilt in Easy English

**Hilt** is the **automatic delivery assistant** (Dependency Injection framework) for your Android app.

### 💡 Analogy
- ❌ **Without Hilt**: Whenever a screen needs data, you must manually create the Database, create the DAO, create the Repository, and pass them into the ViewModel step-by-step.
- ✅ **With Hilt**: Hilt builds all required objects automatically behind the scenes! Your screen simply asks for a ViewModel with 1 line of code: `val viewModel: HomeViewModel = hiltViewModel()`.

### 🛠️ How Hilt is used in Madicare
1. **`MadicareApplication.kt` (`@HiltAndroidApp`)**: Initializes Hilt when the app launches.
2. **`DatabaseModule.kt` (`@Module`, `@Provides`, `@Singleton`)**: Teaches Hilt how to create the `AppDatabase` and `MedicareDao` instances.
3. **`MedicareRepository.kt` (`@Inject constructor`)**: Tells Hilt to automatically pass `MedicareDao` into the Repository.
4. **`HomeViewModel.kt` (`@HiltViewModel`, `@Inject`)**: Tells Hilt to automatically pass `MedicareRepository` into the ViewModels.
5. **`MainActivity.kt` (`@AndroidEntryPoint`)**: Allows Hilt to connect dependencies into your Activity and Compose UI screens.

---

## 🧱 Key Layers & Components

### 1. 🗄️ Database Layer (`DATABASE/`)
- **Entities (`database.kt`)**: Defines data models stored in the database.
  - `medicine_details`: Holds medicine name, dose, frequency, time, total tablets, remaining tablets, pill color, and taken status.
  - `ProfileEntity`, `HealthInfoEntity`, `DoctorVisitEntity`: Store user profile and extra medical records.
- **DAO (`DAO.kt`)**: Data Access Object containing SQL queries to insert, update, fetch, and delete medicines. It returns Kotlin `Flow`, enabling real-time automatic UI updates whenever database values change.
- **Database (`AppDatabase.kt`)**: The main Room SQLite database instance.

### 2. 🔌 Dependency Injection (`DATABASE/HiltModules/` & Hilt)
- Powered by **Dagger Hilt**.
- Automatically creates and supplies instances of `AppDatabase`, `MedicareDao`, and `MedicareRepository` across the app without manual wiring.

### 3. 📦 Repository Layer (`MedicareRepository.kt`)
- Serves as the single access point for data operations.
- Interacts directly with `MedicareDao` to get medicines or mark them as taken.

### 4. 🧠 ViewModel Layer (`UI/`)
- **`HomeViewModel`**: Prepares data for the daily schedule, generates calendar dates, calculates taken vs total pills count, and maps status (**Taken**, **Next**, **Upcoming**).
- **`MedicinesViewModel`**: Handles medicine list filtering (**All**, **Active**, **Completed**) and pill remaining percentages.
- **`AddMedicineViewModel`**: Handles form inputs, validation, and saving new or updated medicine records.
- **`MedicineDetailsViewModel`**: Fetches specific medicine details and handles deletion/editing.

### 5. 🎨 UI / View Layer (`UI/`, `Navigation/`)
- Built 100% in **Jetpack Compose**.
- **`MainScreen.kt`**: Main container with `NavHost` and `BottomNavigationBar` managing screen navigation (**Home**, **Medicines**, **Reports**, **Family**, **More**).
- **`Home.kt`**: Displays daily schedule cards, weekly date selector, and progress summaries.
- **`Mymedicine.kt`**: Displays stored medicines in cards with progress bars for pill counts.
- **`MedicineAdd.kt`**: Interactive form to input or edit medicine specifications.
- **`MedicineDetailsScreen.kt`**: Full overview screen of a single medicine.

---

## 📁 Project Structure

```
com.example.madicare/
├── Data/                       # Data models & UI States (e.g., Medicine, HomeUiState)
├── DATABASE/                   # Room Entities, DAO, AppDatabase & MedicareRepository
│   └── HiltModules/            # Hilt Dependency Injection module
├── Ennum/                      # Enums (e.g., MedicineStatus)
├── Navigation/                 # Jetpack Compose NavHost, Routes & Bottom Navigation Bar
├── UI/                         # Compose UI Screens & ViewModels
│   ├── home/                   # Home Screen & HomeViewModel
│   ├── Medicines/              # My Medicines Screen & ViewModel
│   │   └── madicine_ADD/       # Add/Edit Medicine Screen & ViewModel
│   ├── MedicineDetails/        # Medicine Details Screen & ViewModel
│   └── loginScreen.kt          # Login/Onboarding Screen
└── ui/theme/                   # Material 3 Color, Type, and Theme configurations
```

---

## 🚀 Getting Started & How to Run

### Prerequisites
- **Android Studio**: Ladybug / Jellyfish or newer.
- **JDK**: Version 11 or higher.
- **Min SDK**: 24 (Android 7.0 Nougat).
- **Target SDK**: 37.

### Steps
1. Open **Android Studio**.
2. Select **Open** and choose the `Madicare` project folder.
3. Wait for Gradle to finish syncing dependencies.
4. Select an Android Emulator or connected physical device.
5. Click **Run (`Shift + F10`)** or press the **Play ▶️** button in Android Studio.

---

## 🛠️ Built With

- **[Kotlin](https://kotlinlang.org/)** - Primary programming language.
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)** - Modern toolkit for building native UI.
- **[Material 3](https://m3.material.io/)** - Latest Android design system components.
- **[Room Database](https://developer.android.com/training/data-storage/room)** - SQLite object mapping library.
- **[Hilt / Dagger](https://developer.android.com/training/dependency-injection/hilt-android)** - Dependency injection library.
- **[Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html)** - Asynchronous and reactive stream handling.
- **[Jetpack Navigation Compose](https://developer.android.com/guide/navigation)** - In-app navigation.
