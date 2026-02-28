---
name: review-implementation
description: Review implementation plan or Story implementation quality using BEST_PRACTICES.md as reference. Called from plan-epic (plan mode) and implement-epic (story mode).
---

# 実装レビュースキル

## 概要

`/plan-epic` および `/implement-epic` から呼び出される品質レビュースキル。
`backend/docs/BEST_PRACTICES.md` と `frontend/docs/BEST_PRACTICES.md` を参照しながら
明確なレビュー観点で品質チェックを実施します。

---

## 使用方法

```bash
# 実装計画のレビュー（plan-epic から呼び出し）
/review-implementation plan

# Story 実装のレビュー（implement-epic から呼び出し）
/review-implementation story
```

---

## 呼び出し方法

このスキルは **Agent-Callable** です。`plan-epic`（Step 10）および `implement-epic`（Phase 3 Step 5）から Task ツール経由で呼び出されます。

- **推奨**: Task ツール（サブエージェント）経由（メインコンテキスト節約のため）
- **直接実行**: ユーザーがデバッグ目的で単独実行する場合のみ

---

## 実行フロー

### Step 1: BEST_PRACTICES.md の読み込み

レビュー実行前に必ず以下を読み込む:

```bash
Read backend/docs/BEST_PRACTICES.md
Read frontend/docs/BEST_PRACTICES.md
```

### Step 2: モード判定

引数が `plan` の場合は「計画レビュー」、`story` の場合は「Story 実装レビュー」を実施する。

---

## plan レビュー（`/review-implementation plan`）

`/plan-epic` が生成した Epic ドキュメント（`.epic/` 配下）を対象にレビューする。

### レビュー対象の読み込み

```bash
# 最新の Epic ディレクトリを特定
ls -lt .epic/ | head -5

# ドキュメントを読み込む
Read .epic/[最新の Epic ディレクトリ]/requirements.md
Read .epic/[最新の Epic ディレクトリ]/design.md
Read .epic/[最新の Epic ディレクトリ]/overview.md
# 各 Story の tasklist.md も読み込む
Read .epic/[最新の Epic ディレクトリ]/story*/tasklist.md
```

### レビュー観点

#### 1. 構造的観点

- **Story 分割**: 各 Story は独立して実装・テスト可能か
- **Task 粒度**: 1 タスクは 1 時間以内で完了可能か
- **依存関係**: 循環依存がないか、推奨実装順序が適切か
- **完結性**: 各 Story はその Story 内でテストまで完結しているか（「テストは次 Story で」は NG）

#### 2. 完全性観点

- 仕様（OpenAPI・受け入れ条件）の全エンドポイントがカバーされているか
- Backend 実装と Frontend 実装の両方が含まれているか
- テスト Story（または各 Story 内のテストタスク）が含まれているか
- DB マイグレーション（Flyway）が含まれているか（DB 変更がある場合）

#### 3. Backend ベストプラクティス観点（BEST_PRACTICES.md 参照）

- 各 Story/タスクが Clean Architecture レイヤーに正しくマッピングされているか
  - Domain → Application → Infrastructure → Presentation の順序で依存が流れているか
- レイヤー別に適切なテスト種別が計画されているか（BEST_PRACTICES.md §2 の対応表）
  - Domain: Pure Unit テスト
  - UseCase: Mockito 単体テスト
  - Mapper: Testcontainers 統合テスト
  - Controller: MockMvc 統合テスト
- `./mvnw verify` を使用する計画になっているか（`integration-test` ゴールは禁止）

#### 4. Frontend ベストプラクティス観点（BEST_PRACTICES.md 参照）

- API クライアント再生成タスク（`pnpm generate:api`）が含まれているか
- Storybook 作成タスクが含まれているか
- Context を実装する場合、`| undefined` 型パターンの確認が計画されているか
- `pnpm type-check` の実行が計画されているか
- **UIテキスト変更（ラベル・見出し・ボタン・テーブルヘッダー等）を含む Story に、既存 e2e テストの文字列セレクター確認・更新タスクが含まれているか**
  - 含まれていない場合は 🔴 必須修正として報告する

#### 5. ADR 観点

以下に該当する技術的決定に ADR が作成されているか確認する:

- プロジェクト全体（他 Epic にも影響する）技術選定
- 既存の技術スタックに新しいライブラリを追加する場合
- アーキテクチャパターンの変更・追加
- 代替案が複数存在し、選定理由に後から参照価値のある決定

---

## story レビュー（`/review-implementation story`）

`/implement-epic` が実装した Story の実装コードを対象にレビューする。

### レビュー対象の特定

```bash
# 変更されたファイルを確認
git diff --name-only HEAD~1 HEAD
# または
git status
```

### レビュー観点

#### 1. Backend コード品質（BEST_PRACTICES.md 参照）

- **Clean Architecture 依存ルール違反がないか**
  - application 層が infrastructure パッケージを import していないか
  - domain 層が application / infrastructure / presentation を import していないか
- **新規 Java クラスに対応するテストクラスが存在するか**
  - `*UseCase.java` → `*UseCaseTest.java`
  - `*Mapper.java` → `*MapperTest.java`
  - `*Controller.java` → `*ControllerTest.java`
- **テスト構造が AAA パターンか**（Arrange / Act / Assert のコメントがあるか）
- **テストメソッド命名が規則に従っているか**（`methodName_condition_expectedBehavior`）
- **統合テストで `@Transactional` が付与されているか**（テストデータのロールバックのため）
- **パスワードハッシュを使い回していないか**（bcrypt は個別 encode が必要）

#### 2. Frontend コード品質（BEST_PRACTICES.md 参照）

- **named export になっているか**（default export は禁止）
- **Context 型に `| undefined` が含まれているか**
- **新規コンポーネントに Storybook が作成されているか**
- **Orval 生成ファイル（`src/lib/api/generated/`）を直接編集していないか**
- **`useRouter` 等 Next.js Hook を使うコンポーネントのテストで `vi.mock('next/navigation', ...)` が設定されているか**
- **React Query Hook のテストで `QueryClientProvider` でラップされているか**
- **UIテキストを変更した場合、`tests/e2e/` 内の文字列セレクター（`getByRole({ name })`・`getByLabel`・`getByText` 等）を更新しているか**
  - 更新していない場合は 🔴 必須修正として報告する

#### 3. テスト網羅性

- カバレッジ目標（BEST_PRACTICES.md §2 の対応表）が達成されているか:
  - Backend Domain: 90%+
  - Backend UseCase: 85%+
  - Backend Mapper/Controller: 80%+
  - Frontend Utils/Hooks: 90%+
  - Frontend Components: 80%+
- 正常系・異常系・境界値のテストが揃っているか

#### 4. セキュリティ（Backend のみ）

- 認証が必要なエンドポイントに `@PreAuthorize` または Security Config が設定されているか
- MyBatis の XML 定義で `${}` ではなく `#{}` を使用しているか（SQL インジェクション対策）

---

## 出力形式（両モード共通）

```
## 📋 レビュー結果

### ✅ 良い点
- [具体例]

### 🔴 必須修正（Must Fix）
- [問題] → [修正方法]

### 🟡 推奨修正（Should Fix）
- [問題] → [修正方法]

### 🔵 提案（Could Fix）
- [提案内容]

### ⬜ ADR 作成推奨（plan レビューのみ）
- [技術的決定] → docs/adr/ に ADR を作成することを推奨

### 🎯 総合評価
品質: ⭐⭐⭐⭐☆ (4/5)
結論: [承認 / 修正が必要 / 重大な問題あり]
```

### 各評価基準

| 評価 | 説明 | 結論 |
|------|------|------|
| 🔴 必須修正あり | Clean Architecture 違反、テスト未作成、セキュリティ問題など | 修正が必要 |
| 🟡 推奨修正のみ | 改善できるが動作に支障なし | 承認（修正推奨） |
| 🔵 提案のみ | より良い実装への提案 | 承認 |
| 問題なし | すべての観点をクリア | 承認 |

---

## 修正対応

### 自動修正可能な場合

`/review-implementation plan` で明らかな問題（Task 粒度が大きすぎる、必須タスクの抜け漏れなど）は自動修正して再生成する。

### ユーザー確認が必要な場合

以下は `AskUserQuestion` でユーザーに確認してから対応する:

- 技術選定に関わる判断
- スコープの変更
- ADR 作成が必要な技術的決定

---

## 関連スキル

- `/plan-epic` - 実装計画策定（完了後に本スキルの `plan` モードを呼び出し）
- `/implement-epic` - Epic 実装（Story 完了後に本スキルの `story` モードを呼び出し）
