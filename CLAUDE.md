# CLAUDE.md

Claude Code 向けプロジェクトガイド。

## Working Agreement

**警告: このルールに違反した場合は切腹（即時作業停止・ユーザーへの報告・原因究明）とする。**

- **Language**: 説明は日本語。コードコメントは英語
- **Autonomy**: 計画承認後は自律実装。エラーは自己修正
- **Plan Mode**: 複数ファイル変更・新機能・アーキテクチャ変更では必ず使用
- **Testing**: TDD。テストは漏れなくダブりなく（MECE）書く
- **Documents**: 簡潔に書く。冗長な説明・重複は省く
- **Design**: シンプルに実装する。最小限の複雑さで要件を満たす
- **Security**: OWASP Top 10 を意識する
- **Code Quality**: Spotless/Prettier 自動フォーマットを前提とする
- **CI**: PR 作成前に `./scripts/ci-check-local.sh` を必ず実行
- **Retrospective**: 作業を常に振り返ること。そして改善すること

## Project Structure

```
sandbox-claude-code/
├── backend/     # Spring Boot API (Java 25, Maven)
├── frontend/    # Next.js App (TypeScript, pnpm)
├── docs/        # Documentation
└── scripts/     # Utility scripts
```

## 開発プロセス（SDD）

1. `/create-epic-issue` → 2-4. `/create-spec-pr` → 5. レビュー（手動） → 6. `/update-spec-approved`
→ 7. `/plan-epic` → 8. 計画レビュー（手動） → 9-12. `/implement-epic` → 13. deploy 前確認（手動）

詳細: [docs/development/SDD.md](docs/development/SDD.md)

## スキル一覧

### Human-Only

| スキル | 用途 |
|--------|------|
| `/create-epic-issue` | Epic Issue 作成 |
| `/create-spec-pr` | 仕様 PR 作成 |
| `/update-spec-approved` | Issue 更新 + ラベル付与 |
| `/plan-epic` | 実装計画策定 |
| `/implement-epic` | Story 実装 |
| `/setup-worktree` | Worktree 環境構築 |

### Agent-Callable（Task ツール経由で呼び出すこと）

| スキル | 用途 |
|--------|------|
| `/review-implementation` | 実装品質レビュー |
| `/epic-status` | Epic 進捗確認 |
| `/generate-api` | API クライアント生成 |
| `/test-coverage` | カバレッジレポート |
| `/retrospective` | 振り返り |
| `/run-docker` | Docker 環境管理 |
| `/run-storybook` | Storybook 起動 |

## Git

ブランチ: `master → feature/issue-[N]-[epic-name] → ...-story[M]`

PR 作成前: `./scripts/ci-check-local.sh`

## Epic Documents

`.epic/[YYYYMMDD]-[issue-N]-[タイトル]/` に管理。詳細は各スキルを参照。
