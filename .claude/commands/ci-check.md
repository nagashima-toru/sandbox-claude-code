# Local CI Verification

Run local CI checks before pushing to ensure GitHub CI will pass.

## Quick Commands

```bash
# Standard check (recommended before PR)
./scripts/ci-check-local.sh

# Backend only
./scripts/ci-check-local.sh --backend-only

# Frontend only
./scripts/ci-check-local.sh --frontend-only

# Full check with E2E
./scripts/ci-check-local.sh --e2e
```

## What Gets Checked

### Backend
- Maven build and tests
- Code coverage (≥80% lines, ≥75% branches)
- SpotBugs static analysis
- Checkstyle validation

### Frontend
- ESLint and Prettier
- TypeScript type checking
- Vitest tests with coverage
- Production build

## Golden Rule

If `./scripts/ci-check-local.sh` passes locally, CI should pass.

See [docs/LOCAL_CI_VERIFICATION.md](../docs/LOCAL_CI_VERIFICATION.md) for detailed documentation.
