# Documentation

このディレクトリには人間向けの知識ドキュメント（アーキテクチャ・ADR・セキュリティポリシー）を格納します。

AI 向けの操作手順・ワークフロー手順書は `.claude/skills/` に配置します。

## ドキュメント一覧

### Architecture

システム設計・構成に関するドキュメント。

- [Architecture Overview](architecture/README.md) - システムアーキテクチャ概要
- [C4 Context Diagram](architecture/c4-context.md) - システムコンテキスト図
- [C4 Container Diagram](architecture/c4-container.md) - コンテナアーキテクチャ図
- [API Design Guidelines](architecture/api/README.md) - API 設計ガイドライン
- [Error Handling](architecture/api/error-handling.md) - RFC 7807 エラーハンドリング仕様

### ADR (Architectural Decision Records)

アーキテクチャ上の意思決定記録。

- [ADR-0001: OpenAPI-First](adr/0001-use-openapi-first.md) - OpenAPI-First アプローチの採用

### Quality

品質・セキュリティに関するポリシードキュメント。

- [テスト戦略（全体）](quality/TEST_STRATEGY.md) - システム全体のテスト方針・E2E 戦略
- [Security](quality/SECURITY.md) - セキュリティポリシーと Dependabot 対応

## ドキュメント配置ルール

| 種類 | 配置先 | 例 |
|------|--------|-----|
| 人間向け知識（アーキテクチャ、ADR、ポリシー） | `docs/` | C4 図、API 設計ガイドライン、セキュリティポリシー |
| AI 操作手順（スクリプト実行、ワークフロー） | `.claude/skills/<skill>/SKILL.md` | /generate-api, /run-docker |
| AI クロスカッティングルール | `CLAUDE.md` / `backend/CLAUDE.md` / `frontend/CLAUDE.md` | .gitignore 階層、空実装ルール |

## 関連ドキュメント

- [Root CLAUDE.md](../CLAUDE.md) - プロジェクト全体のガイダンス（AI 向けスキル一覧含む）
- [Backend CLAUDE.md](../backend/CLAUDE.md) - バックエンド固有のガイダンス
- [Frontend CLAUDE.md](../frontend/CLAUDE.md) - フロントエンド固有のガイダンス
