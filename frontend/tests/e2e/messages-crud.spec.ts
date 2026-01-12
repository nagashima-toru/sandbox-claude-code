import { test, expect } from '@playwright/test';
import {
  waitForFrontend,
  fillMessageForm,
  openCreateModal,
  saveModalForm,
  waitForModalToClose,
  createMessage,
  editMessage,
  deleteMessage,
} from './helpers';

test.describe('Messages CRUD Operations', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the messages page
    await page.goto('/');
    await waitForFrontend(page);
  });

  test('should display messages list', async ({ page }) => {
    // Check if the page header is visible
    await expect(page.getByRole('heading', { name: /Message Management/i })).toBeVisible();
  });

  test('should create a new message', async ({ page }) => {
    const timestamp = Date.now();
    const code = `TEST_${timestamp}`;
    const content = `Test message ${timestamp}`;

    // Open create modal
    await openCreateModal(page);

    // Fill in the form using helper function
    await fillMessageForm(page, { code, content });

    // Submit the form
    await saveModalForm(page);

    // Wait for modal to close
    await waitForModalToClose(page);

    // Verify the message appears in the list
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await expect(page.getByRole('table').getByText(code).first()).toBeVisible({ timeout: 10000 });
  });

  test('should edit an existing message', async ({ page }) => {
    // First, create a message to edit
    const timestamp = Date.now();
    const originalCode = `EDIT_${timestamp}`;
    const originalContent = `Original ${timestamp}`;

    // Create message using helper
    await createMessage(page, originalCode, originalContent);

    // Edit the message using helper
    const updatedContent = `Updated ${Date.now()}`;
    await editMessage(page, originalCode, updatedContent);

    // Verify the updated content appears in the table
    await expect(page.getByRole('table').getByText(updatedContent).first()).toBeVisible();
  });

  test('should delete a message', async ({ page }) => {
    // First, create a message to delete
    const timestamp = Date.now();
    const code = `DELETE_${timestamp}`;
    const content = `Delete ${timestamp}`;

    // Create message using helper
    await createMessage(page, code, content);

    // Delete the message using helper
    await deleteMessage(page, code);

    // Verify the message is no longer visible in the table
    await expect(page.getByRole('table').getByText(code)).not.toBeVisible();
  });

  test('should validate required fields', async ({ page }) => {
    // Open create modal
    await openCreateModal(page);

    // Try to submit without filling fields
    await saveModalForm(page);

    // Modal should stay open due to validation errors
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();
  });

  test('should prevent duplicate codes', async ({ page }) => {
    // Create first message
    const timestamp = Date.now();
    const duplicateCode = `DUP_${timestamp}`;

    await createMessage(page, duplicateCode, 'First message');

    // Try to create another message with same code
    await openCreateModal(page);
    await fillMessageForm(page, { code: duplicateCode, content: 'Second message' });
    await saveModalForm(page);

    // Modal should stay open due to duplicate error
    await page.waitForTimeout(1000);
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();
  });
});
