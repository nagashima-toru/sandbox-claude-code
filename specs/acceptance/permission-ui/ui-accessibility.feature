# language: ja
@permission-ui @frontend @accessibility
Feature: 権限制御UIのアクセシビリティとユーザビリティ

  権限に応じたUI表示が適切にアクセシブルで、ユーザーフレンドリーであることを保証する

  Background:
    Given 以下のユーザーが登録されている:
      | username | password  | role   |
      | admin    | admin123  | ADMIN  |
      | viewer   | viewer123 | VIEWER |

  # 視覚的フィードバック
  @positive @visual
  Scenario: 読み取り専用モードの視覚的表現
    Given ユーザー "viewer" でログインしている
    When メッセージ一覧画面を表示する
    Then 画面全体が読み取り専用であることが視覚的に明確である
    And 無効化された要素は視覚的に識別可能である（グレーアウトなど）
    And 色だけに依存しない視覚的表現が使用されている

  # ARIA属性
  @positive @aria
  Scenario: 適切なARIA属性の設定
    Given ユーザー "viewer" でログインしている
    When メッセージ一覧画面を表示する
    Then 非表示のボタンには aria-hidden="true" が設定されている
    And 無効化された要素には aria-disabled="true" が設定されている
    And 読み取り専用フィールドには aria-readonly="true" が設定されている

  # ツールチップとヘルプテキスト
  @positive @tooltip
  Scenario: 閲覧者ロールに権限不足の説明を表示
    Given ユーザー "viewer" でログインしている
    When メッセージ一覧画面を表示する
    Then 画面上部に "このロールでは閲覧のみ可能です" という説明が表示される
    And 説明は視覚的に目立つが、邪魔にならない位置に配置されている

  @positive @tooltip
  Scenario: 非表示ボタンのツールチップ表示
    Given ユーザー "viewer" でログインしている
    When 通常ボタンが表示される位置にマウスオーバーする
    Then ツールチップで "この操作には管理者権限が必要です" と表示される

  # キーボードナビゲーション
  @positive @keyboard
  Scenario: 閲覧者ロールでのキーボードナビゲーション
    Given ユーザー "viewer" でログインしている
    And メッセージ一覧画面を表示している
    When Tab キーを押下する
    Then フォーカスは表示されている要素のみに移動する
    And 非表示のボタンにはフォーカスが移動しない

  # レスポンシブデザイン
  @positive @responsive
  Scenario: モバイル表示での権限制御
    Given ユーザー "viewer" でログインしている
    And 画面サイズを 375px x 667px に設定する
    When メッセージ一覧画面を表示する
    Then 権限制御UIがモバイルでも適切に表示される
    And "閲覧のみ可能です" メッセージが適切に配置されている
    And 操作不可能なボタンが非表示である

  # パフォーマンス
  @positive @performance
  Scenario: 権限チェックのパフォーマンス
    Given ユーザー "admin" でログインしている
    When メッセージ一覧画面を表示する
    Then ページ読み込みから 1 秒以内に権限に応じたUIが表示される
    And 権限チェックによる遅延が体感できない

  # エラーメッセージ
  @positive @error-message
  Scenario: 権限不足時のエラーメッセージ
    Given ユーザー "viewer" でログインしている
    When 何らかの方法で更新APIを直接呼び出そうとする
    Then 403 Forbidden エラーが返される
    And エラーメッセージに "この操作を実行する権限がありません" と表示される
    And エラーメッセージが RFC 7807 形式である