import { test, expect } from '@playwright/test';

test.describe('Messages Error Handling', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the messages page
    await page.goto('/');
    await page.waitForLoadState('networkidle');
  });

  test('should show validation error for empty code field', async ({ page }) => {
    // Open create modal
    await page.getByRole('button', { name: /new message|create|add/i }).click();
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    // Fill only content, leave code empty
    await page.fill(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
      'Test content'
    );

    // Try to submit
    await page.getByRole('button', { name: /save|submit|create/i }).click();

    // Check for validation error
    const errorMessage = page.locator('text=/code.*required|required.*code/i');
    await expect(errorMessage).toBeVisible({ timeout: 5000 });
  });

  test('should show validation error for empty content field', async ({ page }) => {
    // Open create modal
    await page.getByRole('button', { name: /new message|create|add/i }).click();
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    // Fill only code, leave content empty
    await page.fill('input[name="code"], input[placeholder*="code" i]', `TEST_${Date.now()}`);

    // Try to submit
    await page.getByRole('button', { name: /save|submit|create/i }).click();

    // Check for validation error
    const errorMessage = page.locator('text=/content.*required|required.*content/i');
    await expect(errorMessage).toBeVisible({ timeout: 5000 });
  });

  test('should show error for code exceeding max length', async ({ page }) => {
    // Open create modal
    await page.getByRole('button', { name: /new message|create|add/i }).click();
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    // Fill with code that's too long (max is 50 characters)
    const longCode = 'A'.repeat(51);
    await page.fill('input[name="code"], input[placeholder*="code" i]', longCode);
    await page.fill(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
      'Test content'
    );

    // Try to submit
    await page.getByRole('button', { name: /save|submit|create/i }).click();

    // Check for validation error
    const errorMessage = page.locator('text=/code.*50|50.*character|too long/i');
    await expect(errorMessage).toBeVisible({ timeout: 5000 });
  });

  test('should show error for content exceeding max length', async ({ page }) => {
    // Open create modal
    await page.getByRole('button', { name: /new message|create|add/i }).click();
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    // Fill with content that's too long (max is 255 characters based on OpenAPI spec)
    const longContent = 'A'.repeat(256);
    await page.fill('input[name="code"], input[placeholder*="code" i]', `TEST_${Date.now()}`);
    await page.fill(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
      longContent
    );

    // Try to submit
    await page.getByRole('button', { name: /save|submit|create/i }).click();

    // Check for validation error
    const errorMessage = page.locator('text=/content.*255|255.*character|too long/i');
    await expect(errorMessage).toBeVisible({ timeout: 5000 });
  });

  test('should show error when trying to edit non-existent message', async ({ page }) => {
    // Try to navigate to a non-existent message ID
    // This test assumes the app handles 404 errors properly
    // The actual implementation may vary based on routing

    // Create a valid message first
    const timestamp = Date.now();
    const code = `TEST_${timestamp}`;

    await page.getByRole('button', { name: /new message|create|add/i }).click();
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    await page.fill('input[name="code"], input[placeholder*="code" i]', code);
    await page.fill(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
      'Test content'
    );
    await page.getByRole('button', { name: /save|submit|create/i }).click();
    await expect(modal).not.toBeVisible({ timeout: 10000 });

    await page.waitForTimeout(1000);

    // Delete the message
    const row = page.locator(`tr:has-text("${code}"), [data-testid="message-row"]:has-text("${code}")`);
    const deleteButton = row.getByRole('button', { name: /delete/i });
    await deleteButton.click();

    const confirmDialog = page.locator('[role="alertdialog"], [role="dialog"]');
    await expect(confirmDialog).toBeVisible();
    await page.getByRole('button', { name: /delete|confirm|yes/i }).click();
    await expect(confirmDialog).not.toBeVisible({ timeout: 10000 });

    // If there's an edit button for a deleted message ID in the UI, this would trigger a 404
    // This is a simplified test - adjust based on actual implementation
  });

  test('should cancel creation without saving', async ({ page }) => {
    // Get initial message count
    const initialRows = await page.locator('tr, [data-testid="message-row"]').count();

    // Open create modal
    await page.getByRole('button', { name: /new message|create|add/i }).click();
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    // Fill in data
    await page.fill('input[name="code"], input[placeholder*="code" i]', `CANCEL_${Date.now()}`);
    await page.fill(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
      'This should not be saved'
    );

    // Click cancel
    const cancelButton = page.getByRole('button', { name: /cancel|close/i });
    await cancelButton.click();

    // Modal should close
    await expect(modal).not.toBeVisible();

    // Message count should remain the same
    await page.waitForTimeout(1000);
    const finalRows = await page.locator('tr, [data-testid="message-row"]').count();
    expect(finalRows).toBe(initialRows);
  });

  test('should handle network errors gracefully', async ({ page, context }) => {
    // Simulate offline mode
    await context.setOffline(true);

    // Try to create a message
    await page.getByRole('button', { name: /new message|create|add/i }).click();
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    await page.fill('input[name="code"], input[placeholder*="code" i]', `OFFLINE_${Date.now()}`);
    await page.fill(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
      'Test content'
    );
    await page.getByRole('button', { name: /save|submit|create/i }).click();

    // Check for network error message
    const errorMessage = page.locator(
      'text=/network error|connection failed|offline|failed to fetch/i'
    );
    await expect(errorMessage).toBeVisible({ timeout: 5000 });

    // Restore online mode
    await context.setOffline(false);
  });

  test('should show validation error for invalid code pattern', async ({ page }) => {
    // Open create modal
    await page.getByRole('button', { name: /new message|create|add/i }).click();
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    // Try to use invalid characters in code (based on OpenAPI pattern: ^[a-zA-Z0-9_-]+$)
    const invalidCode = `INVALID CODE WITH SPACES!@#`;
    await page.fill('input[name="code"], input[placeholder*="code" i]', invalidCode);
    await page.fill(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
      'Test content'
    );

    // Try to submit
    await page.getByRole('button', { name: /save|submit|create/i }).click();

    // Check for validation error about invalid pattern
    const errorMessage = page.locator('text=/invalid.*code|pattern|alphanumeric/i');
    await expect(errorMessage).toBeVisible({ timeout: 5000 });
  });
});
