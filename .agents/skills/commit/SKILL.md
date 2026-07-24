---
name: git-commit
description: Automatically diffs changes, generates a commit message, and requests approval to commit.
---

# Git Commit Automation Skill

## Trigger Phrase
- "Commit my changes"
- "/commit"

## Execution Protocol
1. When triggered, immediately invoke the background system tool to evaluate `git diff --cached`.
2. Analyze the modifications and draft a single-line imperative commit message following Conventional Commits format (e.g., `feat: add feature`, `fix: resolve bug`).
3. Present the proposed commit message to the user.
4. Halt execution and explicitly prompt the user for confirmation before executing the `git commit` terminal command.
