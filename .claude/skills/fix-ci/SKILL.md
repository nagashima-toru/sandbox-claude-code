---
name: fix-ci
description: "Diagnose and fix failing CI checks on a PR: fetch logs, reproduce with CI-equivalent tool versions, fix, verify, push. Includes a checklist of known CI pitfalls."
---

# CI 修正スキル

## 概要

PR の CI 失敗を、ログ取得 → 再現 → 修正 → 確認 → push の定型フローで直す。既知の罠を先にチェックし、CI 上での 1 件ずつの発覚を避ける。

## 呼び出し方法

このスキルは **Agent-Callable** です。Task ツール（サブエージェント）経由で呼び出す。
直接 `/fix-ci` として実行することもできる。

---

## 実行フロー

### Step 1: 失敗ログの取得

1. `mcp__github__pull_request_read`（`get_check_runs`）で失敗したジョブを特定する
2. `mcp__github__get_job_logs` で失敗ジョブのログを取得する
3. **古いコミットの結果でないか確認する**: チェックの head SHA が PR の現在の head と一致しない場合は無視する

### Step 2: 既知の罠チェック

ログを読む前後で、下記チェックリストに該当しないか確認する。

### Step 3: CI と同じ条件で再現

- 該当ワークフロー（`.github/workflows/*.yml`）のツールバージョンを確認する
- pnpm は `frontend/package.json` の `packageManager`（pnpm 9）、Node はワークフローの指定に合わせる
- `./scripts/ci-check-local.sh` または失敗ジョブと同じコマンドで失敗を再現する
- コンテナジョブは手元で再現できないことがある。その場合は下記チェックリストで原因を絞る

### Step 4: 修正・確認・push

1. 最小限の修正を行う
2. Step 3 と同じ手順で解消を確認する
3. `./scripts/ci-check-local.sh` を実行する
4. push し、CI の再実行結果を確認する。新たな失敗は Step 1 からやり直す

---

## 既知の罠チェックリスト

- [ ] **lockfile**: `pnpm-lock.yaml` は pnpm 9 で生成されたか（`patchedDependencies` の hash 形式。別メジャーで `pnpm install` すると壊れる。#226）
- [ ] **Dockerfile**: `patches/` 等の追加ファイルを `frontend/Dockerfile` が COPY しているか
- [ ] **コンテナジョブ**: `git config --global --add safe.directory "$GITHUB_WORKSPACE"` と `env: HOME: /root` があるか（例: `frontend-vrt.yml`）
- [ ] **PR 本文の編集**: `pull_request` ワークフローは再実行されない。`labeled` 等の新しいイベントが必要

---

## 禁止事項

- テストの skip・無効化・隔離でグリーンにしない
- 空コミットや PR の close/reopen で CI を再実行しない
