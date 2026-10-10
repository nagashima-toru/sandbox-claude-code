# language: ja
@named-export @eslint @frontend
Feature: ESLint ルールで default export を自動検出する

  import/no-default-export ルールを eslint.config.mjs に追加し、
  src/components / src/hooks / src/contexts 配下での default export を
  ESLint エラーとして自動検出する。
  再発防止の仕組みとして機能させる。

  Background:
    Given "eslint.config.mjs" に import/no-default-export ルールが設定されている
    And 適用範囲は以下のディレクトリである:
      | ディレクトリ                        |
      | src/components/**/*.{ts,tsx}       |
      | src/hooks/**/*.{ts,tsx}            |
      | src/contexts/**/*.{ts,tsx}         |

  @positive
  Scenario: コンポーネントファイルで default export を使用するとエラーになる
    Given "src/components/messages/MessageTable.tsx" で default export を使用している
    When "pnpm lint" を実行する
    Then ESLint エラー "import/no-default-export" が報告される

  @positive
  Scenario: named export を使用したコンポーネントは ESLint エラーにならない
    Given "src/components/messages/MessageTable.tsx" で named export を使用している
    When "pnpm lint" を実行する
    Then "src/components/messages/MessageTable.tsx" に ESLint エラーがない

  @positive
  Scenario: Next.js App Router の pages ファイルは ESLint エラーにならない
    Given "src/app/page.tsx" が default export を使用している
    When "pnpm lint" を実行する
    Then "src/app/page.tsx" に "import/no-default-export" エラーがない

  @positive
  Scenario: Next.js layout ファイルは ESLint エラーにならない
    Given "src/app/layout.tsx" が default export を使用している
    When "pnpm lint" を実行する
    Then "src/app/layout.tsx" に "import/no-default-export" エラーがない

  @positive
  Scenario: Storybook stories ファイルは ESLint エラーにならない
    Given "src/components/messages/MessageTable.stories.tsx" が "export default meta" を使用している
    When "pnpm lint" を実行する
    Then "src/components/messages/MessageTable.stories.tsx" に "import/no-default-export" エラーがない

  @positive
  Scenario: i18n 設定ファイルは ESLint エラーにならない
    Given "src/i18n/request.ts" が default export を使用している
    When "pnpm lint" を実行する
    Then "src/i18n/request.ts" に "import/no-default-export" エラーがない

  @positive
  Scenario: providers.tsx（src/app 配下）は ESLint エラーにならない
    Given "src/app/providers.tsx" が named export を使用している
    When "pnpm lint" を実行する
    Then "src/app/providers.tsx" に "import/no-default-export" エラーがない

  @positive
  Scenario: リントが全体としてパスする
    Given 全コンポーネントが named export に修正されている
    When "pnpm lint" を実行する
    Then エラーなく完了する
