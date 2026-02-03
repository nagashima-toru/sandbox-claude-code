package com.sandbox.api.domain.model;

/**
 * Represents user roles in the system.
 *
 * <p>This enum defines the available roles for authorization:
 *
 * <ul>
 *   <li>ADMIN - Full access to all operations
 *   <li>VIEWER - Read-only access
 * </ul>
 */
public enum Role {
  /** Administrator role with full access to all operations (CRUD). */
  ADMIN,

  /** Viewer role with read-only access (GET operations only). */
  VIEWER
}
