# Backend テスト戦略

## 1. テスト戦略の目的

- **品質保証**: コードの正確性と期待動作を保証
- **リグレッション防止**: 変更による既存機能の破壊を検出
- **ドキュメント**: テストコードが仕様書の役割を果たす
- **設計改善**: テスタビリティを意識した設計を促進
- **開発速度**: 安心してリファクタリング・機能追加できる環境

## 2. テストピラミッド

Clean Architectureに最適化したテスト配分:

```
        /\
       /  \  E2E Tests (5%)
      /____\
     /      \  Integration Tests (25%)
    /________\
   /          \
  / Unit Tests \ (70%)
 /______________\
```

### 推奨比率

- **Unit Tests**: 70% - 高速、独立、詳細なエッジケース
- **Integration Tests**: 25% - レイヤー間の結合、実際のDB
- **E2E Tests**: 5% - クリティカルなユーザーシナリオ

## 3. レイヤー別テスト戦略

### 3.1 Domain Layer（ドメイン層）

**対象**: `com.sandbox.api.domain.*`

**テストタイプ**: Pure Unit Tests

**特徴**:

- 外部依存なし（モック不要）
- 高速実行
- ビジネスロジックの正確性を検証

**例**: `MessageTest.java`

```java
class MessageTest {
    @Test
    void constructor_withValidData_createsMessage() {
        Message message = new Message(1L, "Hello");

        assertThat(message.getId()).isEqualTo(1L);
        assertThat(message.getContent()).isEqualTo("Hello");
    }

    @Test
    void equals_withSameId_returnsTrue() {
        // ドメインオブジェクトの等価性テスト
    }
}
```

**カバレッジ目標**: 90%以上

---

### 3.2 Application Layer（ユースケース層）

**対象**: `com.sandbox.api.application.usecase.*`

**テストタイプ**: Unit Tests with Mocks

**特徴**:

- Repositoryインターフェースをモック
- ビジネスロジックのフローを検証
- 高速実行

**例**: `GetMessageUseCaseTest.java`

```java
@ExtendWith(MockitoExtension.class)
class GetMessageUseCaseTest {
    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private GetMessageUseCase useCase;

    @Test
    void getMessage_whenMessageExists_returnsMessage() {
        // Arrange
        Message expected = new Message(1L, "Hello, World!");
        when(messageRepository.findById(1L)).thenReturn(Optional.of(expected));

        // Act
        String result = useCase.getMessage();

        // Assert
        assertThat(result).isEqualTo("Hello, World!");
        verify(messageRepository).findById(1L);
    }

    @Test
    void getMessage_whenMessageNotFound_throwsException() {
        when(messageRepository.findById(anyLong()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getMessage())
            .isInstanceOf(MessageNotFoundException.class);
    }
}
```

**カバレッジ目標**: 85%以上

---

### 3.3 Infrastructure Layer（インフラ層）

**対象**: `com.sandbox.api.infrastructure.persistence.*`

#### 3.3.1 Repository実装テスト

**テストタイプ**: Unit Tests with Mocked Mapper

**例**: `MessageRepositoryImplTest.java`

```java
@ExtendWith(MockitoExtension.class)
class MessageRepositoryImplTest {
    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageRepositoryImpl repository;

    @Test
    void findById_whenMapperReturnsMessage_returnsOptionalWithMessage() {
        Message expected = new Message(1L, "Test");
        when(messageMapper.findById(1L)).thenReturn(expected);

        Optional<Message> result = repository.findById(1L);

        assertThat(result).isPresent().contains(expected);
    }

    @Test
    void findById_whenMapperReturnsNull_returnsEmptyOptional() {
        when(messageMapper.findById(anyLong())).thenReturn(null);

        Optional<Message> result = repository.findById(1L);

        assertThat(result).isEmpty();
    }
}
```

#### 3.3.2 MyBatis Mapperテスト

**テストタイプ**: Integration Tests with Real Database

**例**: `MessageMapperTest.java`

```java
@SpringBootTest
@Testcontainers
@Transactional
class MessageMapperTest {
    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MessageMapper messageMapper;

    @Test
    void findById_withExistingId_returnsMessage() {
        Message result = messageMapper.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Hello, World!");
    }

    @Test
    void insert_withValidMessage_insertsSuccessfully() {
        Message newMessage = new Message(null, "New Message");

        messageMapper.insert(newMessage);

        Message result = messageMapper.findById(newMessage.getId());
        assertThat(result.getContent()).isEqualTo("New Message");
    }
}
```

**カバレッジ目標**: 80%以上

---

### 3.4 Presentation Layer（プレゼンテーション層）

**対象**: `com.sandbox.api.presentation.controller.*`

**テストタイプ**: Integration Tests with MockMvc

**特徴**:

- HTTP層の検証（リクエスト/レスポンス）
- JSONシリアライゼーション
- バリデーション
- エラーハンドリング

**例**: `MessageControllerTest.java`（既存を拡張）

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class MessageControllerTest {
    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMessage_whenMessageExists_returns200WithMessage() throws Exception {
        mockMvc.perform(get("/api/message"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, World!"));
    }

    @Test
    void getMessage_whenMessageNotFound_returns404() throws Exception {
        // データを削除してテスト
        mockMvc.perform(get("/api/message"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Message not found"));
    }

    @Test
    void createMessage_withInvalidData_returns400() throws Exception {
        mockMvc.perform(post("/api/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
}
```

**カバレッジ目標**: 80%以上

---

## 4. テストツールとフレームワーク

### 既存の構成（活用）

| ツール | 用途 | 対象レイヤー |
|--------|------|------------|
| **JUnit 5** | テストフレームワーク | 全レイヤー |
| **Mockito** | モッキング | Application, Infrastructure |
| **AssertJ** | 流暢なアサーション | 全レイヤー |
| **MockMvc** | HTTPテスト | Presentation |
| **Testcontainers** | 実DB環境 | Infrastructure, Presentation |
| **Spring Boot Test** | 統合テスト | Infrastructure, Presentation |

### 推奨追加ツール（必要に応じて）

| ツール | 用途 | 優先度 |
|--------|------|--------|
| **JaCoCo** | コードカバレッジ測定 | 高 |
| **ArchUnit** | アーキテクチャルール検証 | 中 |
| **Fixture Monkey** | テストデータ生成 | 低 |
| **WireMock** | 外部API連携テスト | 必要時 |

---

## 5. テスト命名規則とパターン

### クラス命名

```
[TestTarget] + "Test"

例:
- MessageTest
- GetMessageUseCaseTest
- MessageControllerTest
```

### メソッド命名

**パターン1**: `methodName_condition_expectedBehavior`

```java
getMessage_whenMessageExists_returnsMessage()
getMessage_whenMessageNotFound_throwsException()
```

**パターン2**: `should_expectedBehavior_when_condition`

```java
should_returnMessage_when_messageExists()
should_throwException_when_messageNotFound()
```

### テスト構造: AAA Pattern

```java
@Test
void testName() {
    // Arrange (Given): テストデータと事前条件の準備
    Message message = new Message(1L, "Test");
    when(repository.findById(1L)).thenReturn(Optional.of(message));

    // Act (When): テスト対象の実行
    String result = useCase.getMessage();

    // Assert (Then): 期待結果の検証
    assertThat(result).isEqualTo("Test");
}
```

---

## 6. カバレッジ目標

### レイヤー別目標

| レイヤー | 目標カバレッジ | 理由 |
|---------|--------------|------|
| Domain | 90%+ | ビジネスロジックの核心 |
| Application | 85%+ | ユースケースのフロー |
| Infrastructure | 80%+ | 技術的な実装詳細 |
| Presentation | 80%+ | API契約の保証 |

### プロジェクト全体

- **Line Coverage**: 80%以上
- **Branch Coverage**: 75%以上

### カバレッジ測定（JaCoCo導入）

**pom.xml に追加**:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**実行**:

```bash
./mvnw clean test jacoco:report
# レポート: target/site/jacoco/index.html
```

---

## 7. CI/CDでの統合

### GitHub Actions設定例

```yaml
name: Backend Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'

      - name: Run tests with coverage
        run: |
          cd backend
          ./mvnw clean test jacoco:report

      - name: Check coverage threshold
        run: |
          cd backend
          ./mvnw jacoco:check

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./backend/target/site/jacoco/jacoco.xml
```

---

## 8. テストのベストプラクティス

### 8.1 DO（推奨）

✅ **独立性**: 各テストは独立して実行可能

```java
@BeforeEach
void setUp() {
    // 各テストで新しいインスタンス
}
```

✅ **決定性**: 同じ入力で常に同じ結果

```java
// NG: ランダム値、現在時刻
// OK: 固定値、モック化した時刻
```

✅ **高速実行**: Unit Testは数ミリ秒で完了

```java
// モックを使って外部依存を排除
```

✅ **明確な失敗メッセージ**:

```java
assertThat(result)
    .as("User should be active after registration")
    .isTrue();
```

✅ **1テスト1検証**: 1つのテストで1つの観点

```java
@Test
void getMessage_returnsCorrectContent() {
    // content のみを検証
}

@Test
void getMessage_returnsCorrectId() {
    // id のみを検証
}
```

### 8.2 DON'T（非推奨）

❌ **テスト間の依存**:

```java
// NG: Test1の結果にTest2が依存
```

❌ **実装詳細のテスト**:

```java
// NG: private メソッドの直接テスト
// OK: public API経由で間接的に検証
```

❌ **過剰なモック**:

```java
// NG: すべてをモック化
// OK: 必要な依存のみモック
```

❌ **テストコードの重複**:

```java
// NG: 同じセットアップを各テストでコピペ
// OK: @BeforeEach や helper メソッドで共通化
```

---

## 9. テストデータ管理

### 9.1 統合テスト用データ

**test-data.sql の活用**:

```sql
-- src/test/resources/test-data.sql
DELETE FROM messages;
INSERT INTO messages (id, content) VALUES
    (1, 'Hello, World!'),
    (2, 'Test Message'),
    (99, 'Edge Case Message');
```

**実行タイミング**:

```yaml
# application-test.yml
spring:
  sql:
    init:
      mode: always
      data-locations: classpath:test-data.sql
```

### 9.2 Test Fixtures

**Builder Patternの活用**:

```java
public class MessageTestFixtures {
    public static Message.MessageBuilder defaultMessage() {
        return Message.builder()
            .id(1L)
            .content("Test Message");
    }

    public static Message messageWithId(Long id) {
        return defaultMessage().id(id).build();
    }
}
```

---

## 10. アーキテクチャテスト（推奨）

Clean Architectureの依存規則を自動検証:

### ArchUnit導入

**pom.xml**:

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.2.1</version>
    <scope>test</scope>
</dependency>
```

**テスト例**: `ArchitectureTest.java`

```java
@AnalyzeClasses(packages = "com.sandbox.api")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainLayerShouldNotDependOnOtherLayers =
        classes()
            .that().resideInPackage("..domain..")
            .should().onlyDependOnClassesIn("..domain..", "java..")
            .because("Domain layer must not depend on outer layers");

    @ArchTest
    static final ArchRule applicationLayerShouldNotDependOnInfrastructure =
        classes()
            .that().resideInPackage("..application..")
            .should().onlyDependOnClassesIn(
                "..application..", "..domain..", "java..", "org.springframework.."
            );

    @ArchTest
    static final ArchRule repositoriesShouldBeInterfaces =
        classes()
            .that().haveNameMatching(".*Repository")
            .and().resideInPackage("..domain.repository..")
            .should().beInterfaces();
}
```

---

## 11. テスト実行戦略

### 11.1 ローカル開発

```bash
# すべてのテストを実行
./mvnw test

# 特定のテストクラスのみ
./mvnw test -Dtest=MessageControllerTest

# 特定のテストメソッドのみ
./mvnw test -Dtest=MessageControllerTest#getMessage_returnsHelloWorld

# Unitテストのみ（高速）
./mvnw test -Dgroups=unit

# Integrationテストのみ
./mvnw test -Dgroups=integration
```

### 11.2 テストタグ化

```java
@Tag("unit")
class GetMessageUseCaseTest { }

@Tag("integration")
class MessageControllerTest { }
```

### 11.3 並列実行（高速化）

**junit-platform.properties**:

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.config.strategy=dynamic
```

---

## 12. 実装チェックリスト

新機能追加時のテスト実装ガイド:

### Domain Layer

- [ ] エンティティの生成テスト
- [ ] ビジネスルール検証テスト
- [ ] 等価性・ハッシュコードテスト
- [ ] バリデーションロジックテスト

### Application Layer

- [ ] 正常系テスト
- [ ] 異常系テスト（例外ケース）
- [ ] Repositoryモックによる単体テスト
- [ ] エッジケーステスト

### Infrastructure Layer

- [ ] Mapper単体テスト（モック使用）
- [ ] Mapper統合テスト（実DB使用）
- [ ] トランザクション動作テスト
- [ ] エラーハンドリングテスト

### Presentation Layer

- [ ] 正常系HTTPレスポンステスト
- [ ] 異常系HTTPステータステスト
- [ ] リクエストバリデーションテスト
- [ ] JSONシリアライゼーションテスト
- [ ] 認証・認可テスト（該当する場合）

---

## 13. まとめ

### テスト戦略の核心

1. **Clean Architectureに沿ったテスト設計**: 依存方向を守り、各レイヤーを独立してテスト
2. **適切なテストタイプの選択**: Unit/Integration/E2Eを適材適所で使い分け
3. **高速フィードバック**: 開発中は高速なUnitテストを中心に実行
4. **継続的改善**: カバレッジを測定し、品質を可視化
5. **自動化**: CI/CDで全テストを自動実行し、品質を保証

### 段階的な導入

**Phase 1（即座に実施）**:

- 既存の `MessageController` テストを拡張
- `GetMessageUseCase` の単体テストを追加
- JaCoCo導入でカバレッジ測定開始

**Phase 2（短期目標）**:

- 全レイヤーのテスト実装
- カバレッジ80%達成
- CI/CD統合

**Phase 3（中長期目標）**:

- ArchUnitでアーキテクチャ保護
- テストパフォーマンス最適化
- E2Eテスト追加（必要に応じて）

---

この戦略に従うことで、高品質で保守性の高いバックエンドシステムを構築できます。
