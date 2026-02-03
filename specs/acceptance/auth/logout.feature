# language: ja
@auth @logout @api
Feature: ログアウト機能

  リフレッシュトークンを無効化してログアウトする

  Background:
    Given 以下のユーザーが存在する:
      | username | password | role  | enabled |
      | admin    | admin123 | ADMIN | true    |

  @positive
  Scenario: 正常にログアウトする
    Given ユーザー "admin" でログインする
    When 取得したアクセストークンで POST /api/auth/logout を呼び出す
    Then ステータスコード 204 が返される

  @positive
  Scenario: ログアウト後、リフレッシュトークンが無効化されている
    Given ユーザー "admin" でログインする
    And ログインレスポンスからリフレッシュトークンを保存する
    When 取得したアクセストークンで POST /api/auth/logout を呼び出す
    Then ステータスコード 204 が返される
    When 保存したリフレッシュトークンで POST /api/auth/refresh を試みる
    Then ステータスコード 401 が返される

  @positive
  Scenario: ログアウト後、アクセストークンは有効期限まで使用可能
    Given ユーザー "admin" でログインする
    And ログインレスポンスからアクセストークンを保存する
    When 保存したアクセストークンで POST /api/auth/logout を呼び出す
    Then ステータスコード 204 が返される
    When 保存したアクセストークンで GET /api/messages を呼び出す
    Then ステータスコード 200 が返される

  @negative
  Scenario: 認証なしでログアウトを試みる
    Given 認証トークンなしでリクエストする
    When POST /api/auth/logout を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である

  @negative
  Scenario: 無効なトークンでログアウトを試みる
    Given 無効なトークンでリクエストする
    When POST /api/auth/logout を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である

  @negative
  Scenario: 期限切れトークンでログアウトを試みる
    Given 期限切れのアクセストークンでリクエストする
    When POST /api/auth/logout を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である
