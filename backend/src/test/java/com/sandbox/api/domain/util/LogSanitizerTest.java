package com.sandbox.api.domain.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class LogSanitizerTest {
  @Test
  void testSanitizeWithNull() {
    String result = LogSanitizer.sanitize(null);
    assertEquals("null", result);
  }

  @Test
  void testSanitizeWithNormalString() {
    String input = "normal string";
    String result = LogSanitizer.sanitize(input);
    assertEquals("normal string", result);
  }

  @Test
  void testSanitizeWithNewline() {
    String input = "line1\nline2";
    String result = LogSanitizer.sanitize(input);
    assertEquals("line1_line2", result);
  }

  @Test
  void testSanitizeWithCarriageReturn() {
    String input = "line1\rline2";
    String result = LogSanitizer.sanitize(input);
    assertEquals("line1_line2", result);
  }

  @Test
  void testSanitizeWithTab() {
    String input = "word1\tword2";
    String result = LogSanitizer.sanitize(input);
    assertEquals("word1 word2", result);
  }

  @Test
  void testSanitizeWithMultipleSpecialCharacters() {
    String input = "line1\nline2\rline3\tword";
    String result = LogSanitizer.sanitize(input);
    assertEquals("line1_line2_line3 word", result);
  }

  @Test
  void testSanitizeWithLogInjectionAttempt() {
    // Simulating a log injection attack attempt
    String input = "admin\n[INFO] Fake log entry";
    String result = LogSanitizer.sanitize(input);
    assertEquals("admin_[INFO] Fake log entry", result);
    // The sanitized version prevents creating a new log line
  }
}
