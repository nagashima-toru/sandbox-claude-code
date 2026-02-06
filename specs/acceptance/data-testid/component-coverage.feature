# language: ja
@data-testid @coverage @frontend
Feature: data-testid コンポーネントカバレッジ

  全フロントエンドコンポーネントに data-testid 属性が付与されていることを保証する。

  Background:
    Given フロントエンドアプリケーションが起動している

  @positive @page
  Scenario: ログインページの data-testid が存在する
    Given ログインページにアクセスする
    Then 以下の data-testid 要素が存在すること:
      | data-testid            |
      | login-page             |
      | login-username-input   |
      | login-password-input   |
      | login-submit-button    |

  @positive @page
  Scenario: メッセージ一覧ページの data-testid が存在する
    Given 認証済みユーザーでメッセージ一覧ページにアクセスする
    Then 以下の data-testid 要素が存在すること:
      | data-testid            |
      | messages-page          |
      | page-header            |
      | message-table          |
      | search-bar             |
      | create-message-button  |
      | pagination             |

  @positive @component
  Scenario: MessageTable コンポーネントの data-testid が存在する
    Given メッセージが1件以上登録されている
    And 認証済みユーザーでメッセージ一覧ページにアクセスする
    Then 以下の data-testid 要素が存在すること:
      | data-testid            |
      | message-table          |
      | message-table-header   |
    And message-row-{id} 形式の data-testid 要素がメッセージ件数分存在すること

  @positive @component
  Scenario: MessageForm コンポーネントの data-testid が存在する
    Given 認証済みユーザーでメッセージ一覧ページにアクセスする
    When 新規作成ボタンをクリックする
    Then 以下の data-testid 要素が存在すること:
      | data-testid            |
      | message-modal          |
      | message-form           |
      | message-code-input     |
      | message-content-input  |
      | message-form-submit    |
      | message-form-cancel    |

  @positive @component
  Scenario: DeleteConfirmDialog コンポーネントの data-testid が存在する
    Given メッセージが1件以上登録されている
    And 認証済みユーザーでメッセージ一覧ページにアクセスする
    When メッセージの削除ボタンをクリックする
    Then 以下の data-testid 要素が存在すること:
      | data-testid              |
      | delete-confirm-dialog    |
      | delete-confirm-button    |
      | delete-cancel-button     |

  @positive @component
  Scenario: SearchBar コンポーネントの data-testid が存在する
    Given 認証済みユーザーでメッセージ一覧ページにアクセスする
    Then 以下の data-testid 要素が存在すること:
      | data-testid        |
      | search-bar         |
      | search-input       |

  @positive @component
  Scenario: Pagination コンポーネントの data-testid が存在する
    Given ページネーションが表示される件数のメッセージが登録されている
    And 認証済みユーザーでメッセージ一覧ページにアクセスする
    Then 以下の data-testid 要素が存在すること:
      | data-testid              |
      | pagination               |
      | pagination-prev-button   |
      | pagination-next-button   |
      | pagination-info          |

  @positive @common
  Scenario: 共通コンポーネントの data-testid が存在する
    Given 認証済みユーザーでメッセージ一覧ページにアクセスする
    Then 以下の data-testid 要素が存在すること:
      | data-testid    |
      | page-header    |

  @positive @common
  Scenario: Loading コンポーネントの data-testid が存在する
    Given データ読み込み中の状態である
    Then 以下の data-testid 要素が存在すること:
      | data-testid      |
      | loading-spinner  |

  @positive @common
  Scenario: ErrorMessage コンポーネントの data-testid が存在する
    Given API エラーが発生した状態である
    Then 以下の data-testid 要素が存在すること:
      | data-testid    |
      | error-message  |
