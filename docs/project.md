# Project Quran — Product Specification & Technical Guidelines

## 1. Vision & Core Value Proposition
- **Mission:** Help Muslims build a stronger daily relationship with Islam through one meaningful, 10-minute journey every day.
- **Core Value:** Eliminate decision fatigue. Do NOT ask users what to read. Prepare one curated journey per day.
- **Philosophy:** Tranquil, distraction-free, zero gamification, zero advertisements, offline-first.

---

## 2. Technical Architecture
- **Framework:** Kotlin Multiplatform (KMP) + Compose Multiplatform
- **Target Platforms:** Android-first (iOS ready)
- **Data Persistence:** Local JSON bundle (30 daily journeys bundled in assets) + SQLDelight / Room KMP for local state (completion tracking).
- **Architecture Pattern:** Clean Architecture + MVVM / Unidirectional Data Flow (UDF).
- **Navigation:** Compose Multiplatform Navigation (Single Activity).

---

## 3. UI/UX Rules & Principles
1. **Zero Setup Friction:** On first launch, open immediately to the Home Screen showing today's journey. No login, no onboarding sliders, no topic pickers.
2. **One Primary Action Per Screen:** Maintain visual simplicity and space.
3. **Pacing:** Soft typography, high contrast, clean line height. Cards should hold max ~150 words per screen to prevent fatigue.
4. **Completion Lock:** When a journey is completed, lock the state with a calm message ("You're all set for today"). Do not allow starting tomorrow's lesson early.

---

## 4. Minimum MVP Screen Specifications (3 Screens Total)

### Screen 1: Home Screen (Daily Hub)
- **Responsibility:** Entry point and daily anchor.
- **State A (Ready):**
    - Displays current Date.
    - Displays Today's Topic Title and estimated duration ("10 mins").
    - Prominent CTA Button: `[ Begin Today's Journey ]`.
- **State B (Completed):**
    - Displays gentle confirmation: "Journey Complete for Today. See you tomorrow insha'Allah."
    - Shows today's takeaway action item for reference.

### Screen 2: Journey Flow Screen (Horizontal Card Pager)
- **Responsibility:** Interactive content delivery.
- **UI Components:**
    - Top Progress Bar showing current step out of 5.
    - Step 1: **Intro** (Context and background)
    - Step 2: **Story** (Main narrative)
    - Step 3: **Key Lessons** (Core takeaway bullet points)
    - Step 4: **Reflection** (Prompt to pause and internalize)
    - Step 5: **Action Item** (Single actionable task for the day)
    - Bottom Navigation: `Next` / `Finish` button or horizontal swipe.

### Screen 3: Completion Screen
- **Responsibility:** Psychological closure and setting the daily action.
- **UI Components:**
    - Completion graphic/icon.
    - Highlighted Action Item for today.
    - Subtitle: "See you tomorrow insha'Allah."
    - Button: `[ Return Home ]` -> Returns to Home Screen in "Completed" state.

---

## 5. Local Data Schema (JSON)

Save as `assets/journeys.json`:

```json
[
  {
    "id": "day_01",
    "dayNumber": 1,
    "title": "The Patience of Prophet Ayyub",
    "category": "Story of a Prophet",
    "durationMinutes": 10,
    "steps": [
      {
        "type": "INTRO",
        "title": "Introduction",
        "content": "Prophet Ayyub (Job) is the ultimate exemplar of Sabr (beautiful patience) in Islamic tradition..."
      },
      {
        "type": "STORY",
        "title": "The Trial",
        "content": "Ayyub was blessed with health, wealth, and family. Suddenly, he was tested with the loss of all three..."
      },
      {
        "type": "KEY_LESSONS",
        "title": "Key Lessons",
        "content": "1. True patience is active trust in Allah, not passive defeat.\n2. Tests are not necessarily punishments."
      },
      {
        "type": "REFLECTION",
        "title": "Reflection",
        "content": "Think of a minor inconvenience you faced today. How did you react emotionally?"
      },
      {
        "type": "ACTION",
        "title": "Today's Action",
        "content": "Pause for 3 seconds before reacting when faced with any frustration today."
      }
    ]
  }
]