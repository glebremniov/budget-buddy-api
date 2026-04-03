Create a branch, commit all staged/unstaged changes, push, and open a PR against `main`. Also bump the patch version in `gradle.properties`.

**Steps:**
1. Run `git diff --stat` and `git status` to understand what changed
2. Increment the `version` in `gradle.properties` by one patch level (e.g. `0.1.5` → `0.1.6`) — read the file first
3. Propose a short, meaningful branch name based on the changes (prefix: `feat/`, `fix/`, `chore/`, `refactor/` as appropriate)
4. `git checkout -b <branch-name>`
5. Stage and commit with a concise message that explains *why*, not just *what*
6. `git push -u origin <branch-name>`
7. Create a PR against `main` using `gh pr create` with a clear title and a summary body (what changed + test plan)
8. Return the PR URL

**Notes:**
- If already on a feature branch (not `main` or `bugfix/*`), skip branch creation and just commit + push + open PR
- Follow the repo's commit style: imperative mood, no period at end
