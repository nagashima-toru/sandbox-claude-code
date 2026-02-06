# .gitignore 管理ルール

このドキュメントは、プロジェクト内の `.gitignore` ファイルの管理方針を定めます。

## 基本方針

このプロジェクトでは **3層構造** で `.gitignore` を管理します。

```
sandbox-claude-code/
├── .gitignore              # レベル1: プロジェクト全体の共通ルール
├── backend/
│   └── .gitignore          # レベル2: バックエンド固有ルール
└── frontend/
    └── .gitignore          # レベル3: フロントエンド固有ルール
```

## レベル1: ルート `.gitignore`

### 対象

プロジェクト全体に共通する、技術スタックに依存しないファイル

### 管理すべきファイル

- ✅ IDE設定ファイル（IntelliJ IDEA, Eclipse, VS Code）
- ✅ OS固有ファイル（.DS_Store, Thumbs.db など）
- ✅ エディタ一時ファイル（*.swp,*.swo, *~ など）
- ✅ Git関連（*.orig など）
- ✅ プロジェクト全体の環境変数（.env）
- ✅ プロジェクト全体のログ（*.log, logs/）
- ✅ バージョン管理ツール（.java-version など）

### 追加基準

以下の質問に **すべて YES** の場合、ルート `.gitignore` に追加：

1. バックエンドとフロントエンドの両方で発生するファイルか？
2. 開発環境（IDE、OS、エディタ）に起因するファイルか？
3. 技術スタック（Java、Node.js など）に依存しないファイルか？

### 例

```gitignore
# ✅ 正しい例
.idea/                    # IntelliJ（全体で使用）
.DS_Store                 # macOS（全体で発生）
*.log                     # ログ（全体で発生）

# ❌ 間違った例
node_modules/             # Node.js固有 → frontend/.gitignore
target/                   # Maven固有 → backend/.gitignore
.next/                    # Next.js固有 → frontend/.gitignore
```

## レベル2: `backend/.gitignore`

### 対象

バックエンド（Java/Maven/Spring Boot）固有のファイル

### 管理すべきファイル

- ✅ コンパイル済みファイル（*.class）
- ✅ パッケージファイル（*.jar,*.war, *.ear）
- ✅ Mavenビルド成果物（target/）
- ✅ Gradleビルド成果物（build/, .gradle/）
- ✅ Spring Boot設定ファイル（application-local.yml など）
- ✅ Java固有の一時ファイル（*.ctxt など）
- ✅ JVM クラッシュログ（hs_err_pid*.log）

### 追加基準

以下の質問に **YES** の場合、`backend/.gitignore` に追加：

1. backend/ ディレクトリ配下でのみ発生するファイルか？
2. Java、Maven、Gradle、Spring Boot に関連するファイルか？
3. バックエンドのビルドや実行によって生成されるファイルか？

### 例

```gitignore
# ✅ 正しい例
target/                   # Mavenビルド成果物
*.class                   # Javaコンパイル済みファイル
application-local.yml     # ローカル環境設定

# ❌ 間違った例
.idea/                    # IDE設定 → ルート.gitignore
.DS_Store                 # OS固有 → ルート.gitignore
```

## レベル3: `frontend/.gitignore`

### 対象

フロントエンド（Node.js/Next.js/pnpm）固有のファイル

### 管理すべきファイル

- ✅ Node.js依存関係（node_modules/, .pnpm-store/）
- ✅ Next.jsビルド成果物（.next/, out/）
- ✅ TypeScriptビルド（*.tsbuildinfo）
- ✅ テストレポート（coverage/, playwright-report/）
- ✅ パッケージマネージャーログ（npm-debug.log*, pnpm-debug.log*）
- ✅ **Orval生成ファイル**（src/lib/api/generated/）
- ✅ Vercel設定（.vercel）

### 追加基準

以下の質問に **YES** の場合、`frontend/.gitignore` に追加：

1. frontend/ ディレクトリ配下でのみ発生するファイルか？
2. Node.js、pnpm、Next.js、TypeScript に関連するファイルか？
3. フロントエンドのビルドや実行によって生成されるファイルか？

### 例

```gitignore
# ✅ 正しい例
node_modules/             # Node.js依存関係
.next/                    # Next.jsビルド
src/lib/api/generated/    # Orval生成コード

# ❌ 間違った例
.idea/                    # IDE設定 → ルート.gitignore
*.log                     # ログ全般 → ルート.gitignore
```

## 特殊ケース

### ケース1: Orval生成ファイル

**場所**: `frontend/.gitignore`

**理由**:

- フロントエンド固有のツール
- frontend/ ディレクトリ配下のみに影響
- `pnpm generate:api` で再生成可能

```gitignore
# Orval generated files
# DO NOT manually edit files in this directory
src/lib/api/generated/
```

### ケース2: OpenAPI仕様ファイル

**場所**: Git管理対象（除外しない）

**理由**:

- `backend/src/main/resources/api/openapi.yaml` はフロントエンドで参照
- バージョン管理が必要
- ビルド時に自動生成されるが、内容の変更履歴を追跡する必要がある

### ケース3: 環境変数ファイル

**ルート `.gitignore`**:

```gitignore
.env
.env.local
.env.*.local
```

**各プロジェクトでの対応**:

- `.env.example` や `.env.local.example` は Git 管理対象
- 実際の `.env.local` は Git 管理対象外

## 新しいルールを追加する手順

### 1. 判定フローチャート

```
新しい除外ファイルを発見
    ↓
質問1: どのディレクトリで発生？
    ├─ ルート → ルート.gitignore へ
    ├─ backend/ のみ → backend/.gitignore へ
    └─ frontend/ のみ → frontend/.gitignore へ
    ↓
質問2: 技術スタック固有？
    ├─ はい → 該当プロジェクトの.gitignore へ
    └─ いいえ（IDE/OS） → ルート.gitignore へ
    ↓
質問3: 複数プロジェクトで発生？
    ├─ はい → ルート.gitignore へ
    └─ いいえ → 該当プロジェクトの.gitignore へ
```

### 2. 追加時のベストプラクティス

```gitignore
# ❌ 悪い例: コメントなし
target/

# ✅ 良い例: セクションとコメント付き
# Maven
target/                   # Mavenビルド成果物
!.mvn/wrapper/maven-wrapper.jar  # ラッパーは含める
```

### 3. セクション分け

各 `.gitignore` は関連するルールをセクションごとにグループ化：

```gitignore
# Dependencies
node_modules/
.pnpm-store/

# Testing
coverage/
test-results/

# Next.js
.next/
out/
```

## よくある間違い

### ❌ 間違い1: すべてをルート `.gitignore` に追加

```gitignore
# ルート .gitignore に以下を追加するのは ❌
node_modules/             # → frontend/.gitignore
target/                   # → backend/.gitignore
.next/                    # → frontend/.gitignore
```

**理由**: 責任範囲が不明確になり、保守性が低下

### ❌ 間違い2: 絶対パスを使用

```gitignore
# ❌ 悪い例
/Users/username/project/backend/target/

# ✅ 良い例（backend/.gitignore 内）
target/
```

**理由**: 他の開発者の環境で動作しない

### ❌ 間違い3: 必要なファイルまで除外

```gitignore
# ❌ 悪い例
*.jar                     # すべてのjarを除外

# ✅ 良い例
*.jar
!.mvn/wrapper/maven-wrapper.jar  # ラッパーは例外的に含める
```

### ❌ 間違い4: コメントなしで曖昧なパターン

```gitignore
# ❌ 悪い例
*.tmp

# ✅ 良い例
# Temporary files
*.tmp                     # 一時ファイル
*.temp
```

## レビューチェックリスト

新しいルールを追加する際は、以下を確認：

- [ ] 適切な `.gitignore` ファイルに追加されているか？
- [ ] セクションコメントが付いているか？
- [ ] パターンが明確か（必要に応じてコメント追加）
- [ ] 他の開発者の環境でも動作するか？（絶対パス使用していないか）
- [ ] 必要なファイルまで除外していないか？
- [ ] 既存のルールと重複していないか？

## メンテナンス

### 定期的な見直し

- 四半期ごとに `.gitignore` の内容を見直す
- 不要になったルールを削除
- 新しいツールの導入時はルールを追加

### ドキュメント更新

- 新しいツールやフレームワークを導入した場合、このドキュメントを更新
- 特殊ケースが発生した場合、このドキュメントに追記

## 参考リンク

- [GitHub - gitignore templates](https://github.com/github/gitignore)
- [Git Documentation - gitignore](https://git-scm.com/docs/gitignore)

---

**最終更新**: 2026-01-06
**管理者**: Development Team
