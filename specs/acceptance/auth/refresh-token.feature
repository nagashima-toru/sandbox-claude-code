# language: ja
@auth @refresh @api
Feature: トークンリフレッシュ機能

  リフレッシュトークンを使用してアクセストークンを更新する

  Background:
    Given 以下のユーザーが存在する:
      | username | password | role  | enabled |
      | admin    | admin123 | ADMIN | true    |

  @positive
  Scenario: 有効なリフレッシュトークンで新しいアクセストークンを取得する
    Given ユーザー "admin" でログインする
    And ログインレスポンスからリフレッシュトークンを取得する
    When 以下のリフレッシュ情報で POST /api/auth/refresh を呼び出す:
      | refreshToken | <取得したリフレッシュトークン> |
    Then ステータスコード 200 が返される
    And レスポンスボディに以下が含まれる:
      | フィールド | 値     |
      | tokenType | Bearer |
    And レスポンスボディに accessToken が含まれる
    And レスポンスボディに refreshToken が含まれる
    And 新しいアクセストークンが有効なJWTトークンである

  @positive
  Scenario: 新しいアクセストークンで保護されたリソースにアクセスできる
    Given ユーザー "admin" でログインする
    And リフレッシュトークンで新しいアクセストークンを取得する
    When 新しいアクセストークンで GET /api/messages を呼び出す
    Then ステータスコード 200 が返される

  @negative
  Scenario: 無効なリフレッシュトークンでトークンリフレッシュを試みる
    Given 以下のリフレッシュ情報を準備する:
      | refreshToken                |
      | invalid.refresh.token.value |
    When POST /api/auth/refresh を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である
    And エラーレスポンスに以下が含まれる:
      | フィールド | 値                       |
      | status    | 401                      |
      | title     | Authentication Error     |

  @negative
  Scenario: 期限切れのリフレッシュトークンでトークンリフレッシュを試みる
    Given 期限切れのリフレッシュトークンを準備する
    When 期限切れのリフレッシュトークンで POST /api/auth/refresh を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である

  @negative
  Scenario: 無効化されたリフレッシュトークンで トークンリフレッシュを試みる
    Given ユーザー "admin" でログインする
    And ログアウトしてリフレッシュトークンを無効化する
    When 無効化されたリフレッシュトークンで POST /api/auth/refresh を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である

  @negative @validation
  Scenario: リフレッシュトークンなしでリクエストする
    Given リフレッシュトークンなしでリクエストを準備する
    When POST /api/auth/refresh を呼び出す
    Then ステータスコード 400 が返される
    And エラーレスポンスがRFC 7807形式である
