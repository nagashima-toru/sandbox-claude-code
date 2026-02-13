package com.sandbox.api.application.dto;

/**
 * User response DTO.
 *
 * @param username username
 * @param role user role (e.g., "ADMIN", "VIEWER")
 */
public record UserResponse(String username, String role) {}
