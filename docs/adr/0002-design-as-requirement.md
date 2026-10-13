# ADR-0002: 画面デザインを要件として Spec PR で確定する

## ステータス

**採用** - 2026-10-13

## コンテキスト

SDD（ADR-0001）では API（OpenAPI）と振る舞い（Gherkin）を Spec PR で確定するが、画面デザインは要件化されておらず実装時に決まっていた。

### 課題

1. **デザインのレビュー時期が遅い**: 見た目を確認できるのは実装 PR 以降で、手戻りが大きい
2. **承認済みデザインと実装の乖離を検知できない**: 目視レビュー頼み
3. **AI 駆動の前提がない**: Claude が設計・作成・検証まで扱える形式・ツールが決まっていない

## 決定

**画面デザインを「テキスト仕様 + Storybook 画面モック」として Spec PR で承認し、VRT（Visual Regression Test）で実装との一致を担保する。正本はコード（shadcn/ui + Tailwind トークン + Storybook）とする。**

### 成果物

| 種類 | 場所 | 役割 |
| --- | --- | --- |
| テキスト仕様 | `specs/ui/<feature>/screens.md` | レイアウト・状態・使用部品・トークン・a11y・data-testid |
| 画面モック | `frontend/src/**/*.stories.tsx`（tag: `screen`） | 視覚要件。`screens.md` の 1 状態 = 1 Story。データは MSW |
| ベースライン | `frontend/tests/vrt/__screenshots__/` | 承認済みデザイン。CI で差分検知 |

### デザインの決め方

人は「方向性の選択」と「承認」のみ行い、作成・検証は Claude が担う。

1. **入力（人）**: Epic Issue に目的・利用者・主要タスク・参考画面
2. **たたき台（Claude）**: `.feature` から画面と状態を洗い出し、`screens.md` にワイヤーを書く
3. **案の比較**: 新しい画面・新しいレイアウトのときのみ 2〜3 案の Story を作り、人が 1 案を選ぶ。既存画面の変更は 1 案
4. **磨き込み（Claude）**: 全状態の Story を作り、Playwright MCP でスクショを確認して自己修正
5. **確定（人）**: Spec PR 承認。ベースラインが正解画像になる

**判断基準**: 既存部品（`components/ui`）とトークン（`globals.css`）だけで構成されているか／`.feature` の全状態がそろっているか

### ルール

- デザイン変更は Spec PR 経由のみ。実装 PR でベースラインを更新しない
- 実装完了条件は VRT 差分 0
- ベースラインは CI（Playwright Docker イメージ）でのみ生成する（OS・フォント差異を排除）

### AI 駆動ツール

| 用途 | ツール |
| --- | --- |
| 部品の検索・追加 | shadcn MCP |
| モックデータ | MSW（Orval が OpenAPI から生成する `*.msw.ts`） |
| 視覚確認・自己修正 | Playwright MCP |
| 一致担保 | Playwright `toHaveScreenshot`（`pnpm test:vrt`、`.github/workflows/frontend-vrt.yml`） |

## 検討した代替案

| 案 | 不採用理由 |
| --- | --- |
| Figma を正本（Figma MCP でコード化） | 正本が 2 つになり乖離する。Git 差分でレビューできない。デザイナー参加時のみ補助として使う |
| テキスト仕様のみ | 見た目は実装時まで確認できず、課題 1 が解決しない |
| 静的 HTML モック | 本番コードと別物で乖離する。VRT のベースラインにできない |
| Chromatic（SaaS VRT） | 外部サービス依存とコスト。Playwright で同等のことができる |

## 結果

### 期待される効果

- デザインの手戻りが Spec PR 段階で止まる
- 承認済みデザインからの逸脱を CI で機械的に検知
- Claude が要件作成〜実装〜検証を一貫して実行できる

### トレードオフ

- Spec PR に画面コンポーネント（表示層）と Story が含まれ、Spec PR が重くなる
- ベースライン更新は CI 経由（`workflow_dispatch`）でしか行えない
- フォントやブラウザ更新時はベースラインの一括更新が必要

## 関連

- [ADR-0001: OpenAPI-First 開発の採用](0001-use-openapi-first.md)
- [SDD 詳細ガイド](../development/SDD.md)
