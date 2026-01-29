# Dependabot PR ドキュメント更新ガイド

## 概要

Dependabot は依存関係を自動的に更新しますが、ドキュメントファイル（CLAUDE.md, README.md など）に記載されているバージョン情報は更新されません。このガイドでは、Dependabot PR に対して @claude を使ってドキュメントを更新する方法を説明します。

## ドキュメント更新が必要な依存関係

以下の依存関係が更新された場合、ドキュメント更新を検討してください。

### Backend（必須）

| 依存関係 | 影響するファイル |
|---------|-----------------|
| **Spring Boot** のバージョン変更（メジャー/マイナー） | `CLAUDE.md`, `backend/README.md`, `backend/CLAUDE.md` |
| **Java** バージョン変更 | `CLAUDE.md`, `backend/README.md`, `backend/CLAUDE.md` |
| **Docker ベースイメージ**（eclipse-temurin）のバージョン変更 | `backend/Dockerfile`, `backend/README.md` |
| **PostgreSQL** バージョン変更 | `backend/README.md`, `backend/CLAUDE.md`, `docker-compose.yml` |

### Frontend（必須）

| 依存関係 | 影響するファイル |
|---------|-----------------|
| **Next.js** のバージョン変更（メジャー） | `README.md`, `frontend/CLAUDE.md` |
| **React** のバージョン変更（メジャー） | `README.md`, `frontend/CLAUDE.md` |
| **Node.js** バージョン変更（Docker イメージ） | `README.md`, `frontend/Dockerfile`, `docs/DEPLOYMENT.md` |
| **TypeScript** のバージョン変更（メジャー） | `frontend/CLAUDE.md` |

### その他（推奨）

- 主要な UI ライブラリのメジャーバージョン変更（shadcn/ui, Tailwind CSS など）
- ビルドツールのメジャーバージョン変更（Maven, pnpm など）

## @claude を使ったドキュメント更新手順

### ステップ 1: Dependabot PR を確認

1. Dependabot が作成した PR を開く
2. PR タイトルと変更内容を確認
3. 上記の「ドキュメント更新が必要な依存関係」に該当するか判断

### ステップ 2: @claude メンションでドキュメント更新を依頼

該当する場合、PR に以下のようなコメントを投稿します。

#### 基本的なプロンプトテンプレート

```markdown
@claude この PR の依存関係更新に伴い、関連するドキュメントのバージョン情報を更新してください。

対象ファイル:
- CLAUDE.md
- README.md
- backend/README.md
- backend/CLAUDE.md
- frontend/CLAUDE.md
- docs/DEPLOYMENT.md（該当する場合）

変更内容を確認し、影響を受けるすべてのバージョン番号を更新してください。
```

#### 具体例 1: Spring Boot 更新時

```markdown
@claude Spring Boot が 3.4.1 から 3.5.0 に更新されました。以下のドキュメント内の Spring Boot バージョン表記を更新してください：
- CLAUDE.md
- backend/README.md
- backend/CLAUDE.md

また、pom.xml を確認して、実際のバージョン番号と整合性を確認してください。
```

#### 具体例 2: Java バージョン更新時

```markdown
@claude Java バージョンが 21 から 23 に変更されました。以下のドキュメント内の Java バージョン表記を更新してください：
- CLAUDE.md
- backend/README.md
- backend/CLAUDE.md

pom.xml の java.version プロパティと整合性を確認してください。
```

#### 具体例 3: Next.js 更新時

```markdown
@claude Next.js が 15.1.0 から 16.0.0 に更新されました。以下のドキュメント内の Next.js バージョン表記を更新してください：
- README.md
- frontend/CLAUDE.md

package.json の実際のバージョンと整合性を確認してください。
```

#### 具体例 4: Node.js Docker イメージ更新時

```markdown
@claude Node.js の Docker ベースイメージが node:20-alpine から node:22-alpine に変更されました。以下のドキュメントを更新してください：
- README.md
- frontend/CLAUDE.md
- docs/DEPLOYMENT.md

frontend/Dockerfile の実際のバージョンと整合性を確認してください。
```

### ステップ 3: Claude の実行を確認

1. GitHub Actions の「Claude Code」ワークフロー（`.github/workflows/claude.yml`）が自動的にトリガーされます
2. Claude がコードを分析し、ドキュメントの変更を提案します
3. Claude が自動的にコミットを追加します

### ステップ 4: 変更内容を確認

1. Claude が追加したコミットを確認
2. 更新されたドキュメントのバージョン番号が正しいか確認
3. pom.xml や package.json との整合性を確認

### ステップ 5: マージ

変更内容に問題がなければ、PR をマージします。

## 手動更新が必要な場合

Claude が実行できない場合や、手動で更新したい場合は、以下の手順で更新してください。

### 手順

1. **各ドキュメントファイルを開く**
2. **該当するバージョン番号を検索**
   - 例: `3.4.1` で検索
3. **新しいバージョンに置換**
   - 例: `3.5.0` に置換
4. **pom.xml や package.json と整合性を確認**
   - Backend: `backend/pom.xml` の `<version>` タグと `<java.version>` プロパティ
   - Frontend: `frontend/package.json` の dependencies
5. **コミット・プッシュ**

### コマンド例（検索）

```bash
# Spring Boot のバージョンを検索
grep -r "Spring Boot 3.4.1" .

# Java のバージョンを検索
grep -r "Java.*21" backend/

# Next.js のバージョンを検索
grep -r "Next.js.*15" frontend/
```

### バージョン情報の真実の情報源

| 技術スタック | 真実の情報源 |
|-------------|-------------|
| Spring Boot | `backend/pom.xml` の `<parent><version>` |
| Java | `backend/pom.xml` の `<java.version>` |
| Next.js, React, TypeScript | `frontend/package.json` の `dependencies` / `devDependencies` |
| Node.js (Docker) | `frontend/Dockerfile` の `FROM node:XX-alpine` |
| PostgreSQL | `docker-compose.yml` の `image: postgres:XX` |

## バージョン情報記載箇所の一覧

### ルートレベル

| ファイル | 記載されているバージョン |
|---------|----------------------|
| `CLAUDE.md` | Spring Boot 3.5.10, Java 25 |
| `README.md` | (プロジェクト概要のみ、詳細なバージョンは各サブディレクトリ) |

### Backend

| ファイル | 記載されているバージョン |
|---------|----------------------|
| `backend/README.md` | Spring Boot 3.5.10, Java 25, PostgreSQL 16 |
| `backend/CLAUDE.md` | Spring Boot 3.5.10, Java 25, PostgreSQL 16 |
| `backend/pom.xml` | Spring Boot 3.5.10 (parent), Java 25 (properties) |
| `backend/Dockerfile` | eclipse-temurin ベースイメージバージョン |

### Frontend

| ファイル | 記載されているバージョン |
|---------|----------------------|
| `README.md` | Next.js 15+, TypeScript, Node.js 20+ |
| `frontend/CLAUDE.md` | Next.js 15+ (App Router), TypeScript |
| `frontend/package.json` | Next.js ^15.1.0, React ^19.0.0, TypeScript ^5.7.3 |
| `frontend/Dockerfile` | node:20-alpine |

### Infrastructure

| ファイル | 記載されているバージョン |
|---------|----------------------|
| `docker-compose.yml` | PostgreSQL 16, Node.js 20-alpine |
| `docs/DEPLOYMENT.md` | Node.js 20-alpine |

## 整合性チェックスクリプト

以下のスクリプトを使って、ドキュメントとソースコードのバージョン情報の整合性を確認できます。

```bash
#!/bin/bash
# version-check.sh

echo "=== Version Information Check ==="
echo ""

echo "--- Backend ---"
echo "Spring Boot (pom.xml):"
grep "<parent>" -A 3 backend/pom.xml | grep "<version>"

echo "Java (pom.xml):"
grep "<java.version>" backend/pom.xml

echo "Spring Boot (CLAUDE.md):"
grep "Spring Boot" CLAUDE.md backend/README.md backend/CLAUDE.md

echo "Java (Documentation):"
grep -E "Java.*21|Java.*23" CLAUDE.md backend/README.md backend/CLAUDE.md

echo ""
echo "--- Frontend ---"
echo "Next.js (package.json):"
grep '"next"' frontend/package.json

echo "Next.js (Documentation):"
grep -i "next.js" README.md frontend/CLAUDE.md

echo "Node.js (Dockerfile):"
grep "FROM node:" frontend/Dockerfile

echo ""
echo "--- Database ---"
echo "PostgreSQL (docker-compose.yml):"
grep "image: postgres:" docker-compose.yml

echo "PostgreSQL (Documentation):"
grep -i "postgresql" backend/README.md backend/CLAUDE.md
```

このスクリプトを実行することで、各ドキュメントとソースコードのバージョン情報を一覧表示し、整合性を目視で確認できます。

## トラブルシューティング

### Claude が応答しない場合

1. **GitHub Actions のログを確認**
   - リポジトリの「Actions」タブを開く
   - 「Claude Code」ワークフローの実行ログを確認
   - エラーメッセージがある場合は内容を確認

2. **再度メンションを試す**
   - 別のコメントで再度 @claude メンションを投稿
   - より具体的なファイル名とバージョン番号を指定

3. **手動更新に切り替え**
   - 上記の「手動更新が必要な場合」のセクションを参照

### バージョン番号が一致しない場合

1. **真実の情報源を確認**
   - Backend: `backend/pom.xml`
   - Frontend: `frontend/package.json`

2. **ドキュメントを手動で修正**
   - 該当するドキュメントファイルを開いて修正

3. **整合性チェックスクリプトを実行**
   - すべてのバージョン情報が一致していることを確認

## 参考リンク

- [Dependabot 公式ドキュメント](https://docs.github.com/ja/code-security/dependabot)
- [GitHub Actions ワークフロー](../.github/workflows/claude.yml)
- [プロジェクトの CLAUDE.md](../CLAUDE.md)
- [Backend の CLAUDE.md](../backend/CLAUDE.md)
- [Frontend の CLAUDE.md](../frontend/CLAUDE.md)

## まとめ

Dependabot PR でドキュメントのバージョン情報を更新するには、@claude メンションを使うのが最も簡単で効率的です。必要に応じて手動更新も可能です。定期的に整合性チェックスクリプトを実行することで、ドキュメントとソースコードの一貫性を保つことができます。
