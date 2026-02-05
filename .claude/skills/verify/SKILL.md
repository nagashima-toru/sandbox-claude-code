# Verification Skill
1. Run `./mvnw verify`
2. If ArchitectureTest fails, identify the violating import
3. Fix dependency direction (application must not import infrastructure)
4. Re-run verification

Then use by typing: /verify