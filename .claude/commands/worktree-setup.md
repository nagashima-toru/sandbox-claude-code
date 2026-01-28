# Git Worktree Environment Setup

Set up a new git worktree with isolated Docker environment.

## Quick Start

```bash
# 1. Create worktree
git worktree add ../sandbox-feature-name feature/name

# 2. Set up environment
cd ../sandbox-feature-name
./scripts/setup-worktree-env.sh

# 3. Start
docker compose up
```

## Port Offsets

| Offset | Frontend | Backend | Use |
|--------|----------|---------|-----|
| 0 | 3000 | 8080 | Main dev |
| 100 | 3100 | 8180 | Feature 1 |
| 200 | 3200 | 8280 | Feature 2 |

## Cleanup

```bash
docker compose down -v
git worktree remove ../sandbox-feature-name
```

See [docs/GIT_WORKTREE.md](../docs/GIT_WORKTREE.md) for detailed documentation.
