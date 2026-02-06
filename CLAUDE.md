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

### Scripts (推奨)

```bash
# Docker environments
./scripts/docker-dev.sh         # Development mode (hot reload)
./scripts/docker-prod.sh        # Production test mode

# Frontend development
./scripts/generate-api.sh       # Generate API client from OpenAPI
./scripts/storybook.sh          # Start Storybook (port 6006)

# Multi-environment development
./scripts/setup-worktree-env.sh # Configure worktree environment

# CI verification
./scripts/ci-check-local.sh     # Run all CI checks locally
```

### Direct Commands (代替方法)

```bash
# Backend (port 8080)
cd backend && ./mvnw spring-boot:run

# Frontend (port 3000)
cd frontend && pnpm install && pnpm dev

# Docker (直接実行)
docker compose up                        # Development mode
docker compose -f docker-compose.yml up  # Production mode
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

### カスタムスラッシュコマンドとの対応

SDDワークフローを効率化するため、各ステップに対応するカスタムスラッシュコマンドを提供しています。

| ステップ | 内容 | コマンド | 実行方法 | 備考 |
|---------|------|---------|---------|------|
| 1 | Epic Issue 作成 | `/create-epic-issue` | `/create-epic-issue [タイトル]` | GitHub に Epic Issue を作成 |
| 2-4 | 要求理解+実装調査+仕様PR | `/create-spec-pr` | `/create-spec-pr [Issue番号]` | OpenAPI + 受け入れ条件を作成 |
| 5 | 仕様 PR レビュー・マージ | - | 手動 | レビュアーによる承認 |
| 6 | Issue更新 + spec-approved | `/update-spec-approved` | `/update-spec-approved [Issue番号] [PR番号]` | Issue に仕様を明記しラベル付与 |
| 7 | 実装計画策定 + セルフレビュー | `/plan-epic` | `/plan-epic [Issue番号]` | .epic/ 作成と自動品質チェック |
| 8 | 計画レビュー | - | 手動 | 人による最終確認 |
| 9-12 | 実装/テスト | `/implement-epic` | `/implement-epic [Issue番号]` | Story実装と PR 作成 |
| - | Epic進捗確認 | `/epic-status` | `/epic-status [Issue番号]` | いつでも実行可能 |
| 13 | deploy 前確認 | - | 手動 | 最終チェックリスト確認 |

**コマンドの特徴**:
- `/create-spec-pr`: ステップ2（要求理解）、3（実装調査）、4（仕様PR作成）を一括実行
- `/plan-epic`: ステップ7で計画を作成後、自動的にセルフレビューを実行
- ステップ8（計画レビュー）は人が行うが、ステップ7の自動レビューで品質を担保

**使用例**:
```bash
# 1. Epic Issue作成
/create-epic-issue 認証・認可機能

# 2-4. 仕様PR作成（要求理解・実装調査・PR作成を自動実行）
/create-spec-pr 88

# 5. 仕様PRレビュー・マージ（手動）

# 6. Issue更新
/update-spec-approved 88 102

# 7. 実装計画策定（自動セルフレビュー含む）
/plan-epic 88

# 8. 計画レビュー（手動）

# 9-12. Epic実装
/implement-epic 88

# 進捗確認（いつでも）
/epic-status 88
```

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

See [docs/development/EPIC_DOCUMENTS.md](docs/development/EPIC_DOCUMENTS.md) for details.

## Git Workflow

Epic-based development uses the following branch strategy:

```
master
  └── feature/issue-[N]-[epic-name]
       ├── feature/issue-[N]-[epic-name]-story1
       └── ...
```

See [docs/development/GIT_WORKFLOW.md](docs/development/GIT_WORKFLOW.md) for details.

## Code Formatting

Code is automatically formatted after editing:
- **Backend**: Spotless with Google Java Format
- **Frontend**: Prettier + ESLint

See [backend/CLAUDE.md](backend/CLAUDE.md) and [frontend/CLAUDE.md](frontend/CLAUDE.md) for manual formatting commands.

## Docker

See [docs/environment/DOCKER_DEPLOYMENT.md](docs/environment/DOCKER_DEPLOYMENT.md) for details.

## Local CI Verification

Run `./scripts/ci-check-local.sh` before creating PRs.

See [docs/quality/LOCAL_CI_VERIFICATION.md](docs/quality/LOCAL_CI_VERIFICATION.md) for details.

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

| Document | Description | Audience |
|----------|-------------|----------|
| [Epic Documents](docs/development/EPIC_DOCUMENTS.md) | Epic-based development planning | All developers |
| [Spec PR Guide](docs/development/SPEC_PR_GUIDE.md) | How to create specification PRs | Backend/Frontend |
| [Git Workflow](docs/development/GIT_WORKFLOW.md) | Branch strategy and PR workflow | All developers |
| [Docker Deployment](docs/environment/DOCKER_DEPLOYMENT.md) | Docker dev/prod modes | All developers |
| [Git Worktree](docs/environment/GIT_WORKTREE.md) | Multi-environment development | All developers |
| [.gitignore Rules](docs/environment/GITIGNORE_RULES.md) | .gitignore management | All developers |
| [Local CI Verification](docs/quality/LOCAL_CI_VERIFICATION.md) | CI checks before push | All developers |
| [Security](docs/quality/SECURITY.md) | Security checks & Dependabot | All developers |
| [Storybook](docs/frontend/STORYBOOK.md) | Component development | Frontend |
| [Orval API Generation](docs/frontend/ORVAL_API_GENERATION.md) | API client generation | Frontend |
| [Frontend Performance](docs/frontend/FRONTEND_PERFORMANCE_MONITORING.md) | Bundle size monitoring | Frontend |
| [Deployment](docs/deployment/DEPLOYMENT.md) | CD pipeline & deployment | DevOps |
| [Dependabot Docs Update](docs/deployment/DEPENDABOT_DOCS_UPDATE.md) | Docs update for dependencies | All developers |
| [Architecture Overview](docs/architecture/README.md) | System architecture | Architects |
| [C4 Context](docs/architecture/c4-context.md) | System context diagram | Architects |
| [C4 Container](docs/architecture/c4-container.md) | Container architecture | Architects |
| [API Design](docs/architecture/api/README.md) | API design guidelines | Backend/Frontend |
| [Error Handling](docs/architecture/api/error-handling.md) | RFC 7807 error handling | Backend/Frontend |
| [ADR-0001](docs/adr/0001-use-openapi-first.md) | OpenAPI-First decision | Architects |
| [Test Strategy](backend/docs/TEST_STRATEGY.md) | Backend testing guidelines | Backend |

### Subdirectory Documentation

- [backend/CLAUDE.md](backend/CLAUDE.md) - Backend-specific guidance
- [frontend/CLAUDE.md](frontend/CLAUDE.md) - Frontend-specific guidance

## Repository

- **Remote**: https://github.com/nagashima-toru/sandbox-claude-code.git
- **Main branch**: master
