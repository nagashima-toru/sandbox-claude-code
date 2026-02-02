# ADR-0001: OpenAPI-First 開発の採用

## ステータス

**採用** - 2026-01-29

## コンテキスト

API を開発する際、以下のアプローチが考えられます：

1. **Code-First（コードファースト）**: コードを書いてから仕様を生成
2. **Specification-First（仕様ファースト）**: OpenAPI 仕様を先に定義してからコードを生成

従来のアプローチでは、実装を先に行い、その後 springdoc-openapi-maven-plugin で仕様を自動生成していました。このアプローチには以下の課題がありました：

### 課題

1. **仕様と実装の不一致**
   - コードを変更しても仕様が自動更新されない場合がある
   - レビュー時に仕様の確認が困難
   - API 契約が曖昧

2. **仕様なしで実装可能**
   - 仕様を定義せずに実装 PR がマージできてしまう
   - API 設計のレビューが不十分
   - ドキュメントが後回しになる

3. **Frontend/Backend の連携問題**
   - API 契約が明確でないため、連携時に問題が発生
   - 型定義の不一致
   - コントラクトテストがない

4. **受け入れ条件の欠如**
   - 検証可能な受け入れ条件が定義されていない
   - テストの網羅性が不明確

## 決定

**OpenAPI-First（仕様ファースト）アプローチを採用し、SDD（Specification-Driven Development、仕様駆動開発）を導入する。**

### 開発フロー

```
1. Story 作成
   - ユーザーストーリー定義
   - 受け入れ条件（Gherkin 形式）定義
   ↓
2. 仕様 PR 作成
   - OpenAPI 仕様定義（specs/openapi/）
   - 受け入れ条件ファイル作成（specs/acceptance/）
   ↓
3. 仕様レビュー & マージ
   - OpenAPI lint（Spectral）
   - Breaking Changes 検出
   - "spec-approved" ラベル付与
   ↓
4. コード生成
   - Backend: openapi-generator-maven-plugin
   - Frontend: Orval
   ↓
5. 実装 PR 作成
   - 生成されたインターフェースを実装
   - spec-approved ラベル必須（CI で強制）
   ↓
6. テスト & マージ
   - OpenAPI 整合性チェック
   - 受け入れテスト実行
   - コントラクトテスト実行
```

### 実装内容

#### 1. OpenAPI 仕様管理

- **場所**: `specs/openapi/`
- **Single Source of Truth**: すべての API はここで定義
- **手動管理**: コード生成ではなく、手動で仕様を記述
- **lint**: Spectral で自動検証

#### 2. コード生成

**Backend（Java）:**
```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <configuration>
        <inputSpec>specs/openapi/openapi.yaml</inputSpec>
        <generatorName>spring</generatorName>
        <apiPackage>com.sandbox.api.presentation.generated.api</apiPackage>
        <modelPackage>com.sandbox.api.presentation.generated.model</modelPackage>
        <configOptions>
            <interfaceOnly>true</interfaceOnly>
            <useSpringBoot3>true</useSpringBoot3>
        </configOptions>
    </configuration>
</plugin>
```

**Frontend（TypeScript）:**
```typescript
// orval.config.ts
export default defineConfig({
  api: {
    input: {
      target: '../specs/openapi/openapi.yaml',
    },
    output: {
      client: 'react-query',
      target: './src/lib/api/generated/messages.ts',
    },
  },
});
```

#### 3. 仕様強制の仕組み

**Issue テンプレート:**
- Epic: 大機能のスコープ定義
- Story: ユーザーストーリー + 受け入れ条件（Gherkin 必須）
- Task: 実装タスク（親 Story 参照必須）

**PR テンプレート:**
- 仕様 PR: OpenAPI + Gherkin ファイル追加
- 実装 PR: 親 Story の spec-approved ラベル必須

**CI ワークフロー:**
```yaml
# implementation-check.yml
- name: Check spec-approved label
  run: |
    if (!parentStoryHasSpecApprovedLabel) {
      core.setFailed('親Storyの仕様が承認されていません。');
    }
```

#### 4. テスト自動化

**OpenAPI Validation Test:**
```java
@Test
void createMessage_shouldConformToOpenApiSpec() {
  given()
    .filter(validationFilter)  // OpenAPI 仕様で検証
    .body(requestBody)
  .when()
    .post("/api/messages")
  .then()
    .statusCode(201);
}
```

**Contract Testing:**
- Spring Cloud Contract で Frontend/Backend 間の契約を検証
- Gherkin で定義された受け入れ条件を自動テスト

## 結果

### 期待される効果

#### ✅ 仕様と実装の一貫性保証

- OpenAPI 仕様が常に最新
- コード生成により型安全性が向上
- Frontend/Backend の型定義が一致

#### ✅ 早期の API 設計レビュー

- コードを書く前に API 設計をレビュー
- Breaking Changes の早期検出
- API 契約の明確化

#### ✅ 自動化された品質保証

- CI で OpenAPI 仕様準拠を自動検証
- 受け入れ条件による検証可能性
- コントラクトテストによる契約保証

#### ✅ 開発効率の向上

- ボイラープレートコードの自動生成
- 型定義の手動メンテナンス不要
- ドキュメントの自動生成

#### ✅ 並行開発の促進

- API 仕様が確定すれば Frontend/Backend を並行開発可能
- モックサーバーの活用
- 結合テスト前の問題検出

### 導入コスト

#### 初期投資

- OpenAPI 仕様の学習コスト: **中**
- ツールチェーン構築: **高**（Phase 1-4）
- プロセス変更の浸透: **中**

#### 継続的コスト

- 仕様記述の時間: **中**（実装時間は削減）
- 仕様メンテナンス: **低**（自動検証により）

## トレードオフ

### 利点

- ✅ API 契約の明確化と強制
- ✅ 自動コード生成による生産性向上
- ✅ 型安全性の向上
- ✅ ドキュメントの常時最新化
- ✅ Breaking Changes の早期検出
- ✅ テスト自動化の向上

### 欠点

- ❌ 学習曲線がある（OpenAPI, Gherkin）
- ❌ 初期セットアップが複雑
- ❌ 小さな変更でも仕様更新が必要
- ❌ 生成コードのカスタマイズが困難

### リスク軽減策

**学習曲線 → ドキュメント整備**
- API 設計ガイドライン作成
- OpenAPI サンプル提供
- チーム内勉強会

**セットアップ複雑性 → 自動化**
- CI/CD パイプラインで自動化
- ローカル検証スクリプト提供

**仕様更新コスト → ツール活用**
- Spectral による lint で品質維持
- テンプレート提供

**カスタマイズ困難 → アーキテクチャ設計**
- 生成コードと手動コードの分離
- Clean Architecture による層分離

## 代替案

### 代替案 1: Code-First のまま

springdoc を使い続け、コードから仕様を生成。

**却下理由:**
- 仕様と実装の不一致リスク
- API 設計の事前レビュー困難
- 仕様なしで実装可能

### 代替案 2: ハイブリッドアプローチ

一部 API は Specification-First、一部は Code-First。

**却下理由:**
- 一貫性がない
- チームの混乱
- 品質のばらつき

### 代替案 3: API Blueprint / RAML

OpenAPI 以外の API 定義言語を使用。

**却下理由:**
- OpenAPI がデファクトスタンダード
- ツールエコシステムが豊富
- コミュニティサポートが充実

## 参照

### 関連ドキュメント

- [API 設計ガイドライン](../api/README.md)
- [エラーハンドリング仕様](../api/error-handling.md)
- [OpenAPI Specification](../../specs/openapi/openapi.yaml)
- [Acceptance Criteria](../../specs/acceptance/)

### 参考資料

- [OpenAPI Specification 3.0](https://spec.openapis.org/oas/v3.0.0)
- [RFC 7807 - Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc7807.html)
- [OpenAPI Generator](https://openapi-generator.tech/)
- [Spectral OpenAPI Linter](https://stoplight.io/open-source/spectral)

### 実装

- Phase 1: 仕様強制の仕組み
- Phase 2: コード生成パイプライン
- Phase 3: コントラクトテスト導入
- Phase 4: CI/CD 統合
- Phase 5: ドキュメント整備

## メンテナンス

### レビュー予定

- 3ヶ月後（2026-04-29）: 初回レビュー
- 6ヶ月後（2026-07-29）: 効果測定

### 評価指標

- API 契約違反の件数
- 受け入れテストカバレッジ
- 仕様と実装の不一致件数
- コード生成による生産性向上

---

**記録者**: Claude Sonnet 4.5
**承認者**: Development Team
**最終更新**: 2026-01-29
