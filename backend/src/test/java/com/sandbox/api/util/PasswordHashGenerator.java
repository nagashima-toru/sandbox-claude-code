package com.sandbox.api.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility test to generate BCrypt password hashes for migration scripts. Run this test and copy the
 * output hashes to your migration files.
 */
public class PasswordHashGenerator {

  @Test
  void generatePasswordHashes() {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    System.out.println("=== Generated Password Hashes ===");
    System.out.println("admin123: " + encoder.encode("admin123"));
    System.out.println("viewer123: " + encoder.encode("viewer123"));
    System.out.println("=================================");
  }
}
