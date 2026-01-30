package com.sandbox.api.domain.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
class LogSanitizerTest {
  @Test
  void testSanitizeWithNull() {
    String result = LogSanitizer.sanitize(null);
    assertEquals("null", result);
  }
  void testSanitizeWithNormalString() {
    String input = "normal string";
    String result = LogSanitizer.sanitize(input);
    assertEquals("normal string", result);
  void testSanitizeWithNewline() {
    String input = "line1\nline2";
    assertEquals("line1_line2", result);
  void testSanitizeWithCarriageReturn() {
    String input = "line1\rline2";
  void testSanitizeWithTab() {
    String input = "word1\tword2";
    assertEquals("word1 word2", result);
  void testSanitizeWithMultipleSpecialCharacters() {
    String input = "line1\nline2\rline3\tword";
    assertEquals("line1_line2_line3 word", result);
  void testSanitizeWithLogInjectionAttempt() {
    // Simulating a log injection attack attempt
    String input = "admin\n[INFO] Fake log entry";
    assertEquals("admin_[INFO] Fake log entry", result);
    // The sanitized version prevents creating a new log line
}
