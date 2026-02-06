# エラーハンドリング仕様

## 概要

このドキュメントは、Sandbox プロジェクトにおけるエラーハンドリングの標準を定義します。すべてのエラーレスポンスは **RFC 7807 (Problem Details for HTTP APIs)** 形式に従います。

## RFC 7807 Problem Details

### 基本形式

```json
{
  "type": "https://api.example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "The request body contains invalid fields.",
  "instance": "/api/messages",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

Content-Type: `application/problem+json`

### フィールド説明

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| `type` | string (URI) | ✅ | エラータイプを識別する URI。デフォルト: `about:blank` |
| `title` | string | ✅ | エラーの概要（人間が読める形式） |
| `status` | integer | ✅ | HTTP ステータスコード |
| `detail` | string | ⬜ | エラーの詳細説明 |
| `instance` | string (URI) | ⬜ | エラーが発生したリクエストの URI |
| `timestamp` | string (ISO 8601) | ⬜ | エラー発生日時（拡張フィールド） |
| `errors` | array | ⬜ | バリデーションエラーの詳細リスト（拡張フィールド） |

## エラータイプ一覧

### 1. Validation Error (400)

**用途**: リクエストボディのバリデーションエラー

**type**: `https://api.example.com/errors/validation-error`

**例**:

```json
{
  "type": "https://api.example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "The request body contains invalid fields.",
  "instance": "/api/messages",
  "timestamp": "2024-01-01T00:00:00Z",
  "errors": [
    {
      "field": "code",
      "rejectedValue": "",
      "message": "コードは必須です"
    },
    {
      "field": "content",
      "rejectedValue": null,
      "message": "コンテンツは必須です"
    }
  ]
}
```

**Java 実装**:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationErrors(
    MethodArgumentNotValidException ex, HttpServletRequest request) {

  List<ErrorResponse.ValidationError> validationErrors =
      ex.getBindingResult().getFieldErrors().stream()
          .map(error -> new ErrorResponse.ValidationError(
              error.getField(),
              error.getRejectedValue(),
              error.getDefaultMessage()))
          .collect(Collectors.toList());

  ErrorResponse error = ErrorResponse.builder()
      .type(ERROR_TYPE_BASE_URI + "/validation-error")
      .title("Validation Error")
      .status(HttpStatus.BAD_REQUEST.value())
      .detail("The request body contains invalid fields.")
      .instance(request.getRequestURI())
      .timestamp(LocalDateTime.now())
      .errors(validationErrors)
      .build();

  return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .contentType(PROBLEM_JSON)
      .body(error);
}
```

### 2. Invalid Parameter (400)

**用途**: クエリパラメータの不正

**type**: `https://api.example.com/errors/invalid-parameter`

**例**:

```json
{
  "type": "https://api.example.com/errors/invalid-parameter",
  "title": "Invalid Parameter",
  "status": 400,
  "detail": "The query parameter 'page' must be non-negative.",
  "instance": "/api/messages?page=-1",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 3. Unauthorized (401)

**用途**: 認証エラー

**type**: `https://api.example.com/errors/unauthorized`

**例**:

```json
{
  "type": "https://api.example.com/errors/unauthorized",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Authentication is required to access this resource.",
  "instance": "/api/messages",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 4. Invalid Token (401)

**用途**: 認証トークン無効

**type**: `https://api.example.com/errors/invalid-token`

**例**:

```json
{
  "type": "https://api.example.com/errors/invalid-token",
  "title": "Invalid Token",
  "status": 401,
  "detail": "The authentication token is invalid or expired.",
  "instance": "/api/messages",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 5. Not Found (404)

**用途**: リソースが見つからない

**type**: `https://api.example.com/errors/not-found`

**例**:

```json
{
  "type": "https://api.example.com/errors/not-found",
  "title": "Not Found",
  "status": 404,
  "detail": "The requested message was not found.",
  "instance": "/api/messages/999",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

**Java 実装**:

```java
@ExceptionHandler(MessageNotFoundException.class)
public ResponseEntity<ErrorResponse> handleMessageNotFound(
    MessageNotFoundException ex, HttpServletRequest request) {

  ErrorResponse error = ErrorResponse.builder()
      .type(ERROR_TYPE_BASE_URI + "/not-found")
      .title("Not Found")
      .status(HttpStatus.NOT_FOUND.value())
      .detail(ex.getMessage())
      .instance(request.getRequestURI())
      .timestamp(LocalDateTime.now())
      .build();

  return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .contentType(PROBLEM_JSON)
      .body(error);
}
```

### 6. Duplicate Code (409)

**用途**: コード重複（UNIQUE 制約違反）

**type**: `https://api.example.com/errors/duplicate-code`

**例**:

```json
{
  "type": "https://api.example.com/errors/duplicate-code",
  "title": "Duplicate Code",
  "status": 409,
  "detail": "A message with the code 'MSG_001' already exists.",
  "instance": "/api/messages",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

**Java 実装**:

```java
@ExceptionHandler(DuplicateMessageCodeException.class)
public ResponseEntity<ErrorResponse> handleDuplicateCode(
    DuplicateMessageCodeException ex, HttpServletRequest request) {

  ErrorResponse error = ErrorResponse.builder()
      .type(ERROR_TYPE_BASE_URI + "/duplicate-code")
      .title("Duplicate Code")
      .status(HttpStatus.CONFLICT.value())
      .detail(ex.getMessage())
      .instance(request.getRequestURI())
      .timestamp(LocalDateTime.now())
      .build();

  return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .contentType(PROBLEM_JSON)
      .body(error);
}
```

### 7. Internal Server Error (500)

**用途**: 予期しないエラー

**type**: `https://api.example.com/errors/internal-error`

**例**:

```json
{
  "type": "https://api.example.com/errors/internal-error",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "An unexpected error occurred. Please try again later.",
  "instance": "/api/messages",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

**Java 実装**:

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGeneralException(
    Exception ex, HttpServletRequest request) {

  // ログに詳細を記録（ユーザーには見せない）
  log.error("Unexpected error occurred", ex);

  ErrorResponse error = ErrorResponse.builder()
      .type(ERROR_TYPE_BASE_URI + "/internal-error")
      .title("Internal Server Error")
      .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
      .detail("An unexpected error occurred. Please try again later.")
      .instance(request.getRequestURI())
      .timestamp(LocalDateTime.now())
      .build();

  return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .contentType(PROBLEM_JSON)
      .body(error);
}
```

## バリデーションエラー詳細

### ValidationError オブジェクト

```json
{
  "field": "code",
  "rejectedValue": "",
  "message": "コードは必須です"
}
```

| フィールド | 型 | 説明 |
|-----------|-----|------|
| `field` | string | エラーが発生したフィールド名 |
| `rejectedValue` | any | 拒否された値（null 可） |
| `message` | string | エラーメッセージ |

### バリデーションルール

#### 必須チェック

```java
@NotNull(message = "コードは必須です")
@NotEmpty(message = "コードは必須です")
private String code;
```

エラー:

```json
{
  "field": "code",
  "rejectedValue": null,
  "message": "コードは必須です"
}
```

#### 文字列長チェック

```java
@Size(min = 1, max = 50, message = "コードは1〜50文字である必要があります")
private String code;
```

エラー:

```json
{
  "field": "code",
  "rejectedValue": "VERY_LONG_CODE_THAT_EXCEEDS_FIFTY_CHARACTERS_LIMIT_...",
  "message": "コードは1〜50文字である必要があります"
}
```

#### パターンチェック

```java
@Pattern(regexp = "^[a-zA-Z0-9_-]+$",
         message = "コードは英数字、ハイフン、アンダースコアのみ使用可能です")
private String code;
```

エラー:

```json
{
  "field": "code",
  "rejectedValue": "MSG@001",
  "message": "コードは英数字、ハイフン、アンダースコアのみ使用可能です"
}
```

## エラーレスポンス実装

### ErrorResponse DTO

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

  private String type;
  private String title;
  private int status;
  private String detail;
  private String instance;
  private LocalDateTime timestamp;
  private List<ValidationError> errors;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ValidationError {
    private String field;
    private Object rejectedValue;
    private String message;
  }
}
```

### GlobalExceptionHandler

```java
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final String ERROR_TYPE_BASE_URI =
      "https://api.example.com/errors";
  private static final MediaType PROBLEM_JSON =
      MediaType.parseMediaType("application/problem+json");

  @ExceptionHandler(MessageNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotFound(
      MessageNotFoundException ex, HttpServletRequest request) {
    // 実装...
  }

  @ExceptionHandler(DuplicateMessageCodeException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateCode(
      DuplicateMessageCodeException ex, HttpServletRequest request) {
    // 実装...
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    // 実装...
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(
      Exception ex, HttpServletRequest request) {
    // 実装...
  }
}
```

## Frontend でのエラーハンドリング

### Axios インターセプター

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const problemDetail = error.response.data;

      // RFC 7807 形式のエラー処理
      if (problemDetail.type && problemDetail.status) {
        console.error('API Error:', {
          type: problemDetail.type,
          title: problemDetail.title,
          status: problemDetail.status,
          detail: problemDetail.detail,
          errors: problemDetail.errors,
        });

        // バリデーションエラーの場合
        if (problemDetail.errors) {
          // フィールドごとのエラーを表示
          problemDetail.errors.forEach((err: any) => {
            showFieldError(err.field, err.message);
          });
        } else {
          // 一般エラーメッセージを表示
          showErrorMessage(problemDetail.title, problemDetail.detail);
        }
      }
    }

    return Promise.reject(error);
  }
);
```

### React でのエラー表示

```typescript
const CreateMessageForm = () => {
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleSubmit = async (data: MessageRequest) => {
    try {
      await api.post('/messages', data);
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        const problemDetail = error.response.data;

        if (problemDetail.errors) {
          // フィールドエラーを state にセット
          const fieldErrors: Record<string, string> = {};
          problemDetail.errors.forEach((err: any) => {
            fieldErrors[err.field] = err.message;
          });
          setErrors(fieldErrors);
        }
      }
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input name="code" />
      {errors.code && <span className="error">{errors.code}</span>}

      <input name="content" />
      {errors.content && <span className="error">{errors.content}</span>}
    </form>
  );
};
```

## セキュリティ考慮事項

### 機密情報の漏洩防止

❌ **悪い例**: 内部実装詳細を公開

```json
{
  "detail": "SQLException: Duplicate entry 'MSG_001' for key 'messages.code'"
}
```

✅ **良い例**: 抽象化されたメッセージ

```json
{
  "detail": "A message with the code 'MSG_001' already exists."
}
```

### スタックトレースの非公開

本番環境では例外のスタックトレースをレスポンスに含めません。

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGeneralException(
    Exception ex, HttpServletRequest request) {

  // ログには詳細を記録
  log.error("Unexpected error occurred", ex);

  // ユーザーには抽象化されたメッセージのみ
  ErrorResponse error = ErrorResponse.builder()
      .type(ERROR_TYPE_BASE_URI + "/internal-error")
      .title("Internal Server Error")
      .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
      .detail("An unexpected error occurred. Please try again later.")
      .instance(request.getRequestURI())
      .timestamp(LocalDateTime.now())
      .build();

  return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .contentType(PROBLEM_JSON)
      .body(error);
}
```

## テスト

### OpenAPI Validation Test

```java
@Test
void createMessage_withInvalidRequest_shouldReturnProblemDetail() {
  String requestBody = """
      {
        "code": "",
        "content": "Test"
      }
      """;

  given()
      .filter(validationFilter)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .body(requestBody)
  .when()
      .post("/api/messages")
  .then()
      .statusCode(400)
      .contentType("application/problem+json")
      .body("type", containsString("/validation-error"))
      .body("status", equalTo(400))
      .body("errors", hasSize(greaterThan(0)));
}
```

### Integration Test

```java
@Test
void createMessage_withDuplicateCode_shouldReturn409() {
  // 既存メッセージを作成
  messageRepository.save(new Message("MSG_001", "Existing"));

  // 重複コードで作成試行
  MessageRequest request = new MessageRequest("MSG_001", "New");

  ResponseEntity<ErrorResponse> response =
      restTemplate.postForEntity("/api/messages", request, ErrorResponse.class);

  assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  assertThat(response.getBody().getType()).contains("/duplicate-code");
  assertThat(response.getBody().getStatus()).isEqualTo(409);
}
```

## 関連ドキュメント

- [API 設計ガイドライン](./README.md)
- [OpenAPI Specification](../../specs/openapi/openapi.yaml)
- [RFC 7807 Specification](https://www.rfc-editor.org/rfc/rfc7807.html)

---

**メンテナンス情報**:

- 最終更新: 2026-01-29
- 更新タイミング: 新しいエラータイプ追加時、エラーハンドリング戦略変更時
