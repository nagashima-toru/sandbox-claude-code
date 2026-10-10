# language: ja
@i18n @language-switch @ui
Feature: 言語切り替え機能

  ユーザーが日本語と英語の間でUIの表示言語を切り替えられる機能。
  言語切り替えボタンはすべての画面（ログイン画面・メイン画面）で利用可能。

  Background:
    Given アプリケーションが起動している

  @positive
  Scenario: ログイン画面に言語切り替えボタンが表示される
    When ログイン画面 (/login) を開く
    Then 言語切り替えボタン（日/EN）が表示される

  @positive
  Scenario: メイン画面に言語切り替えボタンが表示される
    Given 管理者ユーザーでログイン済みである
    When メイン画面 (/) を開く
    Then 言語切り替えボタン（日/EN）が表示される

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
