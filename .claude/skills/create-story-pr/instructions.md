# Create Story PR Instructions

When the user invokes `/create-story-pr [args]`:

1. **Parse arguments**: Extract Issue number and Story number from args (if provided)
2. **Find Epic base branch**: Search for `feature/issue-[N]-*` branch (excluding story branches)
3. **Get current branch**: Use as Story branch
4. **Verify template**: Check `.github/PULL_REQUEST_TEMPLATE/story.md` exists
5. **Create PR**: Use `gh pr create --base [epic-base] --head [story-branch] --template .github/PULL_REQUEST_TEMPLATE/story.md`
6. **Report**: Return PR URL and next steps

**CRITICAL**: Always use `--template` option to avoid Implementation Check failures.
