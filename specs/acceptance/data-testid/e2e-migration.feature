# language: ja
@data-testid @e2e @migration
Feature: e2e テストの data-testid 移行

  既存の e2e テストを data-testid ベースのセレクタに移行し、
  テストの安定性と保守性を向上させる。

  @positive @selector
  Scenario: CSS クラスセレクタを data-testid に置換する
    Given 以下の CSS クラスセレクタが e2e テストで使用されている:
      | 現在のセレクタ              | 用途                  |
      | .md\\:block table          | デスクトップテーブル表示  |
      | .md\\:hidden               | モバイル表示            |
    Then 以下の data-testid セレクタに置換されていること:
      | 新しいセレクタ                                  | 用途                  |
      | [data-testid="message-table-desktop"]          | デスクトップテーブル表示  |
      | [data-testid="message-table-mobile"]           | モバイル表示            |

  @positive @selector
  Scenario: テキストベースセレクタを data-testid で補強する
    Given 行の特定にテキストベースセレクタが使用されている:
      | 現在のセレクタ                | 用途          |
      | tr:has-text("{code}")        | メッセージ行特定 |
    Then data-testid で行を特定できること:
      | 新しいセレクタ                        | 用途          |
      | [data-testid="message-row-{id}"]    | メッセージ行特定 |

  @positive @selector
  Scenario: ID/Name セレクタを data-testid に置換する
    Given 以下の ID/Name セレクタが e2e テストで使用されている:
      | 現在のセレクタ              | 用途              |
      | input[id="username"]       | ユーザー名入力      |
      | input[id="password"]       | パスワード入力      |
      | input[name="code"]         | メッセージコード入力 |
      | textarea[name="content"]   | メッセージ内容入力   |
    Then 以下の data-testid セレクタに置換されていること:
      | 新しいセレクタ                                    | 用途              |
      | [data-testid="login-username-input"]             | ユーザー名入力      |
      | [data-testid="login-password-input"]             | パスワード入力      |
      | [data-testid="message-code-input"]               | メッセージコード入力 |
      | [data-testid="message-content-input"]            | メッセージ内容入力   |

  @positive @helper
  Scenario: テストヘルパー関数が data-testid を使用する
    Given tests/e2e/helpers.ts のヘルパー関数が更新されている
    Then 以下のヘルパー関数が data-testid セレクタを使用すること:
      | ヘルパー関数              | 使用する data-testid                  |
      | login()                  | login-username-input, login-password-input, login-submit-button |
      | waitForModal()           | message-modal                        |
      | fillMessageForm()        | message-code-input, message-content-input |
      | openCreateModal()        | create-message-button                |
      | saveModalForm()          | message-form-submit                  |
      | cancelModalForm()        | message-form-cancel                  |
      | deleteMessage()          | delete-message-button-{id}, delete-confirm-button |
      | performSearch()          | search-input                         |
      | clearSearch()            | search-clear-button                  |

  @positive @coexistence
  Scenario: getByRole セレクタとの共存方針
    Given アクセシビリティベースのセレクタ（getByRole）が使用されている
    Then 以下の方針に従うこと:
      | 方針                                              |
      | getByRole は引き続き使用可能（アクセシビリティテスト目的） |
      | 要素の特定が曖昧な場合は data-testid を優先する          |
      | CSS クラスセレクタは data-testid に完全置換する          |
      | テキストベースセレクタは data-testid で補強する          |

  @positive @responsive
  Scenario: レスポンシブテストで data-testid を使用する
    Given レスポンシブデザインの e2e テストが存在する
    Then 以下のビューポート切替テストが data-testid を使用すること:
      | テスト内容              | 使用する data-testid          |
      | デスクトップ表示確認     | message-table-desktop         |
      | モバイル表示確認        | message-table-mobile          |
      | テーブルヘッダー確認     | message-table-header          |
      | メッセージ行確認        | message-row-{id}              |
