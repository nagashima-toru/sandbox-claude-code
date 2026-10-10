# language: ja
@permission-ui @frontend
Feature: フォーム画面の権限制御UI

  ユーザーのロールに応じて、フォーム画面のUIを適切に表示・制御する機能

  Background:
    Given 以下のユーザーが登録されている:
      | username | password  | role   |
      | admin    | admin123  | ADMIN  |
      | viewer   | viewer123 | VIEWER |
    And 以下のメッセージが存在する:
      | id | code    | content      |
      | 1  | MSG_001 | Test Message |

  # メッセージ一覧画面
  @positive
  Scenario: 管理者ロールでメッセージ一覧を表示
    Given ユーザー "admin" でログインしている
    When メッセージ一覧画面を表示する
    Then "作成" ボタンが表示される
    And 各メッセージ行に "編集" ボタンが表示される
    And 各メッセージ行に "削除" ボタンが表示される

  @positive
  Scenario: 閲覧者ロールでメッセージ一覧を表示
    Given ユーザー "viewer" でログインしている
    When メッセージ一覧画面を表示する
    Then "作成" ボタンが非表示である
    And 各メッセージ行の "編集" ボタンが非表示である
    And 各メッセージ行の "削除" ボタンが非表示である
    And メッセージの内容は閲覧可能である

  # メッセージ作成フォーム
  @positive
  Scenario: 管理者ロールでメッセージ作成フォームを表示
    Given ユーザー "admin" でログインしている
    When "作成" ボタンをクリックする
    Then メッセージ作成フォームが表示される
    And "コード" 入力欄が入力可能である
    And "内容" 入力欄が入力可能である
    And "保存" ボタンが表示される
    And "キャンセル" ボタンが表示される

  @negative
  Scenario: 閲覧者ロールで作成ボタンを直接操作しようとする
    Given ユーザー "viewer" でログインしている
    When メッセージ一覧画面を表示する
    Then "作成" ボタンが非表示であり、クリックできない

  # メッセージ編集フォーム
  @positive
  Scenario: 管理者ロールでメッセージ編集フォームを表示
    Given ユーザー "admin" でログインしている
    And メッセージ一覧画面を表示している
    When メッセージ "MSG_001" の "編集" ボタンをクリックする
    Then メッセージ編集フォームが表示される
    And "コード" 入力欄に "MSG_001" が表示される
    And "コード" 入力欄が入力可能である
    And "内容" 入力欄が入力可能である
    And "保存" ボタンが表示される
    And "削除" ボタンが表示される
    And "キャンセル" ボタンが表示される

  @positive
  Scenario: 閲覧者ロールでメッセージ詳細を表示（読み取り専用）
    Given ユーザー "viewer" でログインしている
    And メッセージ一覧画面を表示している
    When メッセージ "MSG_001" の行をクリックする
    Then メッセージ詳細が読み取り専用モードで表示される
    And "コード" フィールドに "MSG_001" が表示される
    And "内容" フィールドが表示される
    And 入力欄はすべて無効化されている
    And "保存" ボタンが非表示である
    And "削除" ボタンが非表示である
    And "閉じる" ボタンのみが表示される

  # 権限不足時のフィードバック
  @positive
  Scenario: 閲覧者ロールで権限不足メッセージを表示
    Given ユーザー "viewer" でログインしている
    When メッセージ一覧画面を表示する
    Then 画面上部に "閲覧のみ可能です" という情報メッセージが表示される

  # ロール変更時の動的更新
  @positive
  Scenario: ロール変更後にUIが更新される
    Given ユーザー "viewer" でログインしている
    And メッセージ一覧画面を表示している
    When 管理者がユーザー "viewer" のロールを "ADMIN" に変更する
    And ページをリロードする
    Then "作成" ボタンが表示される
    And 各メッセージ行に "編集" ボタンが表示される
    And 各メッセージ行に "削除" ボタンが表示される