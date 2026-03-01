# language: ja
@qa-process @spec-reading @skills
Feature: スキル実行時の仕様ドキュメント読み込み

  確定した仕様（OpenAPI + 受け入れ条件）に対する検証を QA プロセスに組み込む。
  テストケース作成・実装・レビューの各フェーズで仕様ドキュメントを読み込んでから作業する。

  Background:
    Given 以下の仕様ドキュメントが存在する:
      | ファイル                          | 内容             |
      | specs/openapi/openapi.yaml        | OpenAPI 仕様     |
      | specs/acceptance/[機能]/*.feature | Gherkin 受け入れ条件 |

  # test-story スキル

  @test-story
  Scenario: test-story が仕様ドキュメントを読み込んでからテスト計画を立てる
    Given Epic の受け入れ条件ファイルが specs/acceptance/ に存在する
    And OpenAPI 仕様が specs/openapi/openapi.yaml に存在する
    When test-story スキルを実行する
    Then スキルは最初に specs/acceptance/ の受け入れ条件を読み込む
    And スキルは specs/openapi/openapi.yaml を読み込む
    And テスト計画が受け入れ条件のシナリオを網羅している
    And テスト計画が OpenAPI 仕様のエンドポイントを網羅している

  @test-story @negative
  Scenario: 受け入れ条件と乖離したテストケースを検出する
    Given 受け入れ条件に「正常系: ステータス 200 が返される」のシナリオがある
    When test-story スキルが仕様を読み込まずにテスト計画を立てた場合
    Then 受け入れ条件に存在しないテストケースが作成される可能性がある
    And 受け入れ条件に存在するシナリオが漏れる可能性がある

  # review-implementation スキル（plan モード）

  @review-implementation @plan-mode
  Scenario: review-implementation plan が仕様ドキュメントを読み込んでから計画をレビューする
    Given .epic/ ディレクトリに実装計画が存在する
    And Epic の受け入れ条件ファイルが specs/acceptance/ に存在する
    And OpenAPI 仕様が specs/openapi/openapi.yaml に存在する
    When review-implementation plan モードで実行する
    Then スキルは BEST_PRACTICES.md に加えて受け入れ条件も読み込む
    And スキルは OpenAPI 仕様も読み込む
    And 実装計画が受け入れ条件の全シナリオをカバーしているか検証する
    And 実装計画が OpenAPI 仕様の全エンドポイントをカバーしているか検証する

  @review-implementation @story-mode
  Scenario: review-implementation story が仕様ドキュメントを読み込んでから実装をレビューする
    Given Story の実装コードが存在する
    And Epic の受け入れ条件ファイルが specs/acceptance/ に存在する
    And OpenAPI 仕様が specs/openapi/openapi.yaml に存在する
    When review-implementation story モードで実行する
    Then スキルは BEST_PRACTICES.md に加えて受け入れ条件も読み込む
    And スキルは OpenAPI 仕様も読み込む
    And 実装が受け入れ条件を満たしているか検証する
    And 実装が OpenAPI 仕様に準拠しているか検証する

  # implement-epic スキル

  @implement-epic
  Scenario: implement-epic が仕様ドキュメントを読み込んでから実装を開始する
    Given Epic に spec-approved ラベルが付いている
    And Epic の受け入れ条件ファイルが specs/acceptance/ に存在する
    And OpenAPI 仕様が specs/openapi/openapi.yaml に存在する
    When implement-epic スキルを実行する
    Then スキルは BEST_PRACTICES.md と同時に受け入れ条件を読み込む
    And スキルは OpenAPI 仕様も読み込む
    And 実装が仕様に基づいて進行する

  @implement-epic @negative
  Scenario: 仕様を確認せずに実装すると仕様乖離が発生する
    Given Epic の受け入れ条件に「エラー時は RFC 7807 形式で返す」とある
    When 仕様ドキュメントを読み込まずに実装した場合
    Then エラーレスポンス形式が仕様と異なる実装が作成される可能性がある
    And レビュー時に手戻りが発生する