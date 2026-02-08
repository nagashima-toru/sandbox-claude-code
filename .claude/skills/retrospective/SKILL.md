---
name: retrospective
description: Conduct a retrospective after completing work (Epic, Story, task, or session). Document learnings, improvements, and propose automation/new skills. Use when finishing any significant work or when the user asks to reflect.
---

# Retrospective Skill

作業完了後の振り返りを行い、学びや改善点を `.retrospectives/` に記録します。

## 実行手順

### 1. 対象の明確化

まず、何を振り返るかを確認：
- Epic実装全体（例: Epic #88）
- Story実装（例: Story1 - ユーザー登録API）
- 一般的なタスク（例: Docker環境のセットアップ）
- セッション全体（例: 本日の作業全体）

引数がある場合はそれを使用、ない場合はユーザーに確認。

### 2. 振り返り内容の収集

以下の項目について分析し、具体的に記録：

#### ✅ うまくいったこと
- 成功した取り組みや良かった判断
- 効率的だったプロセス
- 役立ったツールやスキル

#### 📚 学んだこと
- 新しく得た技術的知識
- 気づいたパターンやベストプラクティス
- 失敗から学んだこと

#### ⚠️ 改善点・課題
- うまくいかなかったこと
- 時間がかかった部分とその原因
- 手戻りが発生した理由

#### 🤖 自動化・スキル提案（必須）
- **CLAUDE.md への追加提案**: 繰り返し説明したパターンや規約
- **新規スクリプトの提案**: 手動で実行した定型作業
- **新規スキルの提案**: 今後も使えそうなワークフロー
- **既存スキルの改善提案**: 使いにくかった点

#### 📋 次のアクション
- 今後実施すべきタスク
- 残課題や TODO
- 次回の改善施策

### 3. Epic ディレクトリの特定（Epic/Story の場合のみ）

Epic または Story の振り返りの場合、対応する Epic ディレクトリを特定：

1. **引数から Issue 番号を抽出**
   - 例: `Epic #88`, `Story1: ユーザー登録`, `issue-88` など
   - Issue 番号を取得（例: `88`）

2. **`.epic/` から該当ディレクトリを検索**
   ```bash
   ls -d .epic/*-issue-88-* 2>/dev/null
   ```
   - 見つかった場合: Epic ディレクトリ名を取得（例: `20260203-issue-88-auth`）
   - 見つからない場合: ユーザーに確認するか、`.retrospectives/` 直下に保存

3. **振り返り保存先ディレクトリを作成**
   ```bash
   mkdir -p .retrospectives/20260203-issue-88-auth
   ```

### 4. ファイル作成

**保存場所の決定**:

| 対象 | 保存場所 | ファイル名例 |
|------|---------|-------------|
| Epic 全体 | `.retrospectives/[epic-dir]/` | `epic-20260208-150000.md` |
| Story | `.retrospectives/[epic-dir]/` | `story1-user-registration-20260208-150000.md` |
| 一般タスク | `.retrospectives/` | `docker-setup-20260208-150000.md` |
| セッション | `.retrospectives/` | `session-20260208-150000.md` |

**ファイル名形式**:
- Epic: `epic-YYYYMMDD-HHMMSS.md`
- Story: `story[N]-[story-name]-YYYYMMDD-HHMMSS.md`
- その他: `[topic]-YYYYMMDD-HHMMSS.md`

**内容テンプレート**:

```markdown
# Retrospective: [対象の簡潔な説明]

**日時**: YYYY-MM-DD HH:MM:SS
**対象**: [Epic #N, Story N, タスク名, セッション全体、など]

## ✅ うまくいったこと

- [具体例]
- [具体例]

## 📚 学んだこと

- [具体例]
- [具体例]

## ⚠️ 改善点・課題

- [具体例]
- [具体例]

## 🤖 自動化・スキル提案

### CLAUDE.md への追加提案
- [どのような内容を追加すべきか]

### 新規スクリプトの提案
- [どのようなスクリプトを作成すべきか]

### 新規スキルの提案
- [どのようなスキルを作成すべきか]

### 既存スキルの改善提案
- [どのスキルをどう改善すべきか]

## 📋 次のアクション

- [ ] [TODO 1]
- [ ] [TODO 2]
```

### 5. 完了メッセージ

振り返りファイルのパスを表示し、自動化・スキル提案について特に強調して報告。

**Epic/Story の場合の追加メッセージ**:
```
✅ 振り返りを作成しました: .retrospectives/20260203-issue-88-auth/story1-user-registration-20260208-150000.md

📁 Epic #88 の全振り返り:
- .retrospectives/20260203-issue-88-auth/story1-user-registration-20260208-150000.md
- .retrospectives/20260203-issue-88-auth/story2-login-api-20260207-140000.md

Epic 全体の振り返りを行う際は、これらのファイルを参照してください。
```

## 注意事項

- `.retrospectives/` および Epic サブディレクトリが存在しない場合は作成
- Epic/Story の振り返りは必ず Epic ディレクトリ配下に保存（まとめて確認できるようにする）
- 自動化・スキル提案セクションは**必須**（空にしない）
- 具体的な例を含める（抽象的な記述は避ける）
- Epic 全体の振り返りを行う際は、各 Story の振り返りファイルを参照して総括する