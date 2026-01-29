# language: ja
@messages @list @api
Feature: メッセージ一覧取得

  メッセージ一覧取得機能の受け入れ条件

  Background:
    Given データベースが空の状態である

  @positive
  Scenario: メッセージ一覧を取得する
    Given 以下のメッセージが存在する:
      | code    | content   |
      | MSG_001 | Message 1 |
      | MSG_002 | Message 2 |
      | MSG_003 | Message 3 |
    When GET /api/messages を呼び出す
    Then ステータスコード 200 が返される
    And レスポンスに3件のメッセージが含まれる
    And メッセージが作成日時の降順でソートされている

  @positive
  Scenario: メッセージが存在しない場合は空の配列を返す
    Given データベースが空の状態である
    When GET /api/messages を呼び出す
    Then ステータスコード 200 が返される
    And レスポンスが空の配列である

  @positive
  Scenario: ページネーション - 1ページ目を取得
    Given 15件のメッセージが存在する
    When GET /api/messages?page=0&size=10 を呼び出す
    Then ステータスコード 200 が返される
    And レスポンスに10件のメッセージが含まれる
    And ページ情報が以下である:
      | フィールド     | 値    |
      | totalElements | 15   |
      | totalPages    | 2    |
      | currentPage   | 0    |

  @positive
  Scenario: ページネーション - 2ページ目を取得
    Given 15件のメッセージが存在する
    When GET /api/messages?page=1&size=10 を呼び出す
    Then ステータスコード 200 が返される
    And レスポンスに5件のメッセージが含まれる

  @negative @validation
  Scenario Outline: 不正なページネーションパラメータ
    Given メッセージが存在する
    When GET /api/messages?page=<page>&size=<size> を呼び出す
    Then ステータスコード 400 が返される
    And エラーレスポンスがRFC 7807形式である

    Examples:
      | page | size |
      | -1   | 10   |
      | 0    | 0    |
      | 0    | 101  |

  @negative @authorization
  Scenario: 認証なしでメッセージ一覧を取得しようとするとエラー
    Given 認証トークンなしでリクエストする
    When GET /api/messages を呼び出す
    Then ステータスコード 401 が返される
    And エラーレスポンスがRFC 7807形式である
