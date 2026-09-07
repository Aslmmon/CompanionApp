---
name: tdd-agent
description: >-
  Guides the automatic orchestration and production of dedicated TDD subagents for all feature
  development and bug fixing tasks. Contains subagent prompt contracts, inner loop test commands,
  and handoff protocols.
---

# Dedicated TDD Subagent Orchestration Skill

Use this skill whenever a developer requests a new feature, modifications to an existing feature, or a bug fix. Under project rules, **all feature development and bug fixes must produce a dedicated subagent** specifically tasked with executing the TDD inner loop.

---

## 1. Role Division

- **Primary Agent (Architect & Orchestrator)**:
  - Formulates the feature scope, boundaries, and Acceptance Criteria (AC) Matrix, or the bug reproduction criteria.
  - Produces and invokes the dedicated subagent using `invoke_subagent`.
  - Audits the resulting code changes using `arch-audit` and presents the final walkthrough.
  - **Does NOT** directly write production or test code for features or bugfixes in the primary context.

- **Dedicated Subagent (TDD Specialist)**:
  - Executes in an isolated context (`Workspace: "inherit"` by default).
  - Authors tests first in `commonTest` against test fakes (`shared/src/commonTest/.../fakes/`).
  - Follows the strict RED -> GREEN -> REFACTOR sequence.
  - Returns a structured completion report with diffs and test results to the orchestrator.

---

## 2. Producing the Subagent

Use `invoke_subagent` with the following configuration:

```json
{
  "Subagents": [
    {
      "TypeName": "self",
      "Role": "TDD Feature Developer", // or "TDD Bugfix Specialist"
      "Workspace": "inherit",
      "Model": "inherit",
      "Prompt": "<TDD Contract Prompt>"
    }
  ]
}
```

---

## 3. Subagent Contract Prompt Blueprints

### Blueprint A: Feature Development TDD Prompt

```text
You are a dedicated TDD Feature Developer for the Sahaba Companions project.
Your mission is to implement feature [FEATURE_ID: FEATURE_NAME] following strict Test-Driven Development and Clean Architecture.

### 1. Scope & Acceptance Criteria (AC) Matrix
[Insert AC Matrix here, e.g.]
| ID | Given | When | Then | Target Layer |
| AC01 | ... | ... | ... | domain/usecase/ |
| AC02 | ... | ... | ... | presentation/.../ |

### 2. File Boundaries & Layers
- Domain: shared/src/commonMain/kotlin/.../domain/...
- Data: shared/src/commonMain/kotlin/.../data/...
- Presentation: shared/src/commonMain/kotlin/.../presentation/...
- DI: shared/src/commonMain/kotlin/.../di/AppModule.kt
- Strings: shared/src/commonMain/composeResources/values/strings.xml & values-ar/strings.xml
- Tests: shared/src/commonTest/kotlin/.../
- Fakes: shared/src/commonTest/kotlin/.../fakes/

### 3. Execution Protocol (MANDATORY ORDER)
1. RED: Write failing acceptance test(s) in commonTest corresponding 1:1 with the AC Matrix.
   - Use in-memory fakes from fakes/. NEVER import MockK or Mockito.
   - Run fast test command: ./gradlew :shared:testAndroidHostTest --tests "*<YourTestClass>*"
   - Confirm test fails for the expected reason (RED verified).
2. GREEN: Implement minimal production code layer-by-layer:
   - Domain (models, repo interface, usecases)
   - Data (DTOs, mappers, repo impl)
   - Presentation (@Immutable UiState, UiEffect, ViewModel, Screen/Content split)
   - DI (AppModule.kt bindings) & Strings (both EN and AR XMLs)
   - Re-run test command and confirm test passes (GREEN verified).
3. REFACTOR:
   - Clean up code and eliminate duplication.
   - Verify: zero platform imports in domain, @Immutable on UI states, main-safe I/O.
   - Run full test suite: ./gradlew :shared:testAndroidHostTest

### 4. Output Report
When complete, return a concise report with:
- List of created/modified files
- Test command output confirming PASS
- Any architectural notes or fakes added
```

### Blueprint B: Bugfix TDD Prompt

```text
You are a dedicated TDD Bugfix Specialist for the Sahaba Companions project.
Your mission is to resolve bug [BUG_DESCRIPTION] using strict Test-Driven Development.

### 1. Bug Reproduction & Expected Behavior
- Observed Flaw: [Description of bug]
- Root Cause Hypothesis: [Hypothesis]
- Expected Behavior: [What should happen]

### 2. Execution Protocol (MANDATORY ORDER)
1. RED: Write a regression test in commonTest reproducing the bug.
   - Run: ./gradlew :shared:testAndroidHostTest --tests "*<RegressionTest>*"
   - Confirm the test FAILS, reproducing the bug (RED verified).
2. GREEN: Implement minimal surgical fix in the affected layer.
   - Re-run: ./gradlew :shared:testAndroidHostTest --tests "*<RegressionTest>*"
   - Confirm the test PASSES (GREEN verified).
3. REFACTOR:
   - Clean up without expanding scope.
   - Run full test suite: ./gradlew :shared:testAndroidHostTest to verify zero regressions.

### 3. Output Report
When complete, return a concise report with:
- Files modified
- Before/After behavior summary
- Test run confirmation
```

---

## 4. Orchestrator Post-Execution Verification

After the subagent finishes:
1. Review the git diff and list of touched files.
2. Run the `arch-audit` skill to ensure architectural rules were upheld:
   - Zero platform imports in `domain/`
   - ViewModels inject Use Cases, not raw Repositories
   - UI states annotated with `@Immutable`
   - Strings present in both EN and AR XMLs
3. Present the walkthrough artifact to the developer.
