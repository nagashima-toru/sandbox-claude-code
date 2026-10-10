# Backend ベストプラクティス

このドキュメントは `/review-implementation` スキルおよび `/implement-epic` スキルが参照する
バックエンド実装の規約・パターン集です。

---

## 1. Clean Architecture 規則

### パッケージ構成

```
com.sandbox.api
├── domain/           # ビジネスロジック層（外部依存なし）
│   ├── model/        # ドメインエンティティ
│   └── repository/   # リポジトリインターフェース
├── application/      # アプリケーション層
│   └── usecase/      # ユースケース
├── infrastructure/   # インフラ層
│   └── persistence/  # MyBatis Mapper、リポジトリ実装
└── presentation/     # プレゼンテーション層
    └── controller/   # REST コントローラー
```

### レイヤー間依存ルール

| 依存元 | 依存先 | 可否 |
|--------|--------|------|
| domain | application / infrastructure / presentation | ❌ 禁止 |
| application | domain | ✅ 許可 |
| application | infrastructure / presentation | ❌ 禁止 |
| infrastructure | domain | ✅ 許可（インターフェース実装） |
| infrastructure | application | ❌ 禁止 |
| presentation | application | ✅ 許可 |
| presentation | domain | ✅ 許可（読み取り専用） |
| presentation | infrastructure | ❌ 禁止 |

**IMPORTANT**: application 層のクラスが `infrastructure` パッケージを import していないか必ず確認する。

### 違反チェック方法

```bash
# ArchitectureTest を実行して依存関係違反を検出
./mvnw test -Dtest=ArchitectureTest

# または全テスト実行（verify は ArchitectureTest も含む）
./mvnw verify
```

application または domain パッケージを変更したら必ず `./mvnw test -Dtest=ArchitectureTest` を実行する。

---

## 2. テスト戦略

### レイヤー別テスト対応表

| レイヤー | テスト種別 | ツール | カバレッジ目標 | 主な観点 |
|---------|-----------|--------|--------------|---------|
| Domain | Pure Unit | JUnit 5 | 90%+ | ビジネスロジック、バリデーション、等価性 |
| Application / UseCase | Unit + Mockito | JUnit 5 + Mockito | 85%+ | ユースケースフロー、異常系、境界値 |
| Infrastructure / Mapper | Integration | Testcontainers | 80%+ | SQL実行、型マッピング、トランザクション |
| Presentation / Controller | Integration | MockMvc | 80%+ | HTTP レスポンス、バリデーション、エラーハンドリング |

### テストメソッド命名規則

**パターン**: `methodName_condition_expectedBehavior`

```java
getMessage_whenMessageExists_returnsMessage()
getMessage_whenMessageNotFound_throwsException()
createUser_withDuplicateUsername_throwsConflictException()
```

### テスト構造: AAA（Arrange/Act/Assert）

```java
@Test
void methodName_condition_expectedBehavior() {
    // Arrange（テストデータと事前条件の準備）
    Message expected = new Message(1L, "Hello");
    when(repository.findById(1L)).thenReturn(Optional.of(expected));

    // Act（テスト対象の実行）
    String result = useCase.getMessage();

    // Assert（期待結果の検証）
    assertThat(result).isEqualTo("Hello");
}
```

### 統合テスト実行コマンド

```bash
# ✅ 正しい実行方法（verify ゴールを使用）
./mvnw verify

# ❌ 禁止（ポート競合が発生する）
./mvnw integration-test
```

詳細は [backend/docs/TEST_STRATEGY.md](./TEST_STRATEGY.md) を参照。

---

## 3. 実装パターン

### UseCase の標準構造

```java
@Service
@RequiredArgsConstructor
public class GetMessageUseCase {

    private final MessageRepository messageRepository;

    public Message execute(Long id) {
        return messageRepository.findById(id)
            .orElseThrow(() -> new MessageNotFoundException(id));
    }
}
```

### Repository Interface + Implementation の分離パターン

**Domain 層（インターフェース定義）**:

```java
// domain/repository/MessageRepository.java
public interface MessageRepository {
    Optional<Message> findById(Long id);
    List<Message> findAll();
    Message save(Message message);
    void deleteById(Long id);
}
```

**Infrastructure 層（実装）**:

```java
// infrastructure/persistence/MessageRepositoryImpl.java
@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageMapper messageMapper;

    @Override
    public Optional<Message> findById(Long id) {
        return Optional.ofNullable(messageMapper.findById(id));
    }
}
```

### MyBatis Mapper の標準構造

```java
// infrastructure/persistence/MessageMapper.java
@Mapper
public interface MessageMapper {
    Message findById(Long id);
    List<Message> findAll();
    void insert(Message message);
    void update(Message message);
    void deleteById(Long id);
}
```

### DTO の配置場所

| 種別 | 配置場所 | 用途 |
|------|---------|------|
| リクエスト/レスポンス DTO | `presentation/generated/model/` | OpenAPI から自動生成 |
| UseCase 入出力 DTO | `application/dto/` | レイヤー間のデータ転送 |

---

## 4. セキュリティ（OWASP Top 10 2025 準拠）

### A01:2025 アクセス制御の不備（Broken Access Control）

**認証チェック必須エンドポイントの判断基準**:

- ユーザーデータの取得・変更 → 認証必須
- 管理機能（ADMIN ロール必須） → `@PreAuthorize("hasRole('ADMIN')")`
- リソースの作成・更新・削除 → 認証 + 認可必須
- 読み取り専用パブリック API のみ認証不要

```java
// ✅ メソッドレベルの認可
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) { ... }

// ✅ 他人のリソースへのアクセスを防ぐ（水平権限昇格対策）
@PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
public ResponseEntity<UserDto> getUser(@PathVariable Long userId) { ... }
```

**水平権限昇格の防止**（重要）: 自分のリソースのみアクセス可能か、UseCase 内で所有者チェックを行う:

```java
// ✅ UseCase 内で所有者チェック
public Message getMyMessage(Long messageId, Long currentUserId) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new MessageNotFoundException(messageId));

    // 他人のメッセージにアクセスしようとした場合は 403
    if (!message.getOwnerId().equals(currentUserId)) {
        throw new AccessDeniedException("Access denied");
    }
    return message;
}
```

---

### A04:2025 暗号化の失敗（Cryptographic Failures）

**パスワードハッシュ**: BCrypt を使用し、毎回 `passwordEncoder.encode()` を個別呼び出し:

```java
// ✅ 正しい: 毎回個別に encode
jdbcTemplate.update(
    "INSERT INTO users (username, password_hash) VALUES (?, ?) ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash",
    "user1", passwordEncoder.encode("password123"));
jdbcTemplate.update(
    "INSERT INTO users (username, password_hash) VALUES (?, ?) ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash",
    "user2", passwordEncoder.encode("password123"));

// ❌ 禁止: ハッシュを使い回す（bcrypt は呼び出しごとに異なる salt を生成するため）
String hash = passwordEncoder.encode("password123");
jdbcTemplate.update(..., "user1", hash);  // user2 の認証が失敗する可能性
jdbcTemplate.update(..., "user2", hash);
```

**機密データの保護**:

```java
// ❌ 禁止: ログにパスワード・トークンを出力
log.info("Login attempt for user {} with password {}", username, password);
log.debug("JWT token: {}", token);

// ✅ 正しい: 機密データをログに含めない
log.info("Login attempt for user {}", username);
log.debug("JWT token issued for user {}", username);
```

---

### A05:2025 インジェクション（Injection）

**SQL インジェクション対策**: MyBatis の `#{}` プレースホルダーを使用:

```xml
<!-- ✅ 安全: PreparedStatement でエスケープ -->
<select id="findByUsername" resultType="User">
  SELECT * FROM users WHERE username = #{username}
</select>

<!-- ❌ 危険: 文字列として展開される（SQLインジェクション可能） -->
<select id="findByUsername" resultType="User">
  SELECT * FROM users WHERE username = '${username}'
</select>
```

**`${}` が必要な場合**（ORDER BY 列名など）は必ずホワイトリストで検証:

```java
// ✅ 列名をホワイトリストで検証してから ${}  で使用
private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("id", "created_at", "username");

public List<User> findAll(String sortColumn) {
    if (!ALLOWED_SORT_COLUMNS.contains(sortColumn)) {
        throw new IllegalArgumentException("Invalid sort column: " + sortColumn);
    }
    return userMapper.findAllOrderBy(sortColumn); // XML 内で ${sortColumn}
}
```

**コマンドインジェクション対策**: `Runtime.exec()` や `ProcessBuilder` にユーザー入力を渡さない:

```java
// ❌ 禁止: ユーザー入力をシェルコマンドに渡す
Runtime.getRuntime().exec("convert " + userInputFileName);

// ✅ 代替: Java ライブラリを使用する
```

---

### A02:2025 セキュリティの設定ミス（Security Misconfiguration）

**CORS 設定**: 信頼できるオリジンのみ許可:

```java
// ✅ 本番環境では具体的なオリジンを指定
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://example.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowCredentials(true);
    // ...
}

// ❌ 禁止: 全オリジンを許可
config.setAllowedOrigins(List.of("*"));
```

**エラーレスポンスの情報漏洩防止**: スタックトレースをクライアントに返さない:

```java
// ✅ RFC 7807 形式で汎用メッセージを返す
@ExceptionHandler(Exception.class)
public ProblemDetail handleGenericException(Exception e) {
    log.error("Unexpected error", e); // サーバーログには詳細を記録
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setTitle("Internal Server Error");
    pd.setDetail("An unexpected error occurred"); // クライアントには汎用メッセージのみ
    return pd;
}
```

---

### A07:2025 認証の失敗（Authentication Failures）

**JWT 検証**: 署名・有効期限・発行者を必ず検証する:

```java
// ✅ 署名と有効期限を検証
Claims claims = Jwts.parserBuilder()
    .setSigningKey(secretKey)
    .requireIssuer("sandbox-api")  // 発行者チェック
    .build()
    .parseClaimsJws(token)
    .getBody();
// 有効期限は parseClaimsJws が自動チェック
```

**ブルートフォース対策**: ログイン失敗回数をカウントしてロックアウト（Spring Security の設定で対応）。

---

### A09:2025 セキュリティログとアラートの失敗（Security Logging and Alerting Failures）

**記録すべきセキュリティイベント**:

```java
// ✅ 認証失敗を記録（ユーザー名のみ、パスワード不要）
log.warn("Failed login attempt for user: {}", username);

// ✅ 権限エラーを記録
log.warn("Access denied: user {} attempted to access resource {}", userId, resourceId);

// ✅ 重要なデータ変更を記録
log.info("User {} deleted message {}", currentUserId, messageId);
```

**記録してはいけない情報**:
- パスワード（平文・ハッシュ問わず）
- JWT トークン
- Cookie の値
- クレジットカード番号などの PII

---

### A10:2025 例外処理の不適切な扱い（Mishandling of Exceptional Conditions）

2025 版で新規追加。例外を握りつぶしたり、エラー状態を無視することで予期しない動作が生じる問題。

**例外を握りつぶさない**:

```java
// ❌ 禁止: 例外を無視して処理を続行
try {
    userRepository.save(user);
} catch (Exception e) {
    // 何もしない → データが保存されていないのに成功扱いになる
}

// ✅ 正しい: 例外をログに記録して再スローまたは適切にハンドリング
try {
    userRepository.save(user);
} catch (DataAccessException e) {
    log.error("Failed to save user {}", user.getUsername(), e);
    throw new UserSaveException("Failed to create user", e);
}
```

**null チェックを省略しない**:

```java
// ❌ 禁止: Optional を unwrap せずに使用
Optional<User> user = userRepository.findById(id);
return user.get(); // NoSuchElementException の可能性

// ✅ 正しい: 存在しない場合を明示的にハンドリング
return userRepository.findById(id)
    .orElseThrow(() -> new UserNotFoundException(id));
```

**トランザクション境界での例外ハンドリング**: `@Transactional` メソッド内で例外をキャッチして握りつぶすと、ロールバックが行われない:

```java
// ❌ 危険: 例外をキャッチしてロールバックをブロック
@Transactional
public void updateUser(User user) {
    try {
        userRepository.save(user);
        auditRepository.save(audit);
    } catch (Exception e) {
        log.error("Error", e); // ロールバックされず部分更新が残る
    }
}

// ✅ 正しい: 例外を再スローしてロールバックを確実に実行
@Transactional
public void updateUser(User user) {
    userRepository.save(user);
    auditRepository.save(audit); // 例外が発生したら自動ロールバック
}
```

---

## 5. パフォーマンス

> **方針**: 明らかなボトルネックを避ける。計測なしの最適化はしない。

### N+1 問題（最重要）

MyBatis で関連エンティティを取得する際に最も起きやすい問題。

```xml
<!-- ❌ N+1 問題: users を取得後、各 user に対して別クエリを発行 -->
<resultMap id="UserResult" type="User">
  <collection property="messages" select="findMessagesByUserId" column="id" />
</resultMap>

<!-- ✅ JOIN で一括取得 -->
<select id="findUsersWithMessages" resultMap="UserWithMessagesResult">
  SELECT u.*, m.id as m_id, m.content as m_content
  FROM users u
  LEFT JOIN messages m ON m.user_id = u.id
  WHERE u.id = #{id}
</select>
```

コレクションが必要ない場合は `<collection>` を使わず、必要なときだけ別クエリで取得する設計も有効（遅延ロード不要）。

### SELECT * 禁止

必要なカラムのみ取得する:

```xml
<!-- ❌ 全カラム取得（不要なデータ転送が発生） -->
<select id="findAll" resultType="User">
  SELECT * FROM users
</select>

<!-- ✅ 必要なカラムのみ -->
<select id="findAll" resultType="UserSummary">
  SELECT id, username, role, created_at FROM users
</select>
```

### ページネーションの実装

大量データを一括取得しない:

```xml
<!-- ✅ LIMIT/OFFSET でページネーション -->
<select id="findAll" resultType="Message">
  SELECT id, code, content FROM messages
  ORDER BY created_at DESC
  LIMIT #{limit} OFFSET #{offset}
</select>

<!-- ✅ 件数取得は COUNT のみ（SELECT * COUNT は禁止） -->
<select id="count" resultType="long">
  SELECT COUNT(*) FROM messages
</select>
```

### DB インデックスの基本

Flyway マイグレーションでインデックスを追加する判断基準:

```sql
-- WHERE 句や JOIN で頻繁に使う列にインデックスを追加
CREATE INDEX idx_messages_user_id ON messages(user_id);
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);

-- ユニーク制約はインデックスも兼ねる
ALTER TABLE users ADD CONSTRAINT uq_users_username UNIQUE (username);
```

**インデックスを追加すべき列**: 外部キー列、検索・ソート・JOIN で使う列。

### `@Transactional` の範囲を最小化

長いトランザクションは DB ロックの原因になる:

```java
// ❌ 外部 API 呼び出しをトランザクション内に含めない
@Transactional
public void processOrder(Order order) {
    orderRepository.save(order);
    externalPaymentApi.charge(order); // ← 外部呼び出しでトランザクションが長期化
}

// ✅ 外部呼び出しはトランザクション外で行う
public void processOrder(Order order) {
    saveOrder(order);           // トランザクション内
    externalPaymentApi.charge(order); // トランザクション外
}

@Transactional
private void saveOrder(Order order) {
    orderRepository.save(order);
}
```

---

## 6. よくあるアンチパターン

### ❌ `./mvnw integration-test` の使用

**問題**: 統合テスト後にサーバーが正常停止せず、次回実行時にポート競合が発生する。

**解決策**:

```bash
# ❌ 禁止
./mvnw integration-test

# ✅ 正しい方法
./mvnw verify
```

### ❌ パスワードハッシュの使い回し

**問題**: bcrypt は同一パスワードから異なるハッシュを生成するため、1つのハッシュを複数ユーザーで共有すると認証が失敗する場合がある。

**解決策**: `passwordEncoder.encode()` を各ユーザーで個別に呼び出す。

### ❌ `@Transactional` なしの統合テスト

**問題**: テストデータが DB に残り、後続テストが干渉を受ける。

**解決策**: 統合テストクラスに `@Transactional` を付与してテスト後に自動ロールバックする。

```java
@SpringBootTest
@Testcontainers
@Transactional  // ← 必須
class MessageRepositoryImplTest {
    // ...
}
```

### ❌ `ON CONFLICT DO NOTHING` でテストデータをセットアップ

**問題**: 古いデータが残存し、パスワードハッシュが更新されないため認証が失敗する。

**解決策**:

```java
// ✅ ON CONFLICT DO UPDATE で常に最新データを保証
jdbcTemplate.update(
    "INSERT INTO users (...) VALUES (?, ?, ...) ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash",
    "testuser", passwordEncoder.encode("password123"));
```

### ❌ domain 層から infrastructure 層への依存

**問題**: Clean Architecture 違反。ArchitectureTest が失敗する。

**解決策**: domain 層はインターフェースのみ定義し、実装は infrastructure 層に置く。application 層は domain インターフェースに依存する。

---

## 7. 標準タスクパターン（新規エンドポイント実装時）

新規エンドポイントを実装する Story のタスク分割テンプレート:

```
N.1  DB マイグレーション（Flyway）
N.2  Domain Model + 単体テスト（JUnit 5 Pure Unit）
N.3  Repository Interface 定義（domain 層）
N.4  MyBatis Mapper + 統合テスト（Testcontainers）
N.5  Repository Implementation + 単体テスト（Mockito）
N.6  UseCase + Mockito 単体テスト
N.7  Controller + MockMvc 統合テスト
```

---

## 参考資料

- [Backend テスト戦略](./TEST_STRATEGY.md)
- [Clean Architecture](../../docs/architecture/README.md)
- [backend/CLAUDE.md](../CLAUDE.md)
