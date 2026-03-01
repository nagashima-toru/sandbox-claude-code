# SDD（仕様駆動開発）詳細ガイド

## ティアシステム

Spec PR のレビュー時に Epic のティアを確定する。
**ティア判定は Claude が提案し、ユーザーが承認する**（仕様決定はユーザーの責任）。

| ティア | 判定基準 | 計画承認後の実装方式 |
|--------|---------|-------------------|
| **Major** | API スキーマ変更・DB スキーマ変更・新ドメイン追加・外部サービス連携変更 | Story ブランチ × N → Story PR × N → Epic PR（現行通り） |
| **Minor** | 単一機能追加・既存機能変更（API 変更なし）・複数ファイルにまたがる実装変更 | 単一 feature ブランチで全 Story を自律実装 → Epic PR のみ |
| **Micro** | docs・tests・configs・スキルのみの変更。コードの動作に影響なし | 単一 feature ブランチで全 Story を自律実装 → Epic PR のみ |

ユーザーの確認ポイント:
- **Major**: 仕様 PR レビュー + 計画レビュー + Story PR レビュー × N + Epic PR レビュー
- **Minor/Micro**: 仕様 PR レビュー + 計画レビュー + **Epic PR レビューのみ**（Story PR なし）

---

## プロセスフロー

### ステップ 1〜8: 全ティア共通

1. **Epic Issue 作成**（`/create-epic-issue`）
2. 要求仕様の理解
3. 現在の実装調査
4. **仕様 PR 作成**（`/create-spec-pr`）— OpenAPI + 受け入れ条件 + **ティア判定を含む**
5. 仕様 PR レビュー・マージ（手動）— **ティアを承認**
6. **Issue 更新 + spec-approved + ティアラベル付与**（`/update-spec-approved`）
7. **実装計画策定**（`/plan-epic`）— `.epic/` 作成と自動品質チェック
8. 計画レビュー（手動）

### ステップ 9〜: ティア別分岐

**Major:**

```
9.  Story ブランチ作成（feature/issue-N-name-storyX）
10. Story 実装・テスト
11. Story PR 作成 → レビュー・マージ（手動）
    └── 全 Story 完了まで繰り返し
12. Epic PR 作成（feature/issue-N-name → master）
13. Deploy 前確認（手動）
```

**Minor / Micro:**

```
9.  Claude が feature/issue-N-name ブランチを作成
10. 全 Story を順次自律実装（Story ブランチなし・Story PR なし）
11. CI チェック実行（./scripts/ci-check-local.sh）
12. Epic PR 作成（feature/issue-N-name → master）
13. Epic PR レビュー（手動）← このステップのみユーザー確認
```

---

## スキルとステップの対応

| ステップ | 内容 | スキル | 備考 |
|---------|------|--------|------|
| 1 | Epic Issue 作成 | `/create-epic-issue` | GitHub に Epic Issue を作成 |
| 2-4 | 要求理解 + 実装調査 + 仕様 PR + ティア判定 | `/create-spec-pr` | OpenAPI + 受け入れ条件 + ティア提案 |
| 5 | 仕様 PR レビュー・マージ | — | レビュアーによる承認・ティア確認 |
| 6 | Issue 更新 + spec-approved + ティアラベル | `/update-spec-approved` | tier:major / tier:minor / tier:micro ラベルを付与 |
| 7 | 実装計画策定 + セルフレビュー | `/plan-epic` | .epic/ 作成（ティア情報を overview.md に記録） |
| 8 | 計画レビュー | — | 人による最終確認 |
| 9-12 | 実装/テスト | `/implement-epic` | **Major**: Story PR フロー / **Minor/Micro**: 自律実装 → Epic PR |
| — | Epic 進捗確認 | `/epic-status` | いつでも実行可能 |
| — | 品質レビュー | `/review-implementation` | plan-epic/implement-epic から自動呼び出し |
| 13 | Deploy 前確認 / Epic PR レビュー | — | Major: 最終チェック / Minor/Micro: Epic PR レビュー |

---

## 使用例

### Major ティアの場合（認証・認可機能）

```bash
# 1. Epic Issue 作成
/create-epic-issue 認証・認可機能

# 2-4. 仕様 PR 作成（ティア判定を含む）
/create-spec-pr 88
# → Claude が「Major（APIスキーマ変更あり）」と提案

# 5. 仕様 PR レビュー・マージ + ティア承認（手動）

# 6. Issue 更新（tier:major ラベルを付与）
/update-spec-approved 88 102

# 7. 実装計画策定
/plan-epic 88

# 8. 計画レビュー（手動）

# 9-12. Epic 実装（Story PR × N）
/implement-epic 88

# 進捗確認（いつでも）
/epic-status 88
```

### Minor/Micro ティアの場合（E2E テスト戦略見直し）

```bash
# 1. Epic Issue 作成
/create-epic-issue E2Eテスト戦略見直し

# 2-4. 仕様 PR 作成（ティア判定を含む）
/create-spec-pr 165
# → Claude が「Micro（docs・tests・configs のみ変更）」と提案

# 5. 仕様 PR レビュー・マージ + ティア承認（手動）

# 6. Issue 更新（tier:micro ラベルを付与）
/update-spec-approved 165 170

# 7. 実装計画策定
/plan-epic 165

# 8. 計画レビュー（手動）

# 9-12. 自律実装（Story PR なし・Claude が全 Story を実装して Epic PR 作成）
/implement-epic 165

# 13. Epic PR レビュー（手動）
```

---

## Story PR フォーマット（Major ティアのみ）

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

---

## 仕様 PR の空実装ルール

仕様 PR で OpenAPI エンドポイントを追加する場合、Epic ブランチのビルドエラーを防ぐためスタブ実装を含めること:

```java
throw new UnsupportedOperationException("Not implemented yet - Story N");
```

---

## Epic Documents 構造

```
.epic/[YYYYMMDD]-[issue-N]-[epicタイトル]/
├── requirements.md  # 機能要求
├── design.md        # 技術設計
├── overview.md      # Epic 管理（エントリーポイント）※ ティア情報を含む
└── story[N]-[Story名]/
    └── tasklist.md  # Story タスク
```

`overview.md` にはティア情報を必ず記録する:

```markdown
## Epic 概要

**ティア**: Micro（docs・tests・configs のみ変更）
**実装方式**: 単一 feature ブランチ → Epic PR のみ（Story PR なし）
```

**例**: `.epic/20260203-88-auth/`