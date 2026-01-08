# Frontend アプリケーション実装 TODO

メッセージ管理画面の実装計画と進捗管理

**作成日**: 2026-01-06
**目標**: Next.js + Orval を使用したメッセージ管理 CRUD アプリケーション

---

## 📋 目次

- [Phase 1: プロジェクト初期セットアップ](#phase-1-プロジェクト初期セットアップ)
- [Phase 2: API 連携基盤](#phase-2-api-連携基盤)
- [Phase 3: UI 基盤構築](#phase-3-ui-基盤構築)
- [Phase 4: コア機能実装](#phase-4-コア機能実装)
- [Phase 5: 高度な機能](#phase-5-高度な機能)
- [Phase 6: テスト](#phase-6-テスト)
- [Phase 7: 最終調整](#phase-7-最終調整)

---

## Phase 1: プロジェクト初期セットアップ ✅

### 環境構築

- [x] **1.1** Node.js 20+ のインストール確認
  - `node --version` で確認
  - 必要に応じて nvm/volta でインストール

- [x] **1.2** pnpm のインストール
  - `npm install -g pnpm`
  - `pnpm --version` で確認

- [x] **1.3** 依存関係のインストール
  - `cd frontend && pnpm install`
  - エラーがないことを確認

- [x] **1.4** 環境変数ファイルの作成
  - `.env.local.example` を `.env.local` にコピー
  - `NEXT_PUBLIC_API_URL=http://localhost:8080/api` を設定

### バックエンド連携確認

- [x] **1.5** バックエンド API の起動確認
  - `cd backend && ./mvnw spring-boot:run`
  - http://localhost:8080/api-docs.yaml にアクセスできることを確認

- [x] **1.6** OpenAPI 仕様の生成
  - `cd backend && ./mvnw verify`
  - `backend/src/main/resources/api/openapi.yaml` が存在することを確認

---

## Phase 2: API 連携基盤 ✅

### Orval セットアップ

- [x] **2.1** Orval 設定の確認
  - `orval.config.ts` の内容確認
  - OpenAPI ファイルのパスが正しいことを確認

- [x] **2.2** API クライアントの生成
  - `pnpm generate:api`
  - `src/lib/api/generated/` にファイルが生成されることを確認
  - 生成されたファイルの内容確認（型定義、hooks など）

- [x] **2.3** axios インスタンスの動作確認
  - `src/lib/api/client.ts` のインターセプターが動作することを確認
  - 開発者ツールでリクエストログが表示されることを確認

### TanStack Query セットアップ

- [x] **2.4** Query Client Provider の作成
  - `src/lib/query-client.ts` を作成
  - デフォルトオプション設定（staleTime, cacheTime など）

- [x] **2.5** Root Layout への Provider 追加
  - `src/app/layout.tsx` に QueryClientProvider を追加
  - React Query Devtools を開発環境のみ有効化

---

## Phase 3: UI 基盤構築 ✅

### Next.js 基本設定

- [x] **3.1** グローバルスタイルの設定
  - `src/app/globals.css` を作成
  - Tailwind CSS の基本設定追加
  - CSS 変数の定義（shadcn/ui 用）

- [x] **3.2** Root Layout の実装
  - `src/app/layout.tsx` の基本構造作成
  - メタデータ設定（title, description）
  - フォント設定

### shadcn/ui セットアップ

- [x] **3.3** shadcn/ui の初期化
  - `npx shadcn@latest init`
  - スタイル、ベースカラー、CSS 変数の設定

- [x] **3.4** 必要なコンポーネントの追加
  - `npx shadcn@latest add button`
  - `npx shadcn@latest add table`
  - `npx shadcn@latest add dialog`
  - `npx shadcn@latest add form`
  - `npx shadcn@latest add input`
  - `npx shadcn@latest add label`
  - `npx shadcn@latest add card`
  - `npx shadcn@latest add badge`

- [x] **3.5** ユーティリティ関数の作成
  - `src/lib/utils.ts` に `cn()` 関数（既存の場合は確認のみ）

### 共通コンポーネント

- [x] **3.6** Loading コンポーネント
  - `src/components/common/Loading.tsx` を作成
  - スピナーアニメーション

- [x] **3.7** ErrorMessage コンポーネント
  - `src/components/common/ErrorMessage.tsx` を作成
  - エラー表示用

- [x] **3.8** PageHeader コンポーネント
  - `src/components/common/PageHeader.tsx` を作成
  - ページタイトルと説明を表示

---

## Phase 4: コア機能実装

### バリデーションスキーマ

- [ ] **4.1** Zod スキーマの作成
  - `src/lib/validations/message.ts` を作成
  - `messageSchema` の定義（code, content）
  - バリデーションルール設定（必須、最大文字数）

### メッセージ一覧画面（基本）

- [ ] **4.2** ページコンポーネントの作成
  - `src/app/page.tsx` を作成
  - ページレイアウトの基本構造

- [ ] **4.3** MessageTable コンポーネント（基本版）
  - `src/components/messages/MessageTable.tsx` を作成
  - `useGetAllMessages` フックを使用してデータ取得
  - ローディング状態の表示
  - エラー状態の表示
  - テーブルのヘッダー（ID, Code, Content, Actions）
  - テーブルの行表示

- [ ] **4.4** 動作確認
  - バックエンドからデータが取得できることを確認
  - データがテーブルに表示されることを確認

### メッセージ作成機能

- [ ] **4.5** MessageModal コンポーネント（作成）
  - `src/components/messages/MessageModal.tsx` を作成
  - Dialog コンポーネントを使用
  - 開閉状態の管理

- [ ] **4.6** MessageForm コンポーネント（作成）
  - `src/components/messages/MessageForm.tsx` を作成
  - React Hook Form のセットアップ
  - Zod リゾルバーの統合
  - Code 入力フィールド
  - Content 入力フィールド（textarea）
  - バリデーションエラー表示

- [ ] **4.7** 作成処理の実装
  - `useCreateMessage` フックの使用
  - フォーム送信時の処理
  - 成功時にモーダルを閉じる
  - 成功時に一覧を再取得（invalidateQueries）

- [ ] **4.8** 作成ボタンの追加
  - ページヘッダーに「新規作成」ボタンを追加
  - クリック時にモーダルを開く

- [ ] **4.9** 動作確認
  - 新規作成ボタンでモーダルが開くことを確認
  - フォーム入力とバリデーションの動作確認
  - データが作成されることを確認
  - 一覧が自動で更新されることを確認

### メッセージ編集機能

- [ ] **4.10** MessageForm の編集モード対応
  - デフォルト値の設定（編集時）
  - 作成/編集モードの判定

- [ ] **4.11** 編集処理の実装
  - `useUpdateMessage` フックの使用
  - 編集ボタンのクリックで対象データをモーダルに渡す
  - 更新成功時の処理

- [ ] **4.12** 編集ボタンの追加
  - テーブルの Actions カラムに編集ボタン追加
  - クリック時にモーダルを開き、データを渡す

- [ ] **4.13** 動作確認
  - 編集ボタンでモーダルが開き、データが入っていることを確認
  - データが更新されることを確認
  - 一覧が自動で更新されることを確認

### メッセージ削除機能

- [ ] **4.14** DeleteConfirmDialog コンポーネント
  - `src/components/messages/DeleteConfirmDialog.tsx` を作成
  - Dialog コンポーネントを使用
  - 削除対象の code と content を表示
  - 確認メッセージ

- [ ] **4.15** 削除処理の実装
  - `useDeleteMessage` フックの使用
  - 削除確認ダイアログの表示
  - 削除成功時に一覧を再取得

- [ ] **4.16** 削除ボタンの追加
  - テーブルの Actions カラムに削除ボタン追加
  - クリック時に確認ダイアログを開く

- [ ] **4.17** 動作確認
  - 削除ボタンで確認ダイアログが開くことを確認
  - データが削除されることを確認
  - 一覧が自動で更新されることを確認

---

## Phase 5: 高度な機能

### 検索・フィルタリング

- [ ] **5.1** SearchBar コンポーネント
  - `src/components/messages/SearchBar.tsx` を作成
  - 検索入力フィールド
  - リアルタイム検索（debounce 付き）

- [ ] **5.2** useDebounce フックの作成
  - `src/hooks/useDebounce.ts` を作成
  - 300ms でデバウンス

- [ ] **5.3** クライアントサイドフィルタリング
  - MessageTable コンポーネントに検索ロジック追加
  - code と content での検索

- [ ] **5.4** 動作確認
  - 検索入力でリアルタイムにフィルタリングされることを確認
  - デバウンスが効いていることを確認

### ソート機能

- [ ] **5.5** ソート状態の管理
  - useState でソートカラムと方向を管理
  - ソート関数の実装

- [ ] **5.6** ソート可能なテーブルヘッダー
  - ヘッダークリックでソート
  - ソート方向インジケーター（矢印アイコン）

- [ ] **5.7** 動作確認
  - ID, Code, Content でソートできることを確認
  - 昇順/降順の切り替えができることを確認

### ページネーション

- [ ] **5.8** ページネーション状態の管理
  - 現在ページ、1ページあたりの件数
  - ページ計算ロジック

- [ ] **5.9** Pagination コンポーネント
  - `src/components/messages/Pagination.tsx` を作成
  - ページ番号ボタン
  - 前へ/次へボタン
  - 1ページあたりの件数選択（10, 25, 50, 100）

- [ ] **5.10** テーブルとの統合
  - ページネーションされたデータの表示
  - ページ変更時の動作

- [ ] **5.11** 動作確認
  - ページ切り替えが正しく動作することを確認
  - 件数変更が正しく動作することを確認

### レスポンシブ対応

- [ ] **5.12** モバイルレイアウトの実装
  - 768px 未満でカード表示に切り替え
  - MessageCard コンポーネントの作成（オプション）

- [ ] **5.13** モーダルのモバイル対応
  - 小画面でフルスクリーン表示

- [ ] **5.14** 動作確認
  - ブラウザのレスポンシブモードで確認
  - スマートフォン実機で確認（可能であれば）

### エラーハンドリング

- [ ] **5.15** サーバーエラーの表示
  - 400, 404, 409, 500 エラーの適切なメッセージ表示
  - フォーム内エラー表示

- [ ] **5.16** ネットワークエラーの表示
  - タイムアウト処理
  - リトライ機能（TanStack Query のデフォルト設定）

- [ ] **5.17** 動作確認
  - バックエンドを停止してエラー表示を確認
  - 重複コードでの作成を試してエラー表示を確認

---

## Phase 6: テスト

### ユニットテスト

- [ ] **6.1** バリデーションスキーマのテスト
  - `tests/unit/validations/message.test.ts` を作成
  - 正常系・異常系のテスト

- [ ] **6.2** useDebounce フックのテスト
  - `tests/unit/hooks/useDebounce.test.ts` を作成
  - デバウンス動作の確認

### コンポーネントテスト

- [ ] **6.3** MessageForm のテスト
  - `tests/component/messages/MessageForm.test.tsx` を作成
  - レンダリング
  - フォーム入力
  - バリデーション
  - 送信処理

- [ ] **6.4** MessageTable のテスト
  - `tests/component/messages/MessageTable.test.tsx` を作成
  - データ表示
  - ローディング状態
  - エラー状態

- [ ] **6.5** DeleteConfirmDialog のテスト
  - `tests/component/messages/DeleteConfirmDialog.test.tsx` を作成
  - 表示内容
  - キャンセル/削除ボタン

### E2E テスト

- [ ] **6.6** CRUD フローのテスト
  - `tests/e2e/messages.spec.ts` を作成
  - 一覧表示
  - 新規作成
  - 編集
  - 削除

- [ ] **6.7** 検索・フィルタリングのテスト
  - 検索機能の動作確認

- [ ] **6.8** レスポンシブのテスト
  - モバイルビューポートでの動作確認

- [ ] **6.9** エラーハンドリングのテスト
  - バリデーションエラー
  - サーバーエラー

---

## Phase 7: 最終調整

### コード品質

- [ ] **7.1** ESLint の実行と修正
  - `pnpm lint`
  - 警告・エラーの修正

- [ ] **7.2** 型チェック
  - `pnpm type-check`
  - 型エラーの修正

- [ ] **7.3** Prettier でフォーマット
  - `pnpm prettier --write "src/**/*.{ts,tsx}"`

### パフォーマンス最適化

- [ ] **7.4** 不要な再レンダリングの最適化
  - useMemo, useCallback の適用（必要に応じて）

- [ ] **7.5** バンドルサイズの確認
  - `pnpm build`
  - ビルドサイズの確認

### ドキュメント

- [ ] **7.6** コンポーネントに JSDoc コメント追加
  - 複雑なロジックにコメント

- [ ] **7.7** README の更新
  - セットアップ手順の確認
  - スクリーンショット追加（オプション）

### 最終確認

- [ ] **7.8** すべての機能の動作確認
  - CRUD 操作
  - 検索・フィルタリング
  - ソート
  - ページネーション
  - レスポンシブ

- [ ] **7.9** ブラウザ互換性確認
  - Chrome
  - Firefox
  - Safari（可能であれば）

- [ ] **7.10** パフォーマンス確認
  - Lighthouse スコア
  - React DevTools Profiler

---

## 🎯 マイルストーン

| Phase    | 完了目標         | 所要時間（目安） |
| -------- | ---------------- | ---------------- |
| Phase 1  | 環境構築完了     | 30分             |
| Phase 2  | API 連携確認     | 30分             |
| Phase 3  | UI 基盤構築      | 1時間            |
| Phase 4  | CRUD 機能完成    | 3-4時間          |
| Phase 5  | 高度な機能実装   | 2-3時間          |
| Phase 6  | テスト完了       | 2-3時間          |
| Phase 7  | リリース準備完了 | 1時間            |
| **合計** | **全機能完成**   | **10-13時間**    |

---

## 📝 メモ

### 開発時の注意点

- バックエンドを常時起動しておく（`./mvnw spring-boot:run`）
- API 変更時は必ず `pnpm generate:api` を実行
- 生成ファイル（`src/lib/api/generated/`）は編集しない
- コミット前に `pnpm lint` と `pnpm type-check` を実行

### 優先順位の変更が必要な場合

Phase 4 までを最優先で実装し、Phase 5 の高度な機能は後回しにすることも可能です。

### トラブルシューティング

- Orval 生成エラー → OpenAPI ファイルのパスを確認
- 型エラー → `pnpm generate:api` を再実行
- CORS エラー → `next.config.js` のプロキシ設定を確認

---

## 📊 進捗状況

| Phase                                 | 状態          | 完了   | 全体   | 進捗率    |
| ------------------------------------- | ------------- | ------ | ------ | --------- |
| Phase 1: プロジェクト初期セットアップ | ✅ 完了       | 6      | 6      | 100%      |
| Phase 2: API 連携基盤                 | ✅ 完了       | 5      | 5      | 100%      |
| Phase 3: UI 基盤構築                  | ✅ 完了       | 8      | 8      | 100%      |
| Phase 4: コア機能実装                 | ✅ 完了       | 17     | 17     | 100%      |
| Phase 5: 高度な機能                   | 🔜 次         | 0      | 17     | 0%        |
| Phase 6: テスト                       | ⏸️ 未着手     | 0      | 9      | 0%        |
| Phase 7: 最終調整                     | ⏸️ 未着手     | 0      | 10     | 0%        |
| **合計**                              | **🚧 進行中** | **36** | **72** | **50.0%** |

**詳細な作業記録**: [PROGRESS.md](./PROGRESS.md) を参照

---

**最終更新**: 2026-01-06 18:30
**進捗**: 36 / 72 タスク完了 (50.0%)
