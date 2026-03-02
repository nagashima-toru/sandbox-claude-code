# language: ja
@frontend @component @i18n @login
Feature: ログインエラーメッセージの i18n Component テスト

  LoginPage で英語ロケール設定時にエラーメッセージが英語で表示されることを
  Component テストで検証する。
  E2E テスト禁止パターン（i18n テキスト表示検証）を Component テストに移行する。

  Background:
    Given `useAuth` フックがモックされている
    And `useRouter` がモックされている

  @positive
  Scenario: 英語設定時にログイン失敗エラーメッセージが英語で表示される
    Given LoginPage が英語ロケール（`createLocaleWrapper('en')`）でレンダリングされている
    And `login` モック関数が失敗（reject）するよう設定されている
    When ユーザー名とパスワードを入力してログインボタンをクリックする
    Then `[role="alert"]` にエラーメッセージが表示される
    And エラーメッセージのテキストが英語である（"Login failed" を含む）

  @positive
  Scenario: 日本語設定時にログイン失敗エラーメッセージが日本語で表示される
    Given LoginPage が日本語ロケール（`createLocaleWrapper('ja')`）でレンダリングされている
    And `login` モック関数が失敗（reject）するよう設定されている
    When ユーザー名とパスワードを入力してログインボタンをクリックする
    Then `[role="alert"]` にエラーメッセージが表示される
    And エラーメッセージのテキストが日本語である（"ログインに失敗しました" を含む）

  @negative
  Scenario: E2E テストから i18n テキスト検証を削除する
    Given `language-switch.spec.ts` に "ログインエラーメッセージが英語で表示される" テストが存在する
    Then そのテストは `language-switch.spec.ts` から削除されている
    And 代わりに LoginPage Component テストで検証されている
    And E2E テスト総数が削減されている（20件 → 19件以下）