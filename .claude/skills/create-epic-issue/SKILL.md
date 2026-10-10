---
name: create-epic-issue
description: "[Human-Only] Create a GitHub Epic Issue and confirm the Issue number for SDD workflow."
disable-model-invocation: true
---

# Epic Issue 作成コマンド

## ⛔ エージェント呼び出し禁止

このスキルは **ユーザーが直接実行するスキル** です。
他のスキルやエージェント（Task ツール）から呼び出してはいけません。

**禁止理由**:
- ユーザーとの対話（AskUserQuestion）を前提とする（タイトル・背景・目的・スコープの収集）
- GitHub Issue 作成という副作用を含む
- 誤った呼び出しで不要な Issue が作成される可能性がある

**エージェントがこのスキルを実行しようとした場合**:
即座に停止し、ユーザーに「このスキルはユーザー専用です。`/create-epic-issue` を直接実行してください」と報告する。

---

## 概要

GitHub に Epic Issue を作成し、Issue 番号を確定させます。

**対応ステップ**: 1. Epic Issue 作成

---

## 使用方法

```bash
# Epic タイトルを指定して実行
/create-epic-issue 認証・認可機能
```

---

## 実行フロー

### 1. Epic タイトルの確認

**引数あり**: 引数をEpicタイトルとして使用
**引数なし**: AskUserQuestionでタイトルを収集

### 2. 必要な情報の収集

AskUserQuestion を使って以下を収集：

1. **背景（Background）**
   - なぜこの機能が必要か
   - 現状の課題

2. **目的（Purpose）**
   - 何を達成するか
   - 期待される効果

3. **スコープ（Scope）**
   - Phase定義（何を含むか）
   - 含むもの/含まないもの

### 3. Issue本文の作成

以下のMarkdown形式で本文を作成：

```markdown
## 背景

[ユーザーが入力した背景]

## 目的

[ユーザーが入力した目的]

## スコープ

### Phase 1（本Epic対象）
[ユーザーが入力したPhase 1の内容]

### 将来の検討事項
[ユーザーが入力した Phase 2以降の内容]

## 開発プロセス

このEpicは仕様駆動開発（SDD）に従って進めます：

1. [x] Epic Issue 作成（このIssue）
2. [ ] 要求仕様の理解
3. [ ] 現在の実装調査
4. [ ] 仕様 PR 作成（OpenAPI + 受け入れ条件）
5. [ ] 仕様 PR レビュー・マージ
6. [ ] Issue に仕様を明記 + spec-approved ラベル付与
7. [ ] 実装計画策定（.epic/ 作成）
8. [ ] 計画レビュー
9. [ ] 実装/単体テスト実施
10. [ ] 実装/単体テスト review 実施 & 指摘修正
11. [ ] 結合テスト実施
12. [ ] 結合テスト review 実施 & 指摘修正
13. [ ] deploy 前確認

## 参考資料

- [CLAUDE.md](./CLAUDE.md) - 開発プロセス全体
```

### 4. GitHub Issue の作成

```bash
gh issue create \
  --title "[Epic] [Epicタイトル]" \
  --body "[作成した本文]" \
  --label "epic"
```

### 5. 結果の表示

作成されたIssue番号とURLを表示し、次のステップをガイド：

```
✅ Epic Issue を作成しました

Issue: #88
URL: https://github.com/nagashima-toru/sandbox-claude-code/issues/88

## Next Steps

**仕様 PR 作成**（ステップ2-4を一括実行）
```bash
/create-spec-pr 88
```

このコマンドは以下を自動的に実行します：

- **Step 2**: 要求仕様の理解（Issue内容の分析）
- **Step 3**: 現在の実装調査（既存コード・API・DB調査）
- **Step 4**: 仕様PR作成（OpenAPI + 受け入れ条件）

要求理解と実装調査を踏まえた上で、適切な仕様を作成します。

```

---

## エラーハンドリング

### GitHub CLI 未認証
```bash
gh auth status
# 未認証の場合
gh auth login
```

### Issue 作成失敗

- ネットワーク接続を確認
- GitHub のアクセス権限を確認
- リポジトリが正しいか確認

---

## 注意事項

- Issue番号は後続のコマンド（`/create-spec-pr`、`/plan-epic`）で使用します
- Issue作成後は、必ず要求仕様の理解と現在の実装調査を行ってから次のステップ（仕様PR作成）に進んでください
- Epic Issue は簡易的な概要を記載し、詳細は仕様PRで定義します

---

## 関連コマンド

- `/create-spec-pr [Issue番号]` - 仕様PR作成（次のステップ）
- `/epic-status [Issue番号]` - Epic進捗確認
