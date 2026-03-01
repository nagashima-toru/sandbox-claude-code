# language: ja
@message-ui @language-switcher @ui
Feature: 言語切り替えボタンのUI改善

  メイン画面の言語切り替えボタンが視覚的にわかりやすく、
  隣接するボタン（ログアウトボタン）と統一されたサイズで表示される。
  ボタンには地球アイコン（🌐）と現在の言語名が表示される。

  Background:
    Given アプリケーションが起動している
    And 管理者ユーザーでログイン済みである
    And メイン画面 (/) を開いている

  @positive
  Scenario: 言語切り替えボタンとログアウトボタンの高さが統一されている
    Then 言語切り替えボタンとログアウトボタンの高さが同じである

  @positive
  Scenario: 日本語モードで「🌐 日本語」と表示される
    When 言語切り替えボタンで「日本語」を選択する
    Then 言語切り替えボタンに「🌐 日本語」と表示される

  @positive
  Scenario: 英語モードで「🌐 English」と表示される
    When 言語切り替えボタンで「English」を選択する
    Then 言語切り替えボタンに「🌐 English」と表示される

  @positive
  Scenario: 言語切り替えボタンをクリックすると言語が切り替わる
    Given 言語切り替えボタンに「🌐 日本語」と表示されている
    When 言語切り替えボタンをクリックする
    Then 言語切り替えボタンに「🌐 English」と表示される
