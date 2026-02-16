import { test, expect } from '@playwright/test';
import {
  login,
  waitForFrontend,
  createMessage,
  editMessage,
  deleteMessage,
  openCreateModal,
} from './helpers';

/**
 * E2E tests for permission UI with ADMIN role
 *
 * Tests the following scenarios:
 * - ADMIN can see the "Create" button
 * - ADMIN can create messages
 * - ADMIN can edit messages
 * - ADMIN can delete messages
 * - ADMIN does not see the "readonly" info message
 */
test.describe('Permission UI - ADMIN Role', () => {
  test.beforeEach(async ({ page }) => {
    // Login as ADMIN user
    await login(page, 'admin', 'admin123');
    await waitForFrontend(page);
  });

  test('should display the create button for ADMIN', async ({ page }) => {
    // Verify the create button is visible
    const createButton = page.getByTestId('create-message-button');
    await expect(createButton).toBeVisible();
  });

  test('should not display readonly info message for ADMIN', async ({ page }) => {
    // Verify the readonly info message is NOT visible
    const readonlyMessage = page.getByTestId('readonly-info-message');
    await expect(readonlyMessage).not.toBeVisible();
  });

  test('should allow ADMIN to create a new message', async ({ page }) => {
    const timestamp = Date.now();
    const code = `ADMIN_CREATE_${timestamp}`;
    const content = `Admin created message ${timestamp}`;

    // Create a message
    await createMessage(page, code, content);

    // Search for the created message to verify it exists
    const searchInput = page.getByTestId('search-input');
    await searchInput.fill(code);
    await page.waitForTimeout(600);

    // Verify the message appears in the table
    const row = page.locator(`[data-testid^="message-row-"]:has-text("${code}")`).first();
    await expect(row).toBeVisible({ timeout: 10000 });
    await expect(row).toContainText(content);
  });

  test('should allow ADMIN to edit a message', async ({ page }) => {
    const timestamp = Date.now();
    const originalCode = `ADMIN_EDIT_${timestamp}`;
    const originalContent = `Original content ${timestamp}`;
    const updatedContent = `Updated content ${Date.now()}`;

    // Create a message first
    await createMessage(page, originalCode, originalContent);

    // Edit the message
    await editMessage(page, originalCode, updatedContent);

    // Search for the message by code to verify it was updated
    const searchInput = page.getByTestId('search-input');
    await searchInput.fill(originalCode);
    await page.waitForTimeout(600);

    // Verify the updated content appears in the table
    const rowWithUpdatedContent = page
      .locator(`[data-testid^="message-row-"]:has-text("${updatedContent}")`)
      .first();
    await expect(rowWithUpdatedContent).toBeVisible();
  });

  test('should allow ADMIN to delete a message', async ({ page }) => {
    const timestamp = Date.now();
    const code = `ADMIN_DELETE_${timestamp}`;
    const content = `Delete message ${timestamp}`;

    // Create a message first
    await createMessage(page, code, content);

    // Delete the message
    await deleteMessage(page, code);

    // Search for the deleted message to verify it's gone
    const searchInput = page.getByTestId('search-input');
    await searchInput.fill(code);
    await page.waitForTimeout(600);

    // Verify the message is no longer in the table
    const deletedRow = page.locator(`[data-testid^="message-row-"]:has-text("${code}")`).first();
    await expect(deletedRow).not.toBeVisible();
  });

  test('should display edit and delete buttons for each message', async ({ page }) => {
    const timestamp = Date.now();
    const code = `ADMIN_BUTTONS_${timestamp}`;
    const content = `Button test ${timestamp}`;

    // Create a message first
    await createMessage(page, code, content);

    // Search for the message
    const searchInput = page.getByTestId('search-input');
    await searchInput.fill(code);
    await page.waitForTimeout(600);

    // Find the row
    const row = page.locator(`[data-testid^="message-row-"]:has-text("${code}")`).first();
    await expect(row).toBeVisible({ timeout: 10000 });

    // Extract the message ID from the row's data-testid attribute
    const rowTestId = await row.getAttribute('data-testid');
    const messageId = rowTestId?.replace('message-row-', '') || '';

    // Verify edit button is visible (use first() to handle desktop/mobile views)
    const editButton = page.getByTestId(`edit-message-button-${messageId}`).first();
    await expect(editButton).toBeVisible();

    // Verify delete button is visible (use first() to handle desktop/mobile views)
    const deleteButton = page.getByTestId(`delete-message-button-${messageId}`).first();
    await expect(deleteButton).toBeVisible();
  });

  test('should open create modal when clicking create button', async ({ page }) => {
    // Click the create button
    await openCreateModal(page);

    // Verify the modal is visible
    const modal = page.getByTestId('message-modal');
    await expect(modal).toBeVisible();

    // Verify form fields are editable (not disabled)
    const codeInput = page.getByTestId('message-code-input');
    const contentInput = page.getByTestId('message-content-input');

    await expect(codeInput).toBeVisible();
    await expect(codeInput).toBeEnabled();
    await expect(contentInput).toBeVisible();
    await expect(contentInput).toBeEnabled();
  });
});
