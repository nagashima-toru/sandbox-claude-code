---
name: test-coverage
description: Generate test coverage reports and identify files below 90% coverage threshold for frontend/backend.
---

# Test Coverage スキル

テストカバレッジレポートを自動生成し、目標未達成のファイルを一覧表示します。

## 目的

- フロントエンド/バックエンドのカバレッジレポートを生成
- 目標（90%）未達成のファイルを特定
- カバレッジ改善が必要な箇所を明示

## 使用方法

```bash
# フロントエンドのカバレッジを確認
/test-coverage frontend

# バックエンドのカバレッジを確認
/test-coverage backend

# 両方を確認
/test-coverage all
```

## 実行フロー

### 1. 引数の解析

引数から対象を判定：
- `frontend` → フロントエンドのみ
- `backend` → バックエンドのみ
- `all` または引数なし → 両方

### 2. カバレッジレポートの生成

#### フロントエンド

```bash
cd frontend && pnpm test:coverage --run
```

#### バックエンド

```bash
cd backend && ./mvnw test jacoco:report
```

### 3. 目標未達成ファイルの抽出

目標: **90% 以上**

カバレッジが 90% 未満のファイルを抽出して表示。

## 参考

- **カバレッジ比較**: `./scripts/coverage-diff.sh`
- **CLAUDE.md**: カバレッジ目標の定義