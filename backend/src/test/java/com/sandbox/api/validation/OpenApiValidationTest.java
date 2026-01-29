package com.sandbox.api.validation;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import com.atlassian.oai.validator.whitelist.ValidationErrorsWhitelist;
import com.sandbox.api.ApiApplication;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * OpenAPI仕様との整合性を検証するテスト
 *
 * <p>swagger-request-validatorを使用して、実装がOpenAPI仕様に準拠していることを確認します。
 */
@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiValidationTest {

  @Autowired private MockMvc mockMvc;

  private OpenApiValidationFilter validationFilter;

  @BeforeEach
  void setUp() {
    // OpenAPI仕様ファイルのパスを指定
    String specPath =
        Paths.get("../specs/openapi/openapi.yaml").toAbsolutePath().normalize().toString();

    // バリデーターを初期化
    OpenApiInteractionValidator validator =
        OpenApiInteractionValidator.createForSpecificationUrl(specPath)
            .withWhitelist(
                ValidationErrorsWhitelist.create()
                    // 必要に応じてホワイトリストを追加
                    // .withRule("Ignore...", ValidationErrorsWhitelist.Rule....)
                    )
            .build();

    validationFilter = new OpenApiValidationFilter(validator);

    // RestAssured MockMvcのセットアップ
    RestAssuredMockMvc.mockMvc(mockMvc);
  }

  @Test
  @DisplayName("GET /api/messages - OpenAPI仕様に準拠している")
  void getAllMessages_shouldConformToOpenApiSpec() {
    given()
        .filter(validationFilter)
        .when()
        .get("/api/messages")
        .then()
        .statusCode(200)
        .contentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  @DisplayName("POST /api/messages - 有効なリクエストでOpenAPI仕様に準拠している")
  void createMessage_withValidRequest_shouldConformToOpenApiSpec() {
    String requestBody = """
            {
              "code": "MSG_TEST",
              "content": "Test message for OpenAPI validation"
            }
            """;

    given()
        .filter(validationFilter)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .post("/api/messages")
        .then()
        .statusCode(201)
        .contentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  @DisplayName("POST /api/messages - 無効なリクエストでバリデーションエラー")
  void createMessage_withInvalidRequest_shouldReturnValidationError() {
    String requestBody = """
            {
              "code": "",
              "content": "Test message"
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
        .contentType("application/problem+json");
  }

  @Test
  @DisplayName("GET /api/messages/{id} - 存在するIDでOpenAPI仕様に準拠している")
  void getMessageById_withExistingId_shouldConformToOpenApiSpec() {
    // まずメッセージを作成
    String requestBody = """
            {
              "code": "MSG_FOR_GET",
              "content": "Message for GET test"
            }
            """;

    String location =
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(requestBody)
            .when()
            .post("/api/messages")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");

    // 作成したメッセージを取得
    given()
        .filter(validationFilter)
        .when()
        .get(location)
        .then()
        .statusCode(200)
        .contentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  @DisplayName("GET /api/messages/{id} - 存在しないIDで404エラー")
  void getMessageById_withNonExistingId_shouldReturn404() {
    given()
        .filter(validationFilter)
        .when()
        .get("/api/messages/99999")
        .then()
        .statusCode(404)
        .contentType("application/problem+json");
  }

  @Test
  @DisplayName("DELETE /api/messages/{id} - 存在するIDでOpenAPI仕様に準拠している")
  void deleteMessage_withExistingId_shouldConformToOpenApiSpec() {
    // まずメッセージを作成
    String requestBody = """
            {
              "code": "MSG_FOR_DELETE",
              "content": "Message for DELETE test"
            }
            """;

    String location =
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(requestBody)
            .when()
            .post("/api/messages")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");

    // 作成したメッセージを削除
    given().filter(validationFilter).when().delete(location).then().statusCode(204);
  }
}
