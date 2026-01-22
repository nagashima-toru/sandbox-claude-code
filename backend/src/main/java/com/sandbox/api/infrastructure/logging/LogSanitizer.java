package com.sandbox.api.infrastructure.logging;

/**
 * Utility class for sanitizing user input before logging to prevent log injection attacks.
 *
 * <p>Log injection occurs when user-controlled data containing newline characters or other special
 * characters is written to logs, allowing attackers to forge log entries.
 */
public final class LogSanitizer {

  private LogSanitizer() {
    // Utility class - prevent instantiation
  }

  /**
   * Sanitizes a string for safe logging by removing or replacing characters that could be used for
   * log injection attacks.
   *
   * @param input the input string to sanitize (may be null)
   * @return the sanitized string safe for logging, or "null" if input is null
   */
  public static String sanitize(String input) {
    if (input == null) {
      return "null";
    }

    // Replace newline characters and carriage returns with spaces
    // This prevents log injection by ensuring user input cannot create new log lines
    return input.replace('\n', '_').replace('\r', '_').replace('\t', ' ');
  }
}
