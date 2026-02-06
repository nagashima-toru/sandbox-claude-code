# language: ja
@data-testid @naming @frontend
Feature: data-testid 命名規則

  フロントエンドコンポーネントの data-testid 属性命名規則を定義し、
  e2e テストの安定性と保守性を向上させる。

  @convention
  Scenario: 命名規則のフォーマット
    Given data-testid の命名規則が定義されている
    Then 以下のフォーマットに従うこと:
      | ルール                     | 説明                                              |
      | kebab-case                | 全て小文字のハイフン区切り                          |
      | プレフィックスなし          | コンポーネント名をそのまま使用                       |
      | 構造: {component}-{element} | コンポーネント名とサブ要素をハイフンで結合             |
      | 動的ID: {component}-{id}   | 動的な要素にはユニークID（レコードIDなど）をサフィックスに付与 |

  @convention
  Scenario: ページレベルの data-testid
    Given 各ページのルート要素に data-testid が付与されている
    Then 以下の命名に従うこと:
      | data-testid       | 対象コンポーネント |
      | login-page        | ログインページ     |
      | messages-page     | メッセージ一覧ページ |

  @convention
  Scenario: 共通コンポーネントの data-testid
    Given 共通コンポーネントに data-testid が付与されている
    Then 以下の命名に従うこと:
      | data-testid       | 対象コンポーネント |
      | page-header       | PageHeader        |
      | loading-spinner   | Loading           |
      | error-message     | ErrorMessage      |

  @convention
  Scenario: メッセージ機能コンポーネントの data-testid
    Given メッセージ機能のコンポーネントに data-testid が付与されている
    Then 以下の命名に従うこと:
      | data-testid                       | 対象コンポーネント / 要素          |
      | message-table                     | MessageTable ルート要素           |
      | message-table-desktop             | デスクトップ表示のテーブル          |
      | message-table-mobile              | モバイル表示のカード一覧           |
      | message-table-header              | MessageTableHeader               |
      | message-row-{id}                  | MessageTableRow（各行）           |
      | message-form                      | MessageForm                      |
      | message-modal                     | MessageModal                     |
      | delete-confirm-dialog             | DeleteConfirmDialog              |
      | search-bar                        | SearchBar                        |
      | pagination                        | Pagination                       |

  @convention
  Scenario: フォーム要素の data-testid
    Given フォーム要素に data-testid が付与されている
    Then 以下の命名に従うこと:
      | data-testid                | 対象要素                |
      | login-username-input       | ログインユーザー名入力    |
      | login-password-input       | ログインパスワード入力    |
      | login-submit-button        | ログインボタン           |
      | message-code-input         | メッセージコード入力      |
      | message-content-input      | メッセージ内容入力        |
      | message-form-submit        | メッセージ保存ボタン      |
      | message-form-cancel        | メッセージキャンセルボタン |

  @convention
  Scenario: アクションボタンの data-testid
    Given アクションボタンに data-testid が付与されている
    Then 以下の命名に従うこと:
      | data-testid                  | 対象要素                    |
      | create-message-button        | 新規作成ボタン               |
      | edit-message-button-{id}     | 編集ボタン（各行）           |
      | delete-message-button-{id}   | 削除ボタン（各行）           |
      | delete-confirm-button        | 削除確認ダイアログの確認ボタン |
      | delete-cancel-button         | 削除確認ダイアログのキャンセルボタン |

  @convention
  Scenario: 検索・ページネーション要素の data-testid
    Given 検索とページネーション要素に data-testid が付与されている
    Then 以下の命名に従うこと:
      | data-testid                | 対象要素          |
      | search-input               | 検索テキスト入力    |
      | search-clear-button        | 検索クリアボタン   |
      | pagination-prev-button     | 前ページボタン     |
      | pagination-next-button     | 次ページボタン     |
      | pagination-page-{n}        | ページ番号ボタン   |
      | pagination-info            | ページ情報テキスト  |
