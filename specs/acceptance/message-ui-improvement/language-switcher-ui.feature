# language: ja
@message-ui @language-switcher @ui
Feature: 言語切り替えボタンのUI改善

  メイン画面の言語切り替えボタンが視覚的にわかりやすく、
  隣接するボタン（ログアウトボタン）と統一されたサイズで表示される。
  ボタンには地球儀アイコンと現在の言語名が表示される（スマホ幅ではアイコンのみ）。
  言語の選択は一覧メニューから行う。詳細は language-switch.feature と
  specs/ui/language-switch/screens.md を参照。

  Background:
    Given アプリケーションが起動している
    And 管理者ユーザーでログイン済みである
    And メイン画面 (/) を開いている

  @positive
  Scenario: 言語切り替えボタンとログアウトボタンの高さが統一されている
    Then 言語切り替えボタンとログアウトボタンの高さが同じである

  @positive
  Scenario: 日本語モードでボタンに地球儀アイコンと「日本語」が表示される
    When 言語切り替えボタンで「日本語」を選択する
    Then 言語切り替えボタンに地球儀アイコンと「日本語」が表示される

  @positive
  Scenario: 英語モードでボタンに地球儀アイコンと「English」が表示される
    When 言語切り替えボタンで「English」を選択する
    Then 言語切り替えボタンに地球儀アイコンと「English」が表示される
