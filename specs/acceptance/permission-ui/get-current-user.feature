# language: ja
@auth @api @permission-ui
Feature: ユーザー情報取得API

  ログイン中のユーザー情報（ユーザー名とロール）を取得する機能

  Background:
    Given 以下のユーザーが登録されている:
      | username | password  | role   |
      | admin    | admin123  | ADMIN  |
      | viewer   | viewer123 | VIEWER |

  @positive
  Scenario: 管理者ユーザーの情報取得
    Given ユーザー "admin" でログインしている
    When GET /api/users/me を呼び出す
    Then ステータスコード 200 が返される
    And レスポンスボディに以下が含まれる:
      | フィールド | 値    |
      | username  | admin |
      | role      | ADMIN |

  @positive
  Scenario: 閲覧者ユーザーの情報取得
    Given ユーザー "viewer" でログインしている
    When GET /api/users/me を呼び出す
    Then ステータスコード 200 が返される
    And レスポンスボディに以下が含まれる:
      | フィールド | 値     |
      | username  | viewer |
      | role      | VIEWER |

  @negative
  Scenario: 未認証ユーザーの情報取得
    Given 認証されていない状態である
    When GET /api/users/me を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスが RFC 7807 形式である
    And エラーメッセージに "認証が必要です" が含まれる

  @negative
  Scenario: 無効なトークンでの情報取得
    Given 無効なトークンを使用している
    When GET /api/users/me を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスが RFC 7807 形式である
    And エラーメッセージに "トークンが無効です" が含まれる