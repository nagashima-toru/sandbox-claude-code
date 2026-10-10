# language: ja
@named-export @refactoring @frontend
Feature: フロントエンドコンポーネントを named export に統一する

  BEST_PRACTICES.md §1「named export のみ」・§8「default export の使用（アンチパターン）」に従い、
  違反コンポーネントをすべて named export に変更する。
  また ESLint ルールを追加して、将来の違反を自動検出する。

  # 対象ファイル（named export に変更）
  # - src/components/messages/MessageTable.tsx
  # - src/components/messages/MessageModal.tsx
  # - src/components/messages/MessageForm.tsx
  # - src/components/messages/DeleteConfirmDialog.tsx
  # - src/app/providers.tsx
  #
  # 除外（違反ではない）
  # - src/app/layout.tsx, src/app/page.tsx, src/app/login/page.tsx（Next.js App Router 必須）
  # - *.stories.tsx（Storybook の export default meta 慣例）
  # - src/i18n/request.ts（Next.js i18n 設定必須）
  # - src/lib/api/client.ts（Orval 自動生成）

  @positive
  Scenario: MessageTable.tsx が named export になっている
    Then "src/components/messages/MessageTable.tsx" に以下のパターンが存在する:
      | パターン                             |
      | export function MessageTable         |
    And "src/components/messages/MessageTable.tsx" に以下のパターンが存在しない:
      | パターン              |
      | export default        |

  @positive
  Scenario: MessageModal.tsx が named export になっている
    Then "src/components/messages/MessageModal.tsx" に以下のパターンが存在する:
      | パターン                             |
      | export function MessageModal         |
    And "src/components/messages/MessageModal.tsx" に以下のパターンが存在しない:
      | パターン              |
      | export default        |

  @positive
  Scenario: MessageForm.tsx が named export になっている
    Then "src/components/messages/MessageForm.tsx" に以下のパターンが存在する:
      | パターン                                   |
      | export function MessageForm                |
    And "src/components/messages/MessageForm.tsx" に以下のパターンが存在しない:
      | パターン              |
      | export default        |

  @positive
  Scenario: DeleteConfirmDialog.tsx が named export になっている
    Then "src/components/messages/DeleteConfirmDialog.tsx" に以下のパターンが存在する:
      | パターン                                          |
      | export function DeleteConfirmDialog               |
    And "src/components/messages/DeleteConfirmDialog.tsx" に以下のパターンが存在しない:
      | パターン              |
      | export default        |

  @positive
  Scenario: providers.tsx が named export になっている
    Then "src/app/providers.tsx" に以下のパターンが存在する:
      | パターン                          |
      | export function Providers         |
    And "src/app/providers.tsx" に以下のパターンが存在しない:
      | パターン              |
      | export default        |

  @positive
  Scenario: 全 import 側が named import になっている
    Then "src/app/layout.tsx" が Providers を named import で参照している
    And "src/app/page.tsx" が MessageTable を named import で参照している
    And "src/app/page.tsx" が MessageModal を named import で参照している
    And "src/app/page.tsx" が DeleteConfirmDialog を named import で参照している
    And "src/components/messages/MessageModal.tsx" が MessageForm を named import で参照している

  @positive
  Scenario: Storybook stories ファイルも named import を使用している
    Then "src/components/messages/MessageTable.stories.tsx" が MessageTable を named import で参照している
    And "src/components/messages/MessageModal.stories.tsx" が MessageModal を named import で参照している
    And "src/components/messages/MessageForm.stories.tsx" が MessageForm を named import で参照している
    And "src/components/messages/DeleteConfirmDialog.stories.tsx" が DeleteConfirmDialog を named import で参照している

  @positive
  Scenario: アプリケーションが正常に動作する
    Given アプリケーションが起動している
    And 管理者ユーザーでログイン済みである
    When メイン画面 (/) を開く
    Then メッセージ一覧が正常に表示される
    And メッセージ新規作成ダイアログが正常に開ける
    And メッセージ削除確認ダイアログが正常に開ける

  @positive
  Scenario: TypeScript 型チェックが通過する
    When "pnpm type-check" を実行する
    Then エラーなく完了する

  @positive
  Scenario: テストスイートが通過する
    When "pnpm test" を実行する
    Then 全テストがパスする
