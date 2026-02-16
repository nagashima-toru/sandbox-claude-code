import { test, expect } from '@playwright/test';
import { login, waitForFrontend, createMessage } from './helpers';

/**
 * E2E tests for permission errors
 *
 * Tests the following scenarios:
 * - VIEWER receives 403 error when attempting to create a message
 * - VIEWER receives 403 error when attempting to update a message
 * - VIEWER receives 403 error when attempting to delete a message
 * - Error responses follow RFC 7807 format
 */
test.describe('Permission UI - Error Handling', () => {
  let authToken: string;

  test.beforeEach(async ({ page }) => {
    // Login as VIEWER user and capture the auth token
    await login(page, 'viewer', 'viewer123');
    await waitForFrontend(page);

    // Get the auth token from localStorage
    const token = await page.evaluate(() => {
      return localStorage.getItem('access_token');
    });

    if (!token) {
      throw new Error('Failed to get auth token from localStorage');
    }

    authToken = token;
  });

  test('should return 403 when VIEWER attempts to create a message', async ({ request }) => {
    const timestamp = Date.now();
    const testMessage = {
      code: `VIEWER_CREATE_${timestamp}`,
      content: `Should fail ${timestamp}`,
    };

    // Make a direct API call with VIEWER token
    const response = await request.post('http://127.0.0.1:8080/api/messages', {
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json',
      },
      data: testMessage,
    });

    // Verify 403 Forbidden response
    expect(response.status()).toBe(403);

    // Verify RFC 7807 error format
    const errorBody = await response.json();
    expect(errorBody).toHaveProperty('type');
    expect(errorBody).toHaveProperty('title');
    expect(errorBody).toHaveProperty('status', 403);
    expect(errorBody).toHaveProperty('detail');

    // Verify error message indicates permission issue
    expect(errorBody.title).toContain('Forbidden');
  });

  test('should return 403 when VIEWER attempts to update a message', async ({ page, request }) => {
    // First, create a test message as ADMIN
    const adminPage = await page.context().newPage();
    await login(adminPage, 'admin', 'admin123');
    await waitForFrontend(adminPage);

    const timestamp = Date.now();
    const testCode = `VIEWER_UPDATE_${timestamp}`;
    const testContent = `Original content ${timestamp}`;
    await createMessage(adminPage, testCode, testContent);

    // Search for the message to get its ID
    const searchInput = adminPage.getByTestId('search-input');
    await searchInput.fill(testCode);
    await adminPage.waitForTimeout(600);

    // Get the message ID from the row
    const row = adminPage.locator(`[data-testid^="message-row-"]:has-text("${testCode}")`).first();
    await expect(row).toBeVisible({ timeout: 10000 });
    const rowTestId = await row.getAttribute('data-testid');
    const messageId = rowTestId?.replace('message-row-', '') || '';

    await adminPage.close();

    // Now, as VIEWER, attempt to update the message via API
    const updateData = {
      code: testCode,
      content: 'Updated by VIEWER - should fail',
    };

    const response = await request.put(`http://127.0.0.1:8080/api/messages/${messageId}`, {
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json',
      },
      data: updateData,
    });

    // Verify 403 Forbidden response
    expect(response.status()).toBe(403);

    // Verify RFC 7807 error format
    const errorBody = await response.json();
    expect(errorBody).toHaveProperty('type');
    expect(errorBody).toHaveProperty('title');
    expect(errorBody).toHaveProperty('status', 403);
    expect(errorBody).toHaveProperty('detail');
    expect(errorBody.title).toContain('Forbidden');
  });

  test('should return 403 when VIEWER attempts to delete a message', async ({ page, request }) => {
    // First, create a test message as ADMIN
    const adminPage = await page.context().newPage();
    await login(adminPage, 'admin', 'admin123');
    await waitForFrontend(adminPage);

    const timestamp = Date.now();
    const testCode = `VIEWER_DELETE_${timestamp}`;
    const testContent = `Delete test ${timestamp}`;
    await createMessage(adminPage, testCode, testContent);

    // Search for the message to get its ID
    const searchInput = adminPage.getByTestId('search-input');
    await searchInput.fill(testCode);
    await adminPage.waitForTimeout(600);

    // Get the message ID from the row
    const row = adminPage.locator(`[data-testid^="message-row-"]:has-text("${testCode}")`).first();
    await expect(row).toBeVisible({ timeout: 10000 });
    const rowTestId = await row.getAttribute('data-testid');
    const messageId = rowTestId?.replace('message-row-', '') || '';

    await adminPage.close();

    // Now, as VIEWER, attempt to delete the message via API
    const response = await request.delete(`http://127.0.0.1:8080/api/messages/${messageId}`, {
      headers: {
        Authorization: `Bearer ${authToken}`,
      },
    });

    // Verify 403 Forbidden response
    expect(response.status()).toBe(403);

    // Verify RFC 7807 error format
    const errorBody = await response.json();
    expect(errorBody).toHaveProperty('type');
    expect(errorBody).toHaveProperty('title');
    expect(errorBody).toHaveProperty('status', 403);
    expect(errorBody).toHaveProperty('detail');
    expect(errorBody.title).toContain('Forbidden');
  });

  test('should verify RFC 7807 error format structure', async ({ request }) => {
    // Use create endpoint as an example (any forbidden action works)
    const response = await request.post('http://127.0.0.1:8080/api/messages', {
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json',
      },
      data: {
        code: 'TEST',
        content: 'Test',
      },
    });

    expect(response.status()).toBe(403);

    const errorBody = await response.json();

    // RFC 7807 required fields
    expect(errorBody).toHaveProperty('type');
    expect(errorBody).toHaveProperty('title');
    expect(errorBody).toHaveProperty('status');

    // Verify type is a URI (or URI reference)
    expect(typeof errorBody.type).toBe('string');
    expect(errorBody.type.length).toBeGreaterThan(0);

    // Verify title is a string
    expect(typeof errorBody.title).toBe('string');
    expect(errorBody.title.length).toBeGreaterThan(0);

    // Verify status matches HTTP status code
    expect(errorBody.status).toBe(403);

    // Optional fields that may be present
    if (errorBody.detail) {
      expect(typeof errorBody.detail).toBe('string');
    }

    if (errorBody.instance) {
      expect(typeof errorBody.instance).toBe('string');
    }
  });
});
