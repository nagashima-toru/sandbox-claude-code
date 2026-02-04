package com.sandbox.api.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RefreshTokenStoreTest {

  private RefreshTokenStore store;

  @BeforeEach
  void setUp() {
    store = new RefreshTokenStore();
  }

  @Test
  void store_addsTokenToStore() {
    store.store("token123", 1L);

    assertThat(store.isValid("token123")).isTrue();
    assertThat(store.getUserId("token123")).isEqualTo(1L);
  }

  @Test
  void isValid_returnsFalseForNonexistentToken() {
    assertThat(store.isValid("nonexistent")).isFalse();
  }

  @Test
  void getUserId_returnsNullForNonexistentToken() {
    assertThat(store.getUserId("nonexistent")).isNull();
  }

  @Test
  void remove_removesToken() {
    store.store("token123", 1L);

    store.remove("token123");

    assertThat(store.isValid("token123")).isFalse();
    assertThat(store.getUserId("token123")).isNull();
  }

  @Test
  void remove_doesNothingForNonexistentToken() {
    // Should not throw exception
    store.remove("nonexistent");
  }

  @Test
  void removeAllForUser_removesAllTokensForUser() {
    store.store("token1", 1L);
    store.store("token2", 1L);
    store.store("token3", 2L);

    store.removeAllForUser(1L);

    assertThat(store.isValid("token1")).isFalse();
    assertThat(store.isValid("token2")).isFalse();
    assertThat(store.isValid("token3")).isTrue();
  }

  @Test
  void store_overwritesExistingToken() {
    store.store("token123", 1L);
    store.store("token123", 2L);

    assertThat(store.getUserId("token123")).isEqualTo(2L);
  }
}
