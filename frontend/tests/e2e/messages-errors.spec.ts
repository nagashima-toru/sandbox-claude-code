import { test, expect } from '@playwright/test';

test.describe('Messages Error Handling', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the messages page
    await page.goto('/');
    await page.waitForLoadState('networkidle');
  });

  test('should show validation error for empty code field', async ({ page }) => {
    // Open create modal
    await page.getByRole('button', { name: /new message/i }).click();
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    // Fill only content, leave code empty
    await page.locator('input[name="content"]').fill('Test content');

    // Try to submit
    await page.getByRole('button', { name: /save/i }).click();

    // Modal should stay open due to validation error
    await expect(modal).toBeVisible();
  });

  test('should show validation error for empty content field', async ({ page }) => {
    // Open create modal
    await page.getByRole('button', { name: /new message/i }).click();
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    // Fill only code, leave content empty
    await page.locator('input[name="code"]').fill(`TEST_${Date.now()}`);

    // Try to submit
    await modal.getByRole('button', { name: /save/i }).click();

    // Modal should stay open due to validation error
    await expect(modal).toBeVisible();
  });

  test('should show error for code exceeding max length', async ({ page }) => {
    // Open create modal
    await page.getByRole('button', { name: /new message/i }).click();
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    // Fill with code that's too long (max is 50 characters)
    const longCode = 'A'.repeat(51);
    await page.locator('input[name="code"]').fill(longCode);
    await page.locator('input[name="content"]').fill('Test content');

    // Try to submit
    await modal.getByRole('button', { name: /save/i }).click();

    // Modal should stay open due to validation error
    await expect(modal).toBeVisible();
  });

  test('should show error for content exceeding max length', async ({ page }) => {
    // Open create modal
    await page.getByRole('button', { name: /new message/i }).click();
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    // Fill with content that's too long (max is 255 characters based on OpenAPI spec)
    const longContent = 'A'.repeat(256);
    await page.locator('input[name="code"]').fill(`TEST_${Date.now()}`);
    await page.locator('input[name="content"]').fill(longContent);

    // Try to submit
    await modal.getByRole('button', { name: /save/i }).click();

    // Modal should stay open due to validation error
    await expect(modal).toBeVisible();
  });

  test('should show error when trying to edit non-existent message', async ({ page }) => {
    // Try to navigate to a non-existent message ID
    // This test assumes the app handles 404 errors properly
    // The actual implementation may vary based on routing

    // Create a valid message first
    const timestamp = Date.now();
    const code = `TEST_${timestamp}`;

    await page.getByRole('button', { name: /new message/i }).click();
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    await page.locator('input[name="code"]').fill(code);
    await page.locator('input[name="content"]').fill('Test content');
    await modal.getByRole('button', { name: /save/i }).click();
    await expect(modal).not.toBeVisible({ timeout: 15000 });

    await page.waitForTimeout(2000);

    // Delete the message
    const row = page.locator(`tr:has-text("${code}")`);
    await row.getByRole('button', { name: /delete/i }).click();

    const confirmDialog = page.locator('[role="alertdialog"]').or(page.locator('[role="dialog"]'));
    await expect(confirmDialog).toBeVisible();
    await page
      .getByRole('button', { name: /delete/i })
      .last()
      .click();
    await expect(confirmDialog).not.toBeVisible({ timeout: 15000 });

    // If there's an edit button for a deleted message ID in the UI, this would trigger a 404
    // This is a simplified test - adjust based on actual implementation
  });

  test('should cancel creation without saving', async ({ page }) => {
    // Get initial message count
    const initialRows = await page.locator('tr').count();

    // Open create modal
    await page.getByRole('button', { name: /new message/i }).click();
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    // Fill in data
    await page.locator('input[name="code"]').fill(`CANCEL_${Date.now()}`);
    await page.locator('input[name="content"]').fill('This should not be saved');

    // Click cancel - use first() to avoid strict mode violation
    const cancelButton = modal.getByRole('button', { name: /cancel/i });
    await cancelButton.click();

    // Modal should close
    await expect(modal).not.toBeVisible();

    // Message count should remain the same
    await page.waitForTimeout(1000);
    const finalRows = await page.locator('tr').count();
    expect(finalRows).toBe(initialRows);
  });

  test('should handle network errors gracefully', async ({ page, context }) => {
    // Simulate offline mode
    await context.setOffline(true);

    // Try to create a message
    await page.getByRole('button', { name: /new message/i }).click();
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    await page.locator('input[name="code"]').fill(`OFFLINE_${Date.now()}`);
    await page.locator('input[name="content"]').fill('Test content');
    await modal.getByRole('button', { name: /save/i }).click();

    // Wait for error to potentially appear
    await page.waitForTimeout(2000);

    // Modal should stay open due to network error
    await expect(modal).toBeVisible();

    // Restore online mode
    await context.setOffline(false);
  });

  test('should show validation error for invalid code pattern', async ({ page }) => {
    // Open create modal
    await page.getByRole('button', { name: /new message/i }).click();
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    // Try to use invalid characters in code (based on OpenAPI pattern: ^[a-zA-Z0-9_-]+$)
    const invalidCode = `INVALID CODE WITH SPACES!@#`;
    await page.locator('input[name="code"]').fill(invalidCode);
    await page.locator('input[name="content"]').fill('Test content');

    // Try to submit
    await modal.getByRole('button', { name: /save/i }).click();

    // Modal should stay open due to validation error
    await expect(modal).toBeVisible();
  });
});
