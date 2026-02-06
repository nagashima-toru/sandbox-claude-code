# Claude Code Configuration

This directory contains Claude Code configuration for this project.

## Automatic Code Formatting

The project is configured to automatically format code when Claude Code edits files.

### Configuration File

Add the following to `.claude/settings.local.json` (this file is gitignored):

```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit",
        "hooks": [
          {
            "type": "command",
            "command": "./scripts/format-code.sh \"${filePath}\"",
            "async": true,
            "statusMessage": "Formatting code..."
          }
        ]
      },
      {
        "matcher": "Write",
        "hooks": [
          {
            "type": "command",
            "command": "./scripts/format-code.sh \"${filePath}\"",
            "async": true,
            "statusMessage": "Formatting code..."
          }
        ]
      }
    ]
  },
  "permissions": {
    "allow": [
      "Bash(./scripts/format-code.sh:*)",
      "Bash(./mvnw spotless:apply:*)"
    ]
  }
}
```

### How It Works

1. When Claude Code uses the `Edit` or `Write` tool to modify files
2. The `PostToolUse` hook runs `scripts/format-code.sh`
3. The script detects the file type and runs the appropriate formatter:
   - Backend (Java): `./mvnw spotless:apply` (Google Java Format)
   - Frontend (TypeScript/JavaScript): `pnpm prettier --write`

### Benefits

- Automatic code formatting after every edit
- Reduces CI formatting errors
- Consistent code style across the codebase
- Same developer experience for both frontend and backend

## Related Files

- `scripts/format-code.sh` - Formatter script
- `backend/pom.xml` - Spotless Maven plugin configuration
- `backend/checkstyle.xml` - Custom Checkstyle rules compatible with Google Java Format
- `frontend/.lintstagedrc.js` - Frontend pre-commit formatting rules
