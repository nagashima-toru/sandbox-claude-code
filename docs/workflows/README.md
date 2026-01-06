# GitHub Actions ワークフローファイル

このディレクトリには、GitHub Actions のワークフロー定義ファイルが含まれています。

## 使用方法

これらのワークフローファイルを有効にするには、以下の手順を実行してください：

1. このディレクトリ内の `.yml` ファイルを `.github/workflows/` ディレクトリにコピーまたは移動します
2. 変更をコミットしてプッシュします

```bash
# 例: frontend-ci.yml を有効化
cp docs/workflows/frontend-ci.yml .github/workflows/
git add .github/workflows/frontend-ci.yml
git commit -m "ci: フロントエンドCI/CDワークフローを追加"
git push
```

## なぜこのディレクトリにあるのか？

GitHub App には `.github/workflows/` ディレクトリへの直接的な書き込み権限がないため、
自動的に作成されたワークフローファイルは一時的にこのディレクトリに配置されます。

手動で `.github/workflows/` に移動することで、ワークフローを有効化できます。
