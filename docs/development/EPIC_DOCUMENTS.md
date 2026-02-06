# Epic Documents

開発作業の計画は `.epic/` ディレクトリで管理します。このディレクトリは `.gitignore` に含まれており、構成管理対象外です。

---

## ディレクトリ構成

```
.epic/
└── [YYYYMMDD]-[issue-N]-[epicタイトル]/
    ├── requirements.md           # 機能要求
    ├── design.md                 # 実装アプローチと変更内容
    ├── overview.md               # 全 Story の概要とチェックリスト
    └── story[N]-[Story名]/       # Story ごとのディレクトリ
        └── tasklist.md           # その Story の詳細タスクリスト
```

**例:**

```
.epic/20260203-88-auth/
├── requirements.md
├── design.md
├── overview.md
├── story1-user-management/
│   └── tasklist.md
├── story2-jwt-auth/
│   └── tasklist.md
└── story3-auth-endpoints/
    └── tasklist.md
```

**命名規則**:

- `YYYYMMDD`: 開始日
- `issue-N`: GitHub Issue 番号
- `epicタイトル`: 簡潔なタイトル（英数字、ハイフン）

---

## 各ファイルの役割

### requirements.md - 機能要求定義

**目的**: 何を実装するかを明確にする

**内容**:

- 概要と目的
- スコープ（Phase 定義）
- 機能要求（認証・認可・ユーザー管理など）
- 非機能要求（セキュリティ・パフォーマンス・可用性）
- エラーハンドリング仕様
- 受け入れ基準
- 参考資料

**作成タイミング**: 要求仕様の理解 → 実装計画策定の初期段階

---

### design.md - 技術設計

**目的**: どう実装するかを明確にする

**内容**:

- 技術選定（フレームワーク・ライブラリ）
- アーキテクチャ設計（パッケージ構成・フロー図）
- データベース設計（テーブル定義・マイグレーション）
- 設定ファイル（application.yml など）
- OpenAPI 仕様変更
- フロントエンド設計
- テスト戦略
- セキュリティ考慮事項

**作成タイミング**: 実装計画策定（requirements.md の後）

---

### overview.md - Epic 管理

**目的**: Epic 全体の進行を管理する（エントリーポイント）

**内容**:

- Epic 概要（目的・完了条件）
- ドキュメント構成（requirements.md, design.md へのリンク）
- Story 構成（全 Story の一覧・見積もり・依存関係）
- PR 作成ガイド（テンプレートへのリンク）
- ブランチ戦略（CLAUDE.md への参照）
- 進捗管理（全 Story × 全 Task のチェックリスト）

**作成タイミング**: 実装計画策定完了時（Story 分割後）

---

### story[N]-[Story名]/tasklist.md - Story タスク管理

**目的**: 1つの Story の詳細タスクを管理する

**内容**:

- Story 概要と受け入れ基準
- Task リスト（1時間以内の作業単位）
- 各 Task の見積もり・作業内容・完了条件
- 進捗記録（開始日時・完了日時・実績時間）

**作成タイミング**: Story 分割時（overview.md と同時）

---

## 作成ルール

- **ディレクトリ名**: `YYYYMMDD-issue-N-epicタイトル` 形式（例: `20260203-88-auth`）
- **Task の粒度**: 1時間以内の作業単位に分割

---

## SDD（仕様駆動開発）における作成フロー

Epic Documents は**仕様が確定してから**作成します。

```
1. Epic Issue 作成（GitHub）
   ├─ 簡易的な概要
   ├─ 背景・目的
   └─ Issue 番号が確定（例: #88）
   ↓
2. 要求仕様の理解
   ↓
3. 現在の実装調査
   ↓
4. 仕様 PR 作成・マージ
   ├─ OpenAPI 仕様（specs/openapi/）
   └─ 受け入れ条件（specs/acceptance/）
   ↓
5. Issue に仕様を明記
   ├─ 仕様 PR へのリンク
   ├─ OpenAPI 仕様ファイルへのリンク
   └─ 受け入れ条件ファイルへのリンク
   ↓
6. spec-approved ラベル付与
   ↓
7. 📝 Epic Documents 作成（ここから！）
   ├─ .epic/[YYYYMMDD]-[issue-N]-[title]/ 作成
   ├─ requirements.md（確定した仕様を元に）
   ├─ design.md（技術選定・アーキテクチャ）
   ├─ Story 分割 & story[N]/tasklist.md
   └─ overview.md（Epic 全体のまとめ）
   ↓
8. 計画レビュー
   ↓
9. Epic ベースブランチ作成
   ↓
10. 実装開始
```

**重要**: 仕様が確定してから実装計画を立てることで、手戻りを防ぎます。

---

## 作業時の使い方

1. `overview.md` で全体像と依存関係を把握（エントリーポイント）
2. 実装する Story のディレクトリに移動
3. その Story の `tasklist.md` を見ながらタスクを進める
4. タスク完了時にチェックボックスをマーク
5. Story 完了後、`overview.md` の該当 Story をチェック

---

## 更新タイミング

**重要**: 進捗を忘れないために、作業完了時に**必ず更新**すること！

### Story 実装中

**タスク開始時:**

- `tasklist.md` の該当タスクのチェックボックスにチェック

**タスク完了時:**

- `tasklist.md` の該当タスクのチェックボックスにチェック

### Story 完了時（PR作成直後）

**必須の更新:**

1. **`tasklist.md` の進捗セクション**を更新：

   ```markdown
   ## 進捗

   - 開始日時: 2026-02-04 00:06
   - 完了日時: 2026-02-04 00:15
   - 実績時間: 約10分
   - メモ: 全タスク完了。テスト22件全て成功。PR #104 作成済み。
   ```

2. **`tasklist.md` の全タスクの完了条件**にチェック：

   ```markdown
   **完了条件**:
   - [x] User クラスが作成されている
   - [x] Role enum が定義されている
   - [x] 単体テストが作成されている
   ```

3. **`overview.md` の該当 Story のタスク**にチェック：

   ```markdown
   ### Story 1: ユーザー管理基盤 ✅
   - [x] Task 1.1: ユーザードメインモデルの作成
   - [x] Task 1.2: ユーザーリポジトリインターフェースの作成
   - [x] Task 1.3: ユーザーテーブルの Flyway マイグレーション作成
   - [x] Task 1.4: MyBatis Mapper と Repository 実装の作成
   ```

### Note

- `.epic/` ディレクトリは `.gitignore` に含まれるため、**Git でコミット不要**
- ローカルでの進捗管理・計画用
- 各開発者が自分のペースで更新できる
