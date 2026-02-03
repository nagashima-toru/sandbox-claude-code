package com.sandbox.api.validation;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

import com.sandbox.api.ApiApplication;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * OpenAPI仕様との整合性を検証するテスト
 *
 * <p>RestAssuredを使用して、実装がOpenAPI仕様に準拠していることを確認します。
 */
@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@WithMockUser(username = "admin", roles = "ADMIN")
class OpenApiValidationTest {
  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    RestAssuredMockMvc.mockMvc(mockMvc);
  }

  @Test
  @DisplayName("GET /api/messages - レスポンスが正しい形式である")
  void getAllMessages_shouldReturnValidResponse() {
    given()
        .when()
        .get("/api/messages")
        .then()
        .statusCode(200)
        .contentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  @DisplayName("POST /api/messages - 有効なリクエストで201を返す")
  void createMessage_withValidRequest_shouldReturn201() {
    String uniqueCode = "MSG_TEST_" + System.currentTimeMillis();
    String requestBody =
        """
        {
          "code": "%s",
          "content": "Test message for OpenAPI validation"
        }
        """
            .formatted(uniqueCode);
    given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .post("/api/messages")
        .then()
        .statusCode(201)
        .contentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  @DisplayName("POST /api/messages - 無効なリクエストで400を返す")
  void createMessage_withInvalidRequest_shouldReturn400() {
    String requestBody =
        """
        {
          "code": "",
          "content": "Test message"
        }
        """;
    given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .post("/api/messages")
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("GET /api/messages/{id} - 存在するIDで200を返す")
  void getMessageById_withExistingId_shouldReturn200() {
    // まずメッセージを作成
    String uniqueCode = "MSG_FOR_GET_" + System.currentTimeMillis();
    String requestBody =
        """
        {
          "code": "%s",
          "content": "Message for GET test"
        }
        """
            .formatted(uniqueCode);
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
        .when()
        .get(location)
        .then()
        .statusCode(200)
        .contentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  @DisplayName("GET /api/messages/{id} - 存在しないIDで404を返す")
  void getMessageById_withNonExistingId_shouldReturn404() {
    given().when().get("/api/messages/99999").then().statusCode(404);
  }

  @Test
  @DisplayName("DELETE /api/messages/{id} - 存在するIDで204を返す")
  void deleteMessage_withExistingId_shouldReturn204() {
    String uniqueCode = "MSG_FOR_DELETE_" + System.currentTimeMillis();
    String requestBody =
        """
        {
          "code": "%s",
          "content": "Message for DELETE test"
        }
        """
            .formatted(uniqueCode);
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
    given().when().delete(location).then().statusCode(204);
  }
}
