---
name: ui-compose-review
description: Review a Jetpack Compose screen for architecture, performance, recomposition, lifecycle safety, and maintainability. Never modify code before presenting findings and receiving approval.
---

# Compose Review

## Goal

Review a Compose screen as a Senior Android Architect before making any changes.

Focus on:

- Architecture
- State management
- Recomposition
- Performance
- Lifecycle safety
- Accessibility
- Material 3
- Code quality
- Screen decomposition
- Reusable components

Never start refactoring immediately.

---

## Commands

### review

Analyze the provided screen and generate:

- Overall score
- Critical issues
- Warnings
- Suggestions
- Estimated improvements

No code changes.

---

### plan

If the screen is oversized or poorly structured:

Generate a decomposition plan.

Include:

- Proposed file structure
- Components to extract
- Reusable composables
- Dialog extraction
- Preview generation

No code changes.

---

### refactor

Only available after explicit approval.

Apply the approved recommendations while:

- Preserving behavior
- Keeping business logic intact
- Following feature-first architecture
- Reducing recomposition scope
- Extracting reusable composables
- Generating Preview composables

---

## Rules

Always:

- Preserve behavior
- Prefer small composables
- Hoist state
- Keep business logic outside UI
- Keep composables stateless when possible
- Prefer feature-first organization
- Explain major architectural decisions

Never:

- Change behavior
- Introduce unnecessary abstractions
- Create generic Helper/Utils composables
- Rewrite unrelated code
- Refactor without approval

---

## Output

### Review

```text
Score: 90/100

Critical
- ...

Warnings
- ...

Suggestions
- ...
```

### Plan

```text
Current

HomeScreen.kt (642 lines)

Proposed

feature/home/
    HomeRoute.kt
    HomeScreen.kt
    HomeContent.kt
    components/
        Header.kt
        SearchBar.kt
        StatisticsCard.kt
        RecentItems.kt
```

### Approval

Ask:

> Proceed with automatic refactor?

Wait for confirmation.

### Refactor Summary

```text
✓ 8 composables extracted

✓ HomeScreen reduced to 92 lines

✓ Preview files added

✓ Recomposition scope reduced
```