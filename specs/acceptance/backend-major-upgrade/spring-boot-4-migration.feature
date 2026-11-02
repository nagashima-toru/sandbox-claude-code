# language: ja
@backend-major-upgrade @spring-boot-4 @build
Feature: Spring Boot 4 系への移行

  バックエンドを Spring Boot 4 系へ移行し、mybatis-spring-boot-starter 4.x と
  springdoc-openapi 3.x を同時に更新する。API 契約（specs/openapi/）と DB スキーマは変更しない。

  Background:
    Given バックエンドが Spring Boot 3.5.x（Java 25）で動作している
    And 既存の受け入れ条件（specs/acceptance/**）がすべて満たされている

  @build
  Scenario: 依存関係が Boot 4 系に揃っている
    When backend/pom.xml を確認する
    Then spring-boot-starter-parent が 4.x である
    And mybatis-spring-boot-starter が 4.x である
    And springdoc-openapi-starter-webmvc-ui が 3.x である
    And Boot 3 系のオートコンフィグを持つ依存が混在していない

  @startup
  Scenario: アプリケーションが起動する
    When アプリケーションを起動する
    Then BeanDefinitionOverrideException なしで起動が完了する
    And Flyway のマイグレーションが成功する
    And /actuator/health が UP を返す

  @regression @api
  Scenario: 既存 API の挙動が変わらない
    When 既存の全エンドポイントを呼び出す
    Then ステータスコードとレスポンスボディが移行前と同一である
    And レスポンスが specs/openapi/openapi.yaml に適合する
    And 認証・認可（JWT、ロール別アクセス制御）の挙動が移行前と同一である
    And エラーレスポンスが RFC 7807 形式である

  @docs
  Scenario: Swagger UI が利用できる
    When Swagger UI を開く
    Then 画面が表示される

  @ci
  Scenario: 品質ゲートを通過する
    When ./scripts/ci-check-local.sh を実行する
    Then バックエンドの全テストが成功する
    And バックエンドのカバレッジが 90% 以上である
    And Spotless のチェックが成功する

  @ci @dependabot
  Scenario: Dependabot の保留 PR が解消される
    Given Boot 4 前提の更新 PR（#261, #262）が CI 失敗で保留されている
    When 本 Epic の移行が完了する
    Then #261 と #262 相当の更新が master に取り込まれている
