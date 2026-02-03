# language: ja
@auth @login @api
Feature: ログイン機能

  JWTベースの認証機能

  Background:
    Given 以下のユーザーが存在する:
      | username | password  | role   | enabled |
      | admin    | admin123  | ADMIN  | true    |
      | viewer   | viewer123 | VIEWER | true    |
      | disabled | test123   | VIEWER | false   |

  @positive
  Scenario: 正しい認証情報でログインする（ADMIN）
    Given 以下のログイン情報を準備する:
      | username | password |
      | admin    | admin123 |
    When POST /api/auth/login を呼び出す
    Then ステータスコード 200 が返される
    And レスポンスボディに以下が含まれる:
      | フィールド | 値     |
      | tokenType | Bearer |
    And レスポンスボディに accessToken が含まれる
    And レスポンスボディに refreshToken が含まれる
    And レスポンスボディに expiresIn が含まれる
    And accessToken が有効なJWTトークンである
    And refreshToken が有効なJWTトークンである

  @positive
  Scenario: 正しい認証情報でログインする（VIEWER）
    Given 以下のログイン情報を準備する:
      | username | password  |
      | viewer   | viewer123 |
    When POST /api/auth/login を呼び出す
    Then ステータスコード 200 が返される
    And レスポンスボディに accessToken が含まれる
    And レスポンスボディに refreshToken が含まれる

  @negative
  Scenario: 誤ったパスワードでログインを試みる
    Given 以下のログイン情報を準備する:
      | username | password    |
      | admin    | wrongpasswd |
    When POST /api/auth/login を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である
    And エラーレスポンスに以下が含まれる:
      | フィールド | 値                       |
      | status    | 401                      |
      | title     | Authentication Error     |
      | detail    | Invalid credentials or token |

  @negative
  Scenario: 存在しないユーザーでログインを試みる
    Given 以下のログイン情報を準備する:
      | username    | password |
      | nonexistent | test     |
    When POST /api/auth/login を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である
    And エラーレスポンスに以下が含まれる:
      | フィールド | 値                       |
      | status    | 401                      |
      | title     | Authentication Error     |
      | detail    | Invalid credentials or token |

  @negative
  Scenario: 無効化されたユーザーでログインを試みる
    Given 以下のログイン情報を準備する:
      | username | password |
      | disabled | test123  |
    When POST /api/auth/login を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である

  @negative @validation
  Scenario Outline: バリデーションエラー - 必須フィールドの欠如
    Given 以下のログイン情報を準備する:
      | username   | password   |
      | <username> | <password> |
    When POST /api/auth/login を呼び出す
    Then ステータスコード 400 が返される
    And エラーレスポンスがRFC 7807形式である

    Examples:
      | username | password |
      |          | admin123 |
      | admin    |          |

  @positive @security
  Scenario: ログイン成功後、トークンで保護されたリソースにアクセスできる
    Given 以下のログイン情報でログインする:
      | username | password |
      | admin    | admin123 |
    When 取得したアクセストークンで GET /api/messages を呼び出す
    Then ステータスコード 200 が返される
