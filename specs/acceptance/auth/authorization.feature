# language: ja
@auth @authorization @api
Feature: 認可機能

  ロールベースのアクセス制御（RBAC）

  Background:
    Given 以下のユーザーが存在する:
      | username | password  | role   | enabled |
      | admin    | admin123  | ADMIN  | true    |
      | viewer   | viewer123 | VIEWER | true    |
    And 以下のメッセージが存在する:
      | code    | content   |
      | MSG_001 | Message 1 |

  # ADMIN ロールの権限テスト
  @positive @admin
  Scenario: ADMIN ユーザーはメッセージ一覧を取得できる
    Given ユーザー "admin" でログインする
    When 取得したアクセストークンで GET /api/messages を呼び出す
    Then ステータスコード 200 が返される

  @positive @admin
  Scenario: ADMIN ユーザーはメッセージ詳細を取得できる
    Given ユーザー "admin" でログインする
    When 取得したアクセストークンで GET /api/messages/1 を呼び出す
    Then ステータスコード 200 が返される

  @positive @admin
  Scenario: ADMIN ユーザーはメッセージを作成できる
    Given ユーザー "admin" でログインする
    And 以下のメッセージ情報を準備する:
      | code    | content       |
      | MSG_002 | New Message 2 |
    When 取得したアクセストークンで POST /api/messages を呼び出す
    Then ステータスコード 201 が返される

  @positive @admin
  Scenario: ADMIN ユーザーはメッセージを更新できる
    Given ユーザー "admin" でログインする
    And 以下のメッセージ情報を準備する:
      | code    | content           |
      | MSG_001 | Updated Message 1 |
    When 取得したアクセストークンで PUT /api/messages/1 を呼び出す
    Then ステータスコード 200 が返される

  @positive @admin
  Scenario: ADMIN ユーザーはメッセージを削除できる
    Given ユーザー "admin" でログインする
    When 取得したアクセストークンで DELETE /api/messages/1 を呼び出す
    Then ステータスコード 204 が返される

  # VIEWER ロールの権限テスト
  @positive @viewer
  Scenario: VIEWER ユーザーはメッセージ一覧を取得できる
    Given ユーザー "viewer" でログインする
    When 取得したアクセストークンで GET /api/messages を呼び出す
    Then ステータスコード 200 が返される

  @positive @viewer
  Scenario: VIEWER ユーザーはメッセージ詳細を取得できる
    Given ユーザー "viewer" でログインする
    When 取得したアクセストークンで GET /api/messages/1 を呼び出す
    Then ステータスコード 200 が返される

  @negative @viewer
  Scenario: VIEWER ユーザーはメッセージを作成できない
    Given ユーザー "viewer" でログインする
    And 以下のメッセージ情報を準備する:
      | code    | content       |
      | MSG_002 | New Message 2 |
    When 取得したアクセストークンで POST /api/messages を呼び出す
    Then ステータスコード 403 が返される
    And エラーレスポンスがRFC 7807形式である
    And エラーレスポンスに以下が含まれる:
      | フィールド | 値                                              |
      | status    | 403                                             |
      | title     | Authorization Error                             |
      | detail    | Insufficient permissions to access this resource |

  @negative @viewer
  Scenario: VIEWER ユーザーはメッセージを更新できない
    Given ユーザー "viewer" でログインする
    And 以下のメッセージ情報を準備する:
      | code    | content           |
      | MSG_001 | Updated Message 1 |
    When 取得したアクセストークンで PUT /api/messages/1 を呼び出す
    Then ステータスコード 403 が返される
    And エラーレスポンスがRFC 7807形式である

  @negative @viewer
  Scenario: VIEWER ユーザーはメッセージを削除できない
    Given ユーザー "viewer" でログインする
    When 取得したアクセストークンで DELETE /api/messages/1 を呼び出す
    Then ステータスコード 403 が返される
    And エラーレスポンスがRFC 7807形式である

  # 認証なしのアクセステスト
  @negative @unauthorized
  Scenario Outline: 認証なしで保護されたエンドポイントにアクセスできない
    Given 認証トークンなしでリクエストする
    When <method> <endpoint> を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である

    Examples:
      | method | endpoint          |
      | GET    | /api/messages     |
      | POST   | /api/messages     |
      | GET    | /api/messages/1   |
      | PUT    | /api/messages/1   |
      | DELETE | /api/messages/1   |

  # 無効なトークンでのアクセステスト
  @negative @unauthorized
  Scenario: 無効なトークンで保護されたエンドポイントにアクセスできない
    Given 無効なトークンでリクエストする
    When GET /api/messages を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である

  @negative @unauthorized
  Scenario: 期限切れトークンで保護されたエンドポイントにアクセスできない
    Given 期限切れのアクセストークンでリクエストする
    When GET /api/messages を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である
