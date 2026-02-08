---
name: verify
description: Run Maven verify to check architecture tests and code quality. Use after making changes to application or domain layers to ensure clean architecture compliance.
---

# Verification Skill

1. Run `./mvnw verify`
2. If ArchitectureTest fails, identify the violating import
3. Fix dependency direction (application must not import infrastructure)
4. Re-run verification
