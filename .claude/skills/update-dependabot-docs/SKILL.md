---
name: update-dependabot-docs
description: Update documentation version information when Dependabot updates dependencies. Run when @claude is mentioned in a Dependabot PR.
---

# Dependabot ドキュメント更新スキル

Dependabot PR で `@claude` メンションされた場合、またはドキュメントのバージョン情報更新を依頼された場合に実行します。

## 手順

### 1. PR の変更内容を確認

```bash
gh pr view [PR番号] --json title,body,files
```

変更された依存関係を特定し、以下の判定基準で影響ファイルを決定します。

### 2. バージョン情報の真実の情報源

| 技術スタック | 真実の情報源 |
|-------------|-------------|
| Spring Boot | `backend/pom.xml` の `<parent><version>` |
| Java | `backend/pom.xml` の `<java.version>` |
| Next.js, React, TypeScript | `frontend/package.json` の `dependencies` / `devDependencies` |
| Node.js (Docker) | `frontend/Dockerfile` の `FROM node:XX-alpine` |
| PostgreSQL | `docker-compose.yml` の `image: postgres:XX` |

### 3. 影響するファイルと対象依存関係

**Backend（必須）**:

| 依存関係 | 影響するファイル |
|---------|-----------------|
| **Spring Boot** のバージョン変更（メジャー/マイナー） | `CLAUDE.md`, `backend/README.md`, `backend/CLAUDE.md` |
| **Java** バージョン変更 | `CLAUDE.md`, `backend/README.md`, `backend/CLAUDE.md` |
| **Docker ベースイメージ**（eclipse-temurin）のバージョン変更 | `backend/Dockerfile`, `backend/README.md` |
| **PostgreSQL** バージョン変更 | `backend/README.md`, `backend/CLAUDE.md`, `docker-compose.yml` |

**Frontend（必須）**:

| 依存関係 | 影響するファイル |
|---------|-----------------|
| **Next.js** のバージョン変更（メジャー） | `README.md`, `frontend/CLAUDE.md` |
| **React** のバージョン変更（メジャー） | `README.md`, `frontend/CLAUDE.md` |
| **Node.js** バージョン変更（Docker イメージ） | `README.md`, `frontend/Dockerfile` |
| **TypeScript** のバージョン変更（メジャー） | `frontend/CLAUDE.md` |

**その他（推奨）**: 主要 UI ライブラリ・ビルドツールのメジャーバージョン変更

### 4. 整合性チェック

真実の情報源を読み込み、ドキュメントのバージョン番号と照合します。

```bash
# Spring Boot のバージョンを確認
grep -A3 "<parent>" backend/pom.xml | grep "<version>"
grep "<java.version>" backend/pom.xml

# Next.js のバージョンを確認
grep '"next"' frontend/package.json

# Node.js Docker イメージを確認
grep "FROM node:" frontend/Dockerfile

# PostgreSQL を確認
grep "image: postgres:" docker-compose.yml
```

### 5. ドキュメントを更新

影響するファイルの古いバージョン番号を新しいバージョン番号に置換します。

### 6. 変更をコミット

```bash
git add [影響ファイルのパス]
git commit -m "docs: update version references for [依存関係名] [旧バージョン] → [新バージョン]"
git push
```

## バージョン情報記載箇所

### ルートレベル

| ファイル | 記載されているバージョン |
|---------|----------------------|
| `CLAUDE.md` | Spring Boot, Java |
| `README.md` | Next.js, Node.js (概要のみ) |

### Backend

| ファイル | 記載されているバージョン |
|---------|----------------------|
| `backend/README.md` | Spring Boot, Java, PostgreSQL |
| `backend/CLAUDE.md` | Spring Boot, Java, PostgreSQL |
| `backend/pom.xml` | Spring Boot (parent), Java (properties) |
| `backend/Dockerfile` | eclipse-temurin ベースイメージ |

### Frontend

| ファイル | 記載されているバージョン |
|---------|----------------------|
| `README.md` | Next.js, TypeScript, Node.js |
| `frontend/CLAUDE.md` | Next.js, TypeScript |
| `frontend/package.json` | 全 npm パッケージバージョン |
| `frontend/Dockerfile` | node ベースイメージ |

### Infrastructure

| ファイル | 記載されているバージョン |
|---------|----------------------|
| `docker-compose.yml` | PostgreSQL, Node.js |

## トラブルシューティング

**バージョン番号が一致しない場合**: 真実の情報源（pom.xml / package.json）を確認し、ドキュメントを手動修正。

**Claude が応答しない場合**: GitHub Actions の「Claude Code」ワークフロー（`.github/workflows/claude.yml`）のログを確認。
