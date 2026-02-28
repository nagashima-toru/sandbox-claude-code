# SDD（仕様駆動開発）詳細ガイド

## 13 ステップのプロセス

1. **Epic Issue 作成**（`/create-epic-issue`）
2. 要求仕様の理解
3. 現在の実装調査
4. **仕様 PR 作成**（`/create-spec-pr`）— OpenAPI + 受け入れ条件のみ。実装コードは含めない
5. 仕様 PR レビュー・マージ（手動）
6. **Issue に仕様を明記** + spec-approved ラベル付与（`/update-spec-approved`）
7. **実装計画策定**（`/plan-epic`）— `.epic/` 作成と自動品質チェック
8. 計画レビュー（手動）
9. 実装・単体テスト実施（`/implement-epic`）
10. 実装・単体テストレビュー & 指摘修正
11. 結合テスト実施
12. 結合テストレビュー & 指摘修正
13. deploy 前確認（手動）

**重要**:

- 仕様が確定してから実装計画を立てる（手戻りを防ぐ）
- ステップ7で計画を立てる際、仕様 PR で追加した API エンドポイントは未実装前提で、バックエンド・フロントエンド両方を Story に含める

## スキルとステップの対応

| ステップ | 内容 | スキル | 備考 |
|---------|------|--------|------|
| 1 | Epic Issue 作成 | `/create-epic-issue` | GitHub に Epic Issue を作成 |
| 2-4 | 要求理解+実装調査+仕様PR | `/create-spec-pr` | OpenAPI + 受け入れ条件を作成 |
| 5 | 仕様 PR レビュー・マージ | — | レビュアーによる承認 |
| 6 | Issue更新 + spec-approved | `/update-spec-approved` | Issue に仕様を明記しラベル付与 |
| 7 | 実装計画策定 + セルフレビュー | `/plan-epic` | .epic/ 作成と自動品質チェック |
| 8 | 計画レビュー | — | 人による最終確認 |
| 9-12 | 実装/テスト | `/implement-epic` | Story 実装と PR 作成 |
| — | Epic進捗確認 | `/epic-status` | いつでも実行可能 |
| — | 品質レビュー | `/review-implementation` | plan-epic/implement-epic から自動呼び出し |
| 13 | deploy 前確認 | — | 最終チェックリスト確認 |

## 使用例

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

## Epic 管理スキルの使い分け

| スキル | 使用タイミング |
|--------|--------------|
| `/epic-status` | Epic 開始前・実装中・完了後の進捗確認 |
| `/implement-epic` | Epic 実装中（未完了 Story がある場合） |
| `/retrospective` | Story 完了時・Epic 完了時の振り返り |

完了済み Epic に `/implement-epic` を実行すると、スキルが自動的に完了を検出し、振り返りや別 Epic の実装を提案する。

## Story PR フォーマット

Story PR（Story ブランチ → Epic ベースブランチ）の PR body には Issue 番号を以下のいずれかの形式で含めること:

```markdown
Story: #[Issue番号]
```

```markdown
Closes #[Issue番号]
```

**正解例**:

```markdown
Story: #133

## Story 概要
Story 8: E2Eテストと最終確認
```

**不正解例**（CI が失敗する）:

```markdown
関連 Issue: #133  ❌
Issue #133        ❌
Ref: #133         ❌
```

PR body 作成後に編集しても既存 CI は再トリガーされない。修正後は空コミットで再トリガー:

```bash
git commit --allow-empty -m "chore: trigger CI" && git push
```

必ず `/implement-epic` スキルまたは `./scripts/create-story-pr.sh` を使用すると正しいフォーマットになる。

## 仕様 PR の空実装ルール

仕様 PR で OpenAPI エンドポイントを追加する場合、Epic ブランチのビルドエラーを防ぐためスタブ実装を含めること:

```java
throw new UnsupportedOperationException("Not implemented yet - Story N");
```

## Epic Documents 構造

```
.epic/[YYYYMMDD]-[issue-N]-[epicタイトル]/
├── requirements.md  # 機能要求
├── design.md        # 技術設計
├── overview.md      # Epic 管理（エントリーポイント）
└── story[N]-[Story名]/
    └── tasklist.md  # Story タスク
```

**例**: `.epic/20260203-88-auth/`
