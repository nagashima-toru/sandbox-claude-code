# language: ja
@messages @create @api
Feature: メッセージ作成

  メッセージ作成機能の受け入れ条件

  Background:
    Given データベースが空の状態である

  @positive
  Scenario: 有効なデータでメッセージを作成する
    Given 以下のメッセージデータを準備する:
      | code    | content       |
      | MSG_001 | Hello, World! |
    When POST /api/messages を呼び出す
    Then ステータスコード 201 が返される
    And レスポンスボディに以下が含まれる:
      | フィールド | 値            |
      | code      | MSG_001       |
      | content   | Hello, World! |
    And データベースにメッセージが1件保存されている

  @positive
  Scenario: 長いコンテンツでメッセージを作成する
    Given 以下のメッセージデータを準備する:
      | code    | content                                                  |
      | MSG_002 | これは非常に長いメッセージです。1000文字まで保存できます。 |
    When POST /api/messages を呼び出す
    Then ステータスコード 201 が返される
    And データベースにメッセージが1件保存されている

  @positive
  Scenario: 特殊文字を含むメッセージを作成する
    Given 以下のメッセージデータを準備する:
      | code    | content                      |
      | MSG_003 | <script>alert('XSS')</script> |
    When POST /api/messages を呼び出す
    Then ステータスコード 201 が返される
    And レスポンスのcontentがエスケープされている

  @negative @validation
  Scenario: codeが空の場合バリデーションエラー
    Given 以下のメッセージデータを準備する:
      | code | content       |
      |      | Hello, World! |
    When POST /api/messages を呼び出す
    Then ステータスコード 400 が返される
    And エラーレスポンスがRFC 7807形式である
    And エラーメッセージに "code" が含まれる

  @negative @validation
  Scenario: contentが空の場合バリデーションエラー
    Given 以下のメッセージデータを準備する:
      | code    | content |
      | MSG_001 |         |
    When POST /api/messages を呼び出す
    Then ステータスコード 400 が返される
    And エラーレスポンスがRFC 7807形式である
    And エラーメッセージに "content" が含まれる

  @negative @validation
  Scenario Outline: バリデーションエラー - 複数パターン
    Given 無効なデータ: code="<code>", content="<content>"
    When POST /api/messages を呼び出す
    Then ステータスコード 400 が返される
    And エラーレスポンスがRFC 7807形式である
    And エラーメッセージに "<field>" が含まれる

    Examples:
      | code        | content       | field   |
      |             | valid content | code    |
      | VALID_CODE  |               | content |
      | TOO_LONG_CODE_OVER_50_CHARACTERS_ABCDEFGHIJKLMNOP | valid | code |

  @negative @business
  Scenario: 重複するcodeでメッセージを作成しようとするとエラー
    Given 以下のメッセージが既に存在する:
      | code    | content  |
      | MSG_001 | Existing |
    When 以下のメッセージデータで作成APIを呼び出す:
      | code    | content |
      | MSG_001 | New     |
    Then ステータスコード 409 が返される
    And エラーレスポンスがRFC 7807形式である
    And エラーメッセージに "already exists" が含まれる

  @negative @authorization
  Scenario: 認証なしでメッセージを作成しようとするとエラー
    Given 認証トークンなしでリクエストする
    When POST /api/messages を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である
