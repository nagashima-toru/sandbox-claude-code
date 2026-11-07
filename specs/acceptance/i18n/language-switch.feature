# language: ja
@i18n @language-switch @ui
Feature: 言語切り替え機能

  ユーザーが日本語と英語の間でUIの表示言語を切り替えられる機能。
  言語切り替えボタンはすべての画面（ログイン画面・メイン画面）で利用可能。

  言語切り替えボタンは「地球儀アイコン＋現在の言語名」で、押すと言語の一覧メニューが開く。
  言語名はその言語の表記（日本語 / English）で、現在の言語にチェックが付く。
  画面幅が 768px 未満（スマホ幅）では、ボタンは地球儀アイコンのみになる。
  以下の「言語切り替えボタンで「X」を選択する」は、ボタンを押してメニューから「X」を選ぶ操作を指す。
  デザイン: specs/ui/language-switch/screens.md

  Background:
    Given アプリケーションが起動している

  @positive @ui-state-web-closed-ja
  Scenario: ログイン画面の言語切り替えボタンに現在の言語名が表示される
    When ログイン画面 (/login) を PC 幅で開く
    Then 言語切り替えボタンが表示される
    And ボタンに地球儀アイコンと「日本語」が表示される

  @positive @ui-state-web-closed-ja
  Scenario: メイン画面の言語切り替えボタンに現在の言語名が表示される
    Given 管理者ユーザーでログイン済みである
    When メイン画面 (/) を PC 幅で開く
    Then ヘッダーのログアウトボタンの隣に言語切り替えボタンが表示される
    And ボタンに地球儀アイコンと「日本語」が表示される

  @positive @ui-state-web-closed-en
  Scenario: 英語設定時はボタンに「English」が表示される
    Given ログイン画面 (/login) を PC 幅で開く
    When 言語切り替えボタンで「English」を選択する
    Then ボタンに地球儀アイコンと「English」が表示される

  @positive @ui-state-web-open
  Scenario: ボタンを押すと言語の一覧メニューが開き、現在の言語にチェックが付く
    Given ログイン画面 (/login) を PC 幅で開く
    When 言語切り替えボタンを押す
    Then メニューが開き、「日本語」と「English」が言語名で並ぶ
    And 現在の言語「日本語」にチェックが付く
    And 「English」にはチェックが付かない

  @positive
  Scenario: メニューから言語を選ぶと切り替わりメニューが閉じる
    Given ログイン画面 (/login) を PC 幅で開く
    And 言語切り替えメニューが開いている
    When メニューの「English」を選ぶ
    Then 画面の表示が英語に切り替わる
    And メニューが閉じる

  @positive
  Scenario: キーボードだけで言語を切り替えられる
    Given ログイン画面 (/login) を PC 幅で開く
    When 言語切り替えボタンにフォーカスして Enter を押す
    Then メニューが開く
    When 矢印キー（↓）で「English」に移動して Enter を押す
    Then 画面の表示が英語に切り替わる
    And メニューが閉じる

  @positive
  Scenario: Esc でメニューを閉じるとボタンにフォーカスが戻る
    Given ログイン画面 (/login) を PC 幅で開く
    And 言語切り替えメニューが開いている
    When Esc を押す
    Then メニューが閉じる
    And フォーカスが言語切り替えボタンに戻る

  @positive @ui-state-mobile-closed
  Scenario: スマホ幅では言語切り替えボタンが地球儀アイコンのみで表示される
    When ログイン画面 (/login) をスマホ幅（375px）で開く
    Then 言語切り替えボタンが表示される
    And ボタンには地球儀アイコンだけが表示され、言語名は表示されない
    And ボタンのアクセシブルな名前は「言語 / Language」である

  @positive @ui-state-mobile-open
  Scenario: スマホ幅でもボタンを押すと同じ言語の一覧メニューが開く
    Given ログイン画面 (/login) をスマホ幅（375px）で開く
    When 言語切り替えボタンを押す
    Then メニューが開き、「日本語」と「English」が言語名で並ぶ
    And 現在の言語「日本語」にチェックが付く

  @positive
  Scenario: 日本語選択時にログイン画面のUIテキストが日本語で表示される
    Given ログイン画面 (/login) を開く
    When 言語切り替えボタンで「日本語」を選択する
    Then ページタイトルが「ログイン」と表示される
    And ユーザー名ラベルが「ユーザー名」と表示される
    And パスワードラベルが「パスワード」と表示される
    And ログインボタンが「ログイン」と表示される

  @positive
  Scenario: 英語選択時にログイン画面のUIテキストが英語で表示される
    Given ログイン画面 (/login) を開く
    When 言語切り替えボタンで「English」を選択する
    Then ページタイトルが「Login」と表示される
    And ユーザー名ラベルが「Username」と表示される
    And パスワードラベルが「Password」と表示される
    And ログインボタンが「Login」と表示される

  @positive
  Scenario: 英語選択時にメイン画面のUIテキストが英語で表示される
    Given 管理者ユーザーでログイン済みである
    And メイン画面 (/) を開いている
    When 言語切り替えボタンで「English」を選択する
    Then ページタイトルが「Message Management」と表示される
    And ページ説明が「Manage all your messages in one place」と表示される
    And 新規作成ボタンが「New Message」と表示される
    And ログアウトボタンが「Logout」と表示される

  @positive
  Scenario: 日本語選択時にメイン画面のUIテキストが日本語で表示される
    Given 管理者ユーザーでログイン済みである
    And メイン画面 (/) を開いている
    When 言語切り替えボタンで「日本語」を選択する
    Then ページタイトルが「メッセージ管理」と表示される
    And ページ説明が「すべてのメッセージを一元管理します」と表示される
    And 新規作成ボタンが「新規作成」と表示される
    And ログアウトボタンが「ログアウト」と表示される

  @positive
  Scenario: テーブルヘッダーが選択言語で表示される
    Given 管理者ユーザーでログイン済みである
    And メイン画面 (/) を開いている
    When 言語切り替えボタンで「English」を選択する
    Then テーブルヘッダーが以下のように表示される:
      | 列     | 英語テキスト |
      | ID     | ID           |
      | Code   | Code         |
      | Content | Content     |
      | Actions | Actions     |

  @positive
  Scenario: ダイアログのテキストが選択言語で表示される
    Given 管理者ユーザーでログイン済みである
    And メイン画面 (/) を開いている
    And 言語切り替えボタンで「English」を選択している
    When 「New Message」ボタンをクリックして作成モーダルを開く
    Then モーダルタイトルが「Create Message」と表示される
    And 保存ボタンが「Save」と表示される
    And キャンセルボタンが「Cancel」と表示される

  @positive
  Scenario: ログインエラーメッセージが選択言語で表示される
    Given ログイン画面 (/login) を開く
    And 言語切り替えボタンで「English」を選択している
    When 誤ったパスワードでログインを試みる
    Then エラーメッセージが英語で表示される
    And エラーメッセージに「Login failed」が含まれる
