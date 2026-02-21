# language: ja
@i18n @locale-persistence @ui
Feature: 言語設定の永続化

  ユーザーが選択した言語設定がページリロードやセッションをまたいで保持される機能。
  言語設定は localStorage に保存し、次回アクセス時に自動的に適用される。

  Background:
    Given アプリケーションが起動している

  @positive
  Scenario: 英語選択後にページをリロードしても英語が維持される
    Given ログイン画面 (/login) を開く
    And 言語切り替えボタンで「English」を選択している
    When ページをリロードする
    Then 言語設定が「English」のままである
    And ページタイトルが「Login」と表示される

  @positive
  Scenario: 日本語選択後にページをリロードしても日本語が維持される
    Given ログイン画面 (/login) を開く
    And 言語切り替えボタンで「English」を選択している
    When 言語切り替えボタンで「日本語」を選択する
    And ページをリロードする
    Then 言語設定が「日本語」のままである
    And ページタイトルが「ログイン」と表示される

  @positive
  Scenario: ログイン後も選択言語が引き継がれる
    Given ログイン画面 (/login) を開く
    And 言語切り替えボタンで「English」を選択している
    When 正しい認証情報でログインする
    Then メイン画面にリダイレクトされる
    And 言語設定が「English」のままである
    And ページタイトルが「Message Management」と表示される

  @positive
  Scenario: ログアウト後も選択言語が引き継がれる
    Given 管理者ユーザーでログイン済みである
    And メイン画面 (/) を開いている
    And 言語切り替えボタンで「English」を選択している
    When ログアウトボタンをクリックしてログアウトする
    Then ログイン画面にリダイレクトされる
    And 言語設定が「English」のままである
    And ページタイトルが「Login」と表示される

  @positive
  Scenario: 初回アクセス時のデフォルト言語は日本語である
    Given ブラウザのローカルストレージに言語設定が存在しない
    When ログイン画面 (/login) を開く
    Then 言語設定が「日本語」である
    And ページタイトルが「ログイン」と表示される

  @positive
  Scenario: 言語設定がlocalStorageに保存される
    Given ログイン画面 (/login) を開く
    When 言語切り替えボタンで「English」を選択する
    Then localStorage のキー「locale」の値が「en」である
