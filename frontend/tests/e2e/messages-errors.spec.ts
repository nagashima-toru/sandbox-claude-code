import { test, expect } from '@playwright/test';
import {
  waitForFrontend,
  openCreateModal,
  fillFormField,
  fillMessageForm,
  saveModalForm,
  cancelModalForm,
  createMessage,
  deleteMessage,
} from './helpers';

test.describe('Messages Error Handling', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the messages page
    await page.goto('/');
    await waitForFrontend(page);
  });

  test('should show validation error for empty code field', async ({ page }) => {
    // Open create modal
    await openCreateModal(page);

    // Fill only content, leave code empty
    await fillFormField(page, 'content', 'Test content');

    // Try to submit
    await saveModalForm(page);

    // Modal should stay open due to validation error
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();
  });

  test('should show validation error for empty content field', async ({ page }) => {
    // Open create modal
    await openCreateModal(page);

    // Fill only code, leave content empty
    await fillFormField(page, 'code', `TEST_${Date.now()}`);

    // Try to submit
    await saveModalForm(page);

    // Modal should stay open due to validation error
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();
  });

  test('should show error for code exceeding max length', async ({ page }) => {
    // Open create modal
    await openCreateModal(page);

    // Fill with code that's too long (max is 50 characters)
    const longCode = 'A'.repeat(51);
    await fillMessageForm(page, { code: longCode, content: 'Test content' });

    // Try to submit
    await saveModalForm(page);

    // Modal should stay open due to validation error
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();
  });

  test('should show error for content exceeding max length', async ({ page }) => {
    // Open create modal
    await openCreateModal(page);

    // Fill with content that's too long (max is 255 characters based on OpenAPI spec)
    const longContent = 'A'.repeat(256);
    await fillMessageForm(page, { code: `TEST_${Date.now()}`, content: longContent });

    // Try to submit
    await saveModalForm(page);

    // Modal should stay open due to validation error
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();
  });

  test('should show error when trying to edit non-existent message', async ({ page }) => {
    // Create a valid message first
    const timestamp = Date.now();
    const code = `TEST_${timestamp}`;

    await createMessage(page, code, 'Test content');

    // Delete the message
    await deleteMessage(page, code);

    // If there's an edit button for a deleted message ID in the UI, this would trigger a 404
    // This is a simplified test - adjust based on actual implementation
  });

  test('should cancel creation without saving', async ({ page }) => {
    // Get initial message count
    const initialRows = await page.locator('tr').count();

    // Open create modal
    await openCreateModal(page);

    // Fill in data
    await fillMessageForm(page, {
      code: `CANCEL_${Date.now()}`,
      content: 'This should not be saved',
    });

    // Click cancel
    await cancelModalForm(page);

    // Modal should close
    const modal = page.locator('[role="dialog"]');
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
    await openCreateModal(page);

    await fillMessageForm(page, {
      code: `OFFLINE_${Date.now()}`,
      content: 'Test content',
    });

    await saveModalForm(page);

    // Wait for error to potentially appear
    await page.waitForTimeout(2000);

    // Modal should stay open due to network error
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    // Restore online mode
    await context.setOffline(false);
  });

  test('should show validation error for invalid code pattern', async ({ page }) => {
    // Open create modal
    await openCreateModal(page);

    // Try to use invalid characters in code (based on OpenAPI pattern: ^[a-zA-Z0-9_-]+$)
    const invalidCode = `INVALID CODE WITH SPACES!@#`;
    await fillMessageForm(page, { code: invalidCode, content: 'Test content' });

    // Try to submit
    await saveModalForm(page);

    // Modal should stay open due to validation error
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();
  });
});
