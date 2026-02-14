import { test, expect } from '@playwright/test';
import { login, waitForFrontend, createMessage } from './helpers';

/**
 * E2E tests for permission UI with VIEWER role
 *
 * Tests the following scenarios:
 * - VIEWER cannot see the "Create" button
 * - VIEWER cannot see "Edit" and "Delete" buttons
 * - VIEWER sees the "readonly" info message
 * - VIEWER can click on a message row to view it in readonly mode
 * - All form fields are disabled in readonly mode
 */
test.describe('Permission UI - VIEWER Role', () => {
  test.beforeEach(async ({ page }) => {
    // Login as VIEWER user
    await login(page, 'viewer', 'viewer123');
    await waitForFrontend(page);
  });

  test('should not display the create button for VIEWER', async ({ page }) => {
    // Verify the create button is NOT visible
    const createButton = page.getByTestId('create-message-button');
    await expect(createButton).not.toBeVisible();
  });

  test('should display readonly info message for VIEWER', async ({ page }) => {
    // Verify the readonly info message IS visible
    const readonlyMessage = page.getByTestId('readonly-info-message');
    await expect(readonlyMessage).toBeVisible();

    // Verify the message text
    await expect(readonlyMessage).toContainText('閲覧のみ可能です');
  });

  test('should not display edit and delete buttons for VIEWER', async ({ page }) => {
    // Wait for the table to load
    await page.waitForTimeout(1000);

    // Try to find any edit or delete buttons in the first row (if exists)
    const messageRows = page.locator('[data-testid^="message-row-"]');
    const rowCount = await messageRows.count();

    if (rowCount > 0) {
      // Get the first message row
      const firstRow = messageRows.first();
      await expect(firstRow).toBeVisible();

      // Extract the message ID from the row's data-testid attribute
      const rowTestId = await firstRow.getAttribute('data-testid');
      const messageId = rowTestId?.replace('message-row-', '') || '';

      // Verify edit button is NOT visible (using locator that doesn't throw if not found)
      const editButton = page.getByTestId(`edit-message-button-${messageId}`);
      await expect(editButton).not.toBeVisible();

      // Verify delete button is NOT visible
      const deleteButton = page.getByTestId(`delete-message-button-${messageId}`);
      await expect(deleteButton).not.toBeVisible();
    }
  });

  test('should open readonly modal when clicking on a message row', async ({ page }) => {
    // Wait for the table to load
    await page.waitForTimeout(1000);

    // Find a message row to click
    const messageRows = page.locator('[data-testid^="message-row-"]');
    const rowCount = await messageRows.count();

    if (rowCount > 0) {
      const firstRow = messageRows.first();
      await expect(firstRow).toBeVisible();

      // Click on the row (not on any button)
      await firstRow.click();

      // Wait for modal to open
      const modal = page.getByTestId('message-modal');
      await expect(modal).toBeVisible({ timeout: 10000 });

      // Verify form fields are visible but disabled
      const codeInput = page.getByTestId('message-code-input');
      const contentInput = page.getByTestId('message-content-input');

      await expect(codeInput).toBeVisible();
      await expect(codeInput).toBeDisabled();
      await expect(contentInput).toBeVisible();
      await expect(contentInput).toBeDisabled();

      // Verify submit button is NOT visible in readonly mode
      const submitButton = page.getByTestId('message-form-submit');
      await expect(submitButton).not.toBeVisible();

      // Close the modal
      const cancelButton = page.getByTestId('message-form-cancel');
      await expect(cancelButton).toBeVisible();
      await cancelButton.click();

      // Wait for modal to close
      await expect(modal).not.toBeVisible({ timeout: 5000 });
    }
  });

  test('should display all form fields as disabled in readonly modal', async ({ page }) => {
    // This test uses an ADMIN user to create a test message first,
    // then logs in as VIEWER to verify readonly access

    // First, create a test message as ADMIN
    const adminPage = await page.context().newPage();
    await login(adminPage, 'admin', 'admin123');
    await waitForFrontend(adminPage);

    const timestamp = Date.now();
    const testCode = `VIEWER_TEST_${timestamp}`;
    const testContent = `Readonly test ${timestamp}`;
    await createMessage(adminPage, testCode, testContent);
    await adminPage.close();

    // Now, as VIEWER, search for this message
    const searchInput = page.getByTestId('search-input');
    await searchInput.fill(testCode);
    await page.waitForTimeout(600);

    // Click on the message row
    const row = page.locator(`[data-testid^="message-row-"]:has-text("${testCode}")`).first();
    await expect(row).toBeVisible({ timeout: 10000 });
    await row.click();

    // Wait for modal to open
    const modal = page.getByTestId('message-modal');
    await expect(modal).toBeVisible({ timeout: 10000 });

    // Verify all form fields are disabled
    const codeInput = page.getByTestId('message-code-input');
    const contentInput = page.getByTestId('message-content-input');

    await expect(codeInput).toBeDisabled();
    await expect(contentInput).toBeDisabled();

    // Verify the content matches what was created
    await expect(codeInput).toHaveValue(testCode);
    await expect(contentInput).toHaveValue(testContent);

    // Verify submit button is not visible
    const submitButton = page.getByTestId('message-form-submit');
    await expect(submitButton).not.toBeVisible();

    // Verify cancel button is visible (acts as "Close" in readonly mode)
    const cancelButton = page.getByTestId('message-form-cancel');
    await expect(cancelButton).toBeVisible();
  });

  test('should not be able to interact with disabled form fields', async ({ page }) => {
    // Wait for the table to load
    await page.waitForTimeout(1000);

    // Find a message row to click
    const messageRows = page.locator('[data-testid^="message-row-"]');
    const rowCount = await messageRows.count();

    if (rowCount > 0) {
      const firstRow = messageRows.first();
      await expect(firstRow).toBeVisible();

      // Click on the row
      await firstRow.click();

      // Wait for modal to open
      const modal = page.getByTestId('message-modal');
      await expect(modal).toBeVisible({ timeout: 10000 });

      // Try to interact with disabled fields (should not be possible)
      const codeInput = page.getByTestId('message-code-input');
      const contentInput = page.getByTestId('message-content-input');

      // Get current values
      const originalCode = await codeInput.inputValue();
      const originalContent = await contentInput.inputValue();

      // Attempt to fill (should not work because fields are disabled)
      await codeInput.fill('SHOULD_NOT_WORK').catch(() => {
        /* Expected to fail */
      });
      await contentInput.fill('SHOULD_NOT_WORK').catch(() => {
        /* Expected to fail */
      });

      // Verify values haven't changed
      await expect(codeInput).toHaveValue(originalCode);
      await expect(contentInput).toHaveValue(originalContent);

      // Close the modal
      const cancelButton = page.getByTestId('message-form-cancel');
      await cancelButton.click();
      await expect(modal).not.toBeVisible({ timeout: 5000 });
    }
  });
});
