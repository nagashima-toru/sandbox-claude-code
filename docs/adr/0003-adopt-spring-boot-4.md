# ADR-0003: Spring Boot 4.1 への移行

## ステータス

**採用** - 2026-11-03

## コンテキスト

Boot 3.5 のままでは mybatis-spring-boot-starter 4.x / springdoc 3.x を取り込めない（Boot 3 と Boot 4 のオートコンフィグが二重登録され `BeanDefinitionOverrideException` で起動しない。#261 / #262）。Boot 3.5 は 3.x 系の最終マイナーで、Dependabot の更新が今後も滞る。

## 決定

Spring Boot 4.1.2（Spring Framework 7、Spring Security 7、Jackson 3、Tomcat 11、Testcontainers 2）へ移行する。mybatis-spring-boot-starter 4.1.0 は Boot 4.1.0 基準のため 4.1 系に揃える。
`spring-cloud-starter-contract-verifier` はソースから未使用のため削除し、Spring Cloud への依存をなくす（Spring Cloud は Boot 4.1 向けが 2026.0.0-RC1 のみのため）。

## 代替案

- Boot 4.0.9 + mybatis 4.0.1 + Spring Cloud 2025.1.4: 安定版の組み合わせだが、Dependabot の更新 PR（mybatis 4.1.0）と食い違い、数か月後に再度 4.1 への移行が必要になる
- 3.5 のまま固定（Dependabot ignore）: 作業は不要だが、サポート終了後にセキュリティ修正を受けられない

## 移行で分かったこと

- Boot 4 では `spring-security-test` だけでは MockMvc に Security が適用されず、`spring-boot-starter-security-test` が必要（`@WithMockUser` のテストが 401 になる）
- Testcontainers 2 の `PostgreSQLContainer` は非ジェネリック。アーティファクトは `testcontainers-*`
- `rest-assured` は Boot 4 の BOM に含まれないため、版を明示する
- OpenAPI バリデータは `com.atlassian.oai:openapi-request-validator-*` へ移行（Java パッケージは同じ）

## 結果

- 依存ライブラリのメジャー追従が再び可能になる
- Jackson 3 / Spring Security 7 / Testcontainers 2 の API 変更に伴うコード修正が発生する
- jjwt-jackson は Jackson 2 に依存し続ける。
  2026-11-03 時点で jjwt の最新は 0.13.0（jackson-databind 2.x に依存）で、
  Jackson 3 対応は jwtk/jjwt の #1029 / #1032 / #1038 が open。解消条件: Jackson 3 対応版が公開された時点で更新（代替: jjwt-gson）。
  JWT ライブラリの見直しは #272 で扱う
- Dependabot の `org.springframework.boot:*` の `semver-major` ignore は維持する（Boot 5 への意図しない更新を防ぐ）。
  #261 / #262 の失敗は、周辺ライブラリのメジャーが Boot より先に上がったことが原因。
  今後 Boot 本体のメジャーに依存するライブラリの更新 PR が CI で失敗した場合は、Boot 側の対応可否を先に確認する
