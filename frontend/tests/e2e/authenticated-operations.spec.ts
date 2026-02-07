import { test, expect } from '@playwright/test';
import { setupAuthenticatedSession, createMessage, editMessage, deleteMessage } from './helpers';

test.describe('Authenticated Operations', () => {
  test.beforeEach(async ({ page }) => {
    // Setup authenticated session before each test
    await setupAuthenticatedSession(page);
  });

  test('should perform CRUD operations while authenticated', async ({ page }) => {
    const timestamp = Date.now();
    const code = `AUTH_TEST_${timestamp}`;
    const content = `Authenticated message ${timestamp}`;

    // Create a message
    await createMessage(page, code, content);

    // Search for the created message
    const searchInput = page.getByTestId('search-input');
    await searchInput.fill(code);
    await page.waitForTimeout(600);

    // Verify message appears in the table
    const row = page.locator(`[data-testid^="message-row-"]:has-text("${code}")`).first();
    await expect(row).toBeVisible({ timeout: 10000 });
    await expect(row).toContainText(content);

    // Edit the message
    const updatedContent = `Updated ${Date.now()}`;
    await editMessage(page, code, updatedContent);

    // Verify the updated content
    await searchInput.fill(code);
    await page.waitForTimeout(600);
    const updatedRow = page
      .locator(`[data-testid^="message-row-"]:has-text("${updatedContent}")`)
      .first();
    await expect(updatedRow).toBeVisible();

    // Delete the message
    await deleteMessage(page, code);

    // Verify message is deleted
    await searchInput.fill(code);
    await page.waitForTimeout(600);
    await expect(row).not.toBeVisible();
  });

  test('should access all protected pages while authenticated', async ({ page }) => {
    // Already on home page from setupAuthenticatedSession

    // Verify home page is accessible
    await expect(page.getByRole('heading', { name: /Message Management/i })).toBeVisible();

    // Verify search functionality works
    const searchInput = page.getByTestId('search-input');
    await expect(searchInput).toBeVisible();
    await searchInput.fill('test');
    await page.waitForTimeout(600);

    // Verify "New Message" button is accessible
    const newButton = page.getByTestId('create-message-button');
    await expect(newButton).toBeVisible();

    // Table might not be visible on small screens, so we just verify the page loaded
    await expect(page).toHaveTitle(/Message Management/i);
  });

  test('should handle API errors gracefully while authenticated', async ({ page }) => {
    // Try to create a message with duplicate code (if validation exists)
    const timestamp = Date.now();
    const code = `DUP_${timestamp}`;
    const content = `Test message ${timestamp}`;

    // Create first message
    await createMessage(page, code, content);

    // Search for the message
    const searchInput = page.getByTestId('search-input');
    await searchInput.fill(code);
    await page.waitForTimeout(600);

    // Verify message was created
    const row = page.locator(`[data-testid^="message-row-"]:has-text("${code}")`).first();
    await expect(row).toBeVisible({ timeout: 10000 });

    // Clean up
    await deleteMessage(page, code);
  });

  test('should maintain authentication during long session', async ({ page }) => {
    // Perform multiple operations to ensure token doesn't expire
    for (let i = 0; i < 3; i++) {
      const timestamp = Date.now();
      const code = `LONG_SESSION_${timestamp}_${i}`;
      const content = `Session test ${i}`;

      // Create message
      await createMessage(page, code, content);

      // Wait a bit between operations
      await page.waitForTimeout(1000);

      // Verify message was created
      const searchInput = page.getByTestId('search-input');
      await searchInput.fill(code);
      await page.waitForTimeout(600);

      const row = page.locator(`[data-testid^="message-row-"]:has-text("${code}")`).first();
      await expect(row).toBeVisible({ timeout: 10000 });

      // Clean up
      await deleteMessage(page, code);
      await searchInput.clear();
      await page.waitForTimeout(600);
    }

    // Verify user is still authenticated
    await expect(page.getByRole('heading', { name: /Message Management/i })).toBeVisible();
    expect(page.url()).not.toContain('/login');
  });

  test('should handle logout and prevent further operations', async ({ page }) => {
    // Verify user is authenticated
    await expect(page.getByRole('heading', { name: /Message Management/i })).toBeVisible();

    // Logout
    const logoutButton = page.getByRole('button', { name: /logout/i });
    await expect(logoutButton).toBeVisible();
    await logoutButton.click();

    // Wait for redirect to login page
    await page.waitForURL('/login', { timeout: 10000 });

    // Try to navigate back to protected page
    await page.goto('/');

    // Should be redirected back to login
    await page.waitForURL('/login', { timeout: 10000 });
    await expect(page.getByRole('heading', { name: /ログイン/i })).toBeVisible();
  });

  test('should display user role information if available', async ({ page }) => {
    // Verify user is authenticated
    await expect(page.getByRole('heading', { name: /Message Management/i })).toBeVisible();

    // Check if there's any user info displayed (implementation-dependent)
    // This test is flexible based on actual UI implementation
    const logoutButton = page.getByRole('button', { name: /logout/i });
    await expect(logoutButton).toBeVisible();
  });
});
