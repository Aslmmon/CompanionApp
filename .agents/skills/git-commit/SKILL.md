---
name: git-commit
description: Review repository changes, generate a Conventional Commit message, and commit after a single confirmation.
---

# Git Commit Skill

## Trigger
- "Commit my changes"
- "/commit"

## Behavior

1. Automatically inspect the repository by analyzing:
    - Modified files
    - Untracked files
    - Renamed files
    - Deleted files
    - Staged changes (if any)

2. Review all detected changes for:
    - Security vulnerabilities and exposed secrets (API keys, tokens, passwords, credentials).
    - Hardcoded sensitive values.
    - Debug code, logs, TODOs, or temporary code.
    - Performance concerns.
    - Error handling and edge cases.
    - Code quality and best practices.
    - Architecture consistency.
    - Breaking changes.
    - Dependency or configuration changes.
    - Missing tests or documentation (when applicable).
    - Whether the changes represent a single logical commit.

3. If critical issues are found:
    - Explain them briefly.
    - Recommend fixes.
    - Do not proceed until resolved.

4. If the working tree is clean:
    - Inform the user that there are no changes to commit.

5. Otherwise:
    - Generate a concise Conventional Commit message.
    - Maximum 2 lines.
    - Imperative mood.
    - Follow Conventional Commits (`feat`, `fix`, `refactor`, `docs`, `test`, `perf`, `style`, `build`, `ci`, `chore`, `revert`).
    - Ensure the message accurately reflects all modifications.

6. Display:
    - The proposed commit message.
    - A brief summary of the detected changes.
    - Any non-blocking recommendations.

7. Ask for a single confirmation (Yes/No).

8. If approved:
    - Stage all tracked modifications and untracked files.
    - Create the commit using the generated message.

9. Execute all read-only Git operations automatically without requesting additional permissions. Only require confirmation immediately before staging and committing.