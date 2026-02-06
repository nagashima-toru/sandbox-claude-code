# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a sandbox repository for experimenting with Claude Code.

## Project Structure

```
sandbox-claude-code/
├── backend/     # Spring Boot API (Java 25, Maven)
├── frontend/    # Next.js App (TypeScript, pnpm)
├── docs/        # Documentation
├── scripts/     # Utility scripts
└── nginx.conf   # Reverse proxy configuration
```

## Quick Start

### Local Development (without Docker)

```bash
# Backend (port 8080)
cd backend && ./mvnw spring-boot:run

# Frontend (port 3000)
cd frontend && pnpm install && pnpm dev
```

### Docker Development

```bash
# Development mode (hot reload)
docker compose up

# Production mode test
docker compose -f docker-compose.yml up
```

## Development Environment

- **IDE**: IntelliJ IDEA
- **Java**: JDK 25
- **Node.js**: See frontend/.nvmrc
- **Package Manager**: pnpm (frontend), Maven (backend)

## 開発作業全体のプロセス（SDD: 仕様駆動開発）

1. **Epic Issue 作成**（簡易版）
2. 要求仕様の理解
3. 現在の実装調査
4. **仕様 PR 作成**（OpenAPI + 受け入れ条件）
5. 仕様 PR レビュー・マージ
6. **Issue に仕様を明記** + spec-approved ラベル付与
7. **実装計画策定**（.epic/ 作成）
8. 計画レビュー
9. 実装/単体テスト実施
10. 実装/単体テスト review 実施 & 指摘修正
11. 結合テスト実施
12. 結合テスト review 実施 & 指摘修正
13. deploy 前確認

**重要**: 仕様が確定してから実装計画を立てる（手戻りを防ぐ）

## Epic Documents

開発作業の計画は `.epic/` ディレクトリで管理します。

```
.epic/[YYYYMMDD]-[issue-N]-[epicタイトル]/
├── requirements.md  # 機能要求
├── design.md        # 技術設計
├── overview.md      # Epic 管理（エントリーポイント）
└── story[N]-[Story名]/
    └── tasklist.md  # Story タスク
```

**例**: `.epic/20260203-88-auth/`

See [docs/EPIC_DOCUMENTS.md](docs/EPIC_DOCUMENTS.md) for details.

## Git Workflow

Epic-based development uses the following branch strategy:

```
master
  └── feature/issue-[N]-[epic-name]
       ├── feature/issue-[N]-[epic-name]-story1
       └── ...
```

See [docs/GIT_WORKFLOW.md](docs/GIT_WORKFLOW.md) for details.

## Code Formatting

Code is automatically formatted after editing:
- **Backend**: Spotless with Google Java Format
- **Frontend**: Prettier + ESLint

See [backend/CLAUDE.md](backend/CLAUDE.md) and [frontend/CLAUDE.md](frontend/CLAUDE.md) for manual formatting commands.

## Docker

See [docs/DOCKER_DEPLOYMENT.md](docs/DOCKER_DEPLOYMENT.md) for details.

## Local CI Verification

Run `./scripts/ci-check-local.sh` before creating PRs.

See [docs/LOCAL_CI_VERIFICATION.md](docs/LOCAL_CI_VERIFICATION.md) for details.

## Key Conventions

### Code Style

- **Backend**: Clean Architecture, JUnit tests for all classes
- **Frontend**: Functional components, named exports, co-located tests/stories

### File Naming

| Type | Convention | Example |
|------|------------|---------|
| React Component | PascalCase | `MessageTable.tsx` |
| Hook | camelCase + use | `useMessages.ts` |
| Story | + .stories | `MessageTable.stories.tsx` |
| Test | + .test | `MessageTable.test.tsx` |

### Git

- Branch: `feature/`, `bugfix/`, `hotfix/`
- Run CI check before PR: `./scripts/ci-check-local.sh`

## Working Agreement

This section defines the working agreement between developers and Claude Code for this project.

### Communication Style

- **Detail Level**: Moderate explanation (explain what was done and why briefly)
- **Confirmation Frequency**: Only for critical decisions (destructive operations, architecture changes, security-related changes)
- **Language**: Primarily Japanese (explanations in Japanese, code comments in English, technical terms can be in English)

### Work Approach

- **Autonomy**: High (implement autonomously after plan approval, self-correct errors, report only when stuck)
- **Plan Mode**: Use proactively (always plan for multi-file changes, new features, architecture changes)
- **Error Handling**: Self-correction (investigate and fix common errors independently, consult only for complex cases or specification decisions)
- **Testing**: TDD (Test-Driven Development)

### Development Process

- **Git Workflow**: Automatic (create necessary branches after plan approval, commit at each story completion)
- **Documentation**: Detailed before implementation (create .epic/ documents first, finalize API specs before implementation)
- **PR Creation**: After implementation completion (create PR after implementation, tests pass, and CI checks pass)
- **CI Checks**: Before PR creation (always run `./scripts/ci-check-local.sh` before creating PR)

### Quality Standards

- **Code Quality**: Strict compliance (strictly follow CLAUDE.md conventions, assume Spotless/Prettier auto-formatting)
- **Security**: High level (be aware of OWASP Top 10, actively prevent SQL injection, XSS, command injection, etc.)
- **Test Quality**: High coverage (unit tests for all classes, add integration tests for critical features)
- **Performance**: Basic consideration (avoid obvious bottlenecks, but don't over-optimize)

## Documentation Index

| Document | Description |
|----------|-------------|
| [Epic Documents](docs/EPIC_DOCUMENTS.md) | Epic-based development planning |
| [Spec PR Guide](docs/SPEC_PR_GUIDE.md) | How to create specification PRs |
| [Git Workflow](docs/GIT_WORKFLOW.md) | Branch strategy and PR workflow |
| [Docker Deployment](docs/DOCKER_DEPLOYMENT.md) | Docker dev/prod modes, workflows, troubleshooting |
| [Git Worktree](docs/GIT_WORKTREE.md) | Multi-environment development |
| [Local CI Verification](docs/LOCAL_CI_VERIFICATION.md) | CI checks, coverage, hooks |
| [Storybook](docs/STORYBOOK.md) | Component development, MSW, a11y |
| [Orval API Generation](docs/ORVAL_API_GENERATION.md) | API client generation |
| [Test Strategy](backend/docs/TEST_STRATEGY.md) | Backend testing guidelines |
| [Security](docs/SECURITY.md) | Security guidelines |

### Subdirectory Documentation

- [backend/CLAUDE.md](backend/CLAUDE.md) - Backend-specific guidance
- [frontend/CLAUDE.md](frontend/CLAUDE.md) - Frontend-specific guidance

## Repository

- **Remote**: https://github.com/nagashima-toru/sandbox-claude-code.git
- **Main branch**: master
