# language: ja
@message-table @i18n @ui
Feature: MessageTable の UI テキスト多言語対応

  MessageTable コンポーネント内のすべての UI テキストが、
  ハードコードではなく i18n（next-intl）経由で取得される。
  言語切り替えにより、全テキストが即座に更新される。

  Background:
    Given アプリケーションが起動している
    And 管理者ユーザーでログイン済みである
    And メイン画面 (/) を開いている

  # ローディング状態

  @positive
  Scenario: 日本語設定時にローディングテキストが日本語で表示される
    Given データ取得中の状態である
    When 言語切り替えボタンで「日本語」を選択している
    Then ローディングテキストが日本語で表示される

  @positive
  Scenario: 英語設定時にローディングテキストが英語で表示される
    Given データ取得中の状態である
    When 言語切り替えボタンで「English」を選択している
    Then ローディングテキストが英語で表示される

  # エラー状態

  @positive
  Scenario: 日本語設定時にエラーメッセージが日本語で表示される
    Given データ取得に失敗した状態である
    When 言語切り替えボタンで「日本語」を選択している
    Then エラーメッセージが日本語で表示される

  @positive
  Scenario: 英語設定時にエラーメッセージが英語で表示される
    Given データ取得に失敗した状態である
    When 言語切り替えボタンで「English」を選択している
    Then エラーメッセージが英語で表示される

  # 空状態（メッセージなし）

  @positive
  Scenario: 日本語設定時にメッセージ0件テキストが日本語で表示される
    Given メッセージが1件も登録されていない
    When 言語切り替えボタンで「日本語」を選択している
    Then メッセージ未登録のメインテキストが日本語で表示される
    And メッセージ未登録のヒントテキストが日本語で表示される

  @positive
  Scenario: 英語設定時にメッセージ0件テキストが英語で表示される
    Given メッセージが1件も登録されていない
    When 言語切り替えボタンで「English」を選択している
    Then メッセージ未登録のメインテキストが英語で表示される
    And メッセージ未登録のヒントテキストが英語で表示される

  # 検索結果なし状態

  @positive
  Scenario: 日本語設定時に検索結果なしテキストが日本語で表示される
    Given メッセージが1件以上登録されている
    And 検索条件に一致するメッセージが存在しない
    When 言語切り替えボタンで「日本語」を選択している
    Then 検索結果なしのメインテキストが日本語で表示される
    And 検索結果なしのヒントテキストが日本語で表示される

  @positive
  Scenario: 英語設定時に検索結果なしテキストが英語で表示される
    Given メッセージが1件以上登録されている
    And 検索条件に一致するメッセージが存在しない
    When 言語切り替えボタンで「English」を選択している
    Then 検索結果なしのメインテキストが英語で表示される
    And 検索結果なしのヒントテキストが英語で表示される

  # 読み取り専用情報メッセージ

  @positive
  Scenario: 日本語設定時に読み取り専用メッセージが日本語で表示される
    Given 閲覧者（VIEWER）ロールでログイン済みである
    And メイン画面 (/) を開いている
    When 言語切り替えボタンで「日本語」を選択している
    Then 読み取り専用情報メッセージが日本語で表示される

  @positive
  Scenario: 英語設定時に読み取り専用メッセージが英語で表示される
    Given 閲覧者（VIEWER）ロールでログイン済みである
    And メイン画面 (/) を開いている
    When 言語切り替えボタンで「English」を選択している
    Then 読み取り専用情報メッセージが英語で表示される

  # 言語切り替えの即時反映

  @positive
  Scenario: 言語切り替え後に全 UI テキストが即座に更新される
    Given メッセージが1件以上登録されている
    And 言語切り替えボタンで「日本語」を選択している
    When 言語切り替えボタンで「English」を選択する
    Then 検索バープレースホルダーが英語に切り替わる
    And ページタイトルが英語で表示される
    When 言語切り替えボタンで「日本語」を選択する
    Then 検索バープレースホルダーが日本語に切り替わる
    And ページタイトルが日本語で表示される

  # 翻訳キーの存在確認

  @positive
  Scenario: ja.json に必要なすべての翻訳キーが存在する
    Then 以下の翻訳キーが ja.json の messages セクションに存在する:
      | キー                 |
      | loading             |
      | loadError           |
      | noMessages          |
      | noMessagesHint      |
      | readOnlyInfo        |
      | noSearchResults     |
      | noSearchResultsHint |

  @positive
  Scenario: en.json に必要なすべての翻訳キーが存在する
    Then 以下の翻訳キーが en.json の messages セクションに存在する:
      | キー                 |
      | loading             |
      | loadError           |
      | noMessages          |
      | noMessagesHint      |
      | readOnlyInfo        |
      | noSearchResults     |
      | noSearchResultsHint |

  @negative
  Scenario: MessageTable.tsx にハードコードされた UI テキストが残っていない
    Then MessageTable.tsx に以下のハードコード文字列が含まれない:
      | 文字列                                                        |
      | Loading messages...                                          |
      | Failed to load messages. Please try again later.             |
      | No messages found.                                           |
      | Create your first message to get started.                    |
      | 閲覧のみ可能です。変更するには管理者に連絡してください。           |
      | No messages match your search.                               |
      | Try a different search term.                                 |
