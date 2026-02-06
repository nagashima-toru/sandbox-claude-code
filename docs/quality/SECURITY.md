# Security Documentation

このドキュメントでは、本プロジェクトのセキュリティとCI/CDに関する情報を提供します。

## 目次

- [概要](#概要)
- [自動セキュリティチェック](#自動セキュリティチェック)
- [依存関係管理](#依存関係管理)
- [セキュリティアラートへの対応](#セキュリティアラートへの対応)
- [ローカルでのセキュリティチェック](#ローカルでのセキュリティチェック)
- [ベストプラクティス](#ベストプラクティス)

## 概要

本プロジェクトでは、フロントエンド（Next.js/TypeScript）とバックエンド（Spring Boot/Java）の両方に対して、自動化されたセキュリティチェックを実装しています。

### セキュリティ対策の柱

1. **依存関係の脆弱性スキャン**
   - フロントエンド: pnpm audit
   - バックエンド: Maven dependency-check

2. **コードセキュリティスキャン**
   - CodeQL による静的解析（JavaScript/TypeScript、Java）

3. **依存関係の自動更新**
   - Dependabot による定期的な依存関係更新

4. **ライセンスコンプライアンス**
   - npm パッケージのライセンスチェック

5. **未使用依存関係の検出**
   - depcheck による不要なパッケージの検出

## 自動セキュリティチェック

### Frontend Security Checks

**ワークフロー**: `.github/workflows/frontend-security.yml`

#### 実行タイミング

- Push/PR: `frontend/package.json`, `frontend/pnpm-lock.yaml`, または `frontend/pnpm-workspace.yaml` の変更時
- スケジュール: 毎週月曜日 0:00 UTC（日本時間 9:00）
- 手動実行: GitHub Actions の UI から実行可能

#### チェック内容

##### 1. Dependency Vulnerability Audit

```bash
pnpm audit
```

**評価基準**:

- **Critical/High**: CI が失敗 ❌
- **Moderate/Low**: 警告のみ（CI は成功）⚠️

**成果物**: `security-audit-report`（JSON/テキスト形式）

##### 2. License Compliance Check

```bash
pnpm licenses list
```

**チェック内容**:

- 禁止ライセンス（GPL系など）の検出
- 全パッケージのライセンス一覧生成

**成果物**: `license-report`（JSON形式）

##### 3. Unused Dependencies Check

```bash
npx depcheck
```

**チェック内容**:

- 使用されていない dependencies の検出
- 使用されていない devDependencies の検出

**成果物**: `depcheck-report`（JSON形式）

### CodeQL Security Scan

**ワークフロー**: `.github/workflows/codeql.yml`

#### 実行タイミング

- Push/PR: ソースコード（`.java`, `.ts`, `.tsx`, `.js`, `.jsx`）の変更時
- スケジュール: 毎週月曜日 0:00 UTC
- 手動実行: GitHub Actions の UI から実行可能

#### スキャン対象

- **Java**: バックエンドコード
- **JavaScript/TypeScript**: フロントエンドコード

#### クエリセット

- `security-extended`: セキュリティに関する拡張クエリ
- `security-and-quality`: セキュリティと品質に関するクエリ

#### 結果の確認

GitHub の **Security** タブ → **Code scanning** から確認できます。

### Backend Security Checks

**ワークフロー**: `.github/workflows/backend-ci.yml`

バックエンドのセキュリティチェックについては、`backend/CLAUDE.md` を参照してください。

## 依存関係管理

### Dependabot

**設定ファイル**: `.github/dependabot.yml`

#### 更新スケジュール

| エコシステム | ディレクトリ | 頻度 | 実行日時 |
|------------|------------|------|---------|
| npm | `/frontend` | 毎週 | 月曜日 9:00 JST |
| maven | `/backend` | 毎週 | 月曜日 9:00 JST |
| github-actions | `/` | 毎月 | 月曜日 9:00 JST |
| docker | `/backend` | 毎月 | 月曜日 9:00 JST |

#### 自動PR設定

- **上限**: npm/maven は 10件、GitHub Actions/Docker は 5件
- **レビュアー**: @nagashima-toru（プロジェクトに応じて変更）
- **ラベル**: `dependencies` + エコシステム固有のラベル
- **コミットメッセージ**: `chore(deps): ...`

#### 自動マージ戦略

**推奨設定**:

1. **Patch更新**: 自動マージ候補（例: `1.0.0` → `1.0.1`）
2. **Minor更新**: 自動マージ候補（例: `1.0.0` → `1.1.0`）
3. **Major更新**: 手動レビュー必須（例: `1.0.0` → `2.0.0`）

**条件**:

- ✅ すべてのCIチェックが通過
- ✅ セキュリティスキャンで問題なし
- ✅ テストがすべて成功

#### 自動マージの有効化（オプション）

GitHub の設定で Dependabot の自動マージを有効にできます:

```bash
gh api repos/{owner}/{repo}/automated-security-fixes -X PUT
```

または、GitHub UI で:

1. Settings → Security → Dependabot → Enable Dependabot security updates

## セキュリティアラートへの対応

### 脆弱性が検出された場合

#### 1. 通知の確認

GitHub からメール通知またはリポジトリの **Security** タブで確認します。

#### 2. 影響範囲の評価

- **重大度**: Critical、High、Moderate、Low
- **影響を受けるコンポーネント**: どのパッケージか
- **攻撃ベクトル**: どのように悪用される可能性があるか

#### 3. 対応優先度

| 重大度 | 対応期限 | アクション |
|--------|---------|----------|
| Critical | 即時 | 直ちにパッチ適用または回避策を実施 |
| High | 1週間以内 | 速やかにパッチ適用 |
| Moderate | 1ヶ月以内 | 次回の定期更新で対応 |
| Low | 3ヶ月以内 | 定期メンテナンスで対応 |

#### 4. 修正方法

##### フロントエンド

```bash
cd frontend

# 脆弱性のあるパッケージを確認
pnpm audit

# 自動修正を試みる（pnpm-workspace.yaml に overrides を追加）
pnpm audit --fix

# 修正を適用
pnpm install

# 修正を確認
pnpm audit

# 特定のパッケージを更新
pnpm update <package-name>

# メジャーバージョンアップが必要な場合
pnpm add <package-name>@latest
```

**Note**: `pnpm audit --fix` は `pnpm-workspace.yaml` に `overrides` セクションを追加します。これにより、間接的な依存関係（transitive dependencies）のバージョンを強制的に上書きできます。

##### バックエンド

```bash
cd backend

# 依存関係を更新
./mvnw versions:use-latest-versions

# セキュリティチェックを実行
./mvnw verify
```

#### 5. テストとデプロイ

1. ローカルで修正を適用
2. すべてのテストを実行
3. CI/CDパイプラインでの検証
4. ステージング環境でのテスト
5. プロダクション環境へのデプロイ

### Dependabot PRの対応フロー

1. **PR の内容を確認**
   - 変更内容（パッケージとバージョン）
   - リリースノート
   - 破壊的変更の有無

2. **CI チェックの確認**
   - すべてのテストが成功しているか
   - セキュリティスキャンに問題がないか
   - ビルドが成功しているか

3. **マージまたはクローズ**
   - 問題なければマージ
   - 破壊的変更がある場合は、コードの修正を別PR で実施

## ローカルでのセキュリティチェック

### フロントエンド

#### 依存関係の脆弱性スキャン

```bash
cd frontend
pnpm audit
```

**詳細レポート**:

```bash
pnpm audit --json > audit-report.json
```

#### ライセンスチェック

```bash
pnpm licenses list
```

#### 未使用依存関係のチェック

```bash
pnpm add -D depcheck
npx depcheck
```

### バックエンド

```bash
cd backend
./mvnw verify
```

### 手動 CodeQL スキャン（ローカル）

CodeQL CLI をインストールして実行することも可能です:

```bash
# CodeQL CLI のインストール (macOS)
brew install codeql

# データベース作成
codeql database create codeql-db --language=javascript

# クエリ実行
codeql database analyze codeql-db --format=sarif-latest --output=results.sarif
```

詳細は [CodeQL CLI ドキュメント](https://codeql.github.com/docs/codeql-cli/)を参照してください。

## ベストプラクティス

### 1. 依存関係の最小化

- 必要最小限のパッケージのみを使用
- 定期的に未使用依存関係をクリーンアップ

### 2. 定期的な更新

- 月に一度は依存関係を見直し
- Dependabot PR を積極的にマージ

### 3. セキュリティパッチの優先適用

- Critical/High の脆弱性は即座に対応
- セキュリティパッチは通常のリリースサイクルを待たずに適用

### 4. シークレット管理

- `.env` ファイルを `.gitignore` に追加
- 環境変数でAPIキーや認証情報を管理
- GitHub Secrets を活用

### 5. コードレビュー

- すべての依存関係更新を人間がレビュー
- 自動マージは慎重に設定

### 6. セキュリティ教育

- チームメンバーにセキュリティベストプラクティスを共有
- 脆弱性レポートの読み方を学ぶ

## トラブルシューティング

### pnpm audit でエラーが出る

**問題**: 脆弱性が見つかったがパッチが利用できない

**解決策**:

1. `pnpm audit --fix` で自動修正を試す（推奨）
2. 手動でパッケージを更新
3. 代替パッケージの検討
4. 一時的な回避策（override）の使用

```yaml
# pnpm-workspace.yaml
overrides:
  'vulnerable-package@<safe-version': '>=safe-version'
  '@scope/package@vulnerable-range': 'safe-version'
```

**Note**: `pnpm audit --fix` を実行すると、`pnpm-workspace.yaml` に自動的に `overrides` が追加されます。その後、`pnpm install` で適用してください。

### CodeQL スキャンが失敗する

**問題**: ビルドエラーで CodeQL が実行できない

**解決策**:

1. ローカルでビルドが成功することを確認
2. 依存関係を最新に更新
3. CodeQL ワークフローのビルドステップを確認

### Dependabot PR が多すぎる

**問題**: PRが大量に作成される

**解決策**:

1. `open-pull-requests-limit` を調整
2. 更新頻度を変更（weekly → monthly）
3. グループ化設定を検討

## 参考リンク

- [GitHub Dependabot Documentation](https://docs.github.com/en/code-security/dependabot)
- [GitHub CodeQL Documentation](https://codeql.github.com/docs/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [npm Audit Documentation](https://docs.npmjs.com/cli/v10/commands/npm-audit)
- [PNPM Audit Documentation](https://pnpm.io/cli/audit)

## 連絡先

セキュリティに関する問題を発見した場合は、公開せずに以下の方法で報告してください:

1. GitHub Security Advisories（推奨）
2. プロジェクトメンテナーに直接連絡

**重要**: 脆弱性を公開 Issue として報告しないでください。
