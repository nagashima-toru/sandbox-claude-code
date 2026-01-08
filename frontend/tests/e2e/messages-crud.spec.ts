import { test, expect } from '@playwright/test';

test.describe('Messages CRUD Operations', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the messages page
    await page.goto('/');
    await page.waitForLoadState('networkidle');
  });

  test('should display messages list', async ({ page }) => {
    // Wait for the page to load
    await expect(page).toHaveTitle(/Message Management/i);

    // Check if the page header is visible
    await expect(page.getByRole('heading', { name: /Message Management/i })).toBeVisible();
  });

  test('should create a new message', async ({ page }) => {
    // Click the "New Message" button
    const newButton = page.getByRole('button', { name: /new message/i });
    await newButton.click();

    // Wait for modal to open
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    // Fill in the form
    const timestamp = Date.now();
    const code = `TEST_${timestamp}`;
    const content = `Test message ${timestamp}`;

    // Wait for form fields and fill them
    const codeInput = modal.locator('input[name="code"]');
    const contentInput = modal.locator('input[name="content"]');

    await expect(codeInput).toBeVisible();
    await expect(contentInput).toBeVisible();

    await codeInput.fill(code);
    await contentInput.fill(content);

    // Submit the form
    const saveButton = modal.getByRole('button', { name: /save/i });
    await saveButton.click();

    // Wait for modal to close
    await expect(modal).not.toBeVisible({ timeout: 15000 });

    // Verify the message appears in the list
    await page.waitForTimeout(2000); // Wait for the list to update
    await expect(page.getByText(code)).toBeVisible({ timeout: 5000 });
  });

  test('should edit an existing message', async ({ page }) => {
    // First, create a message to edit
    const timestamp = Date.now();
    const originalCode = `EDIT_${timestamp}`;
    const originalContent = `Original ${timestamp}`;

    // Create message via UI
    await page.getByRole('button', { name: /new message/i }).click();
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    await page.locator('input[name="code"]').fill(originalCode);
    await page.locator('input[name="content"]').fill(originalContent);
    await modal.getByRole('button', { name: /save/i }).click();
    await expect(modal).not.toBeVisible({ timeout: 15000 });

    // Wait for message to appear
    await page.waitForTimeout(2000);
    await expect(page.getByText(originalCode)).toBeVisible();

    // Find and click the edit button for this message
    const row = page.locator(`tr:has-text("${originalCode}")`);
    await row.getByRole('button', { name: /edit/i }).click();

    // Wait for edit modal
    await expect(modal).toBeVisible();

    // Update the content
    const updatedContent = `Updated ${Date.now()}`;
    const contentInput = page.locator('input[name="content"]');
    await contentInput.clear();
    await contentInput.fill(updatedContent);

    // Save changes
    await modal.getByRole('button', { name: /save/i }).click();
    await expect(modal).not.toBeVisible({ timeout: 15000 });

    // Verify the updated content appears
    await page.waitForTimeout(2000);
    await expect(page.getByText(updatedContent)).toBeVisible();
  });

  test('should delete a message', async ({ page }) => {
    // First, create a message to delete
    const timestamp = Date.now();
    const code = `DELETE_${timestamp}`;
    const content = `Delete ${timestamp}`;

    // Create message via UI
    await page.getByRole('button', { name: /new message/i }).click();
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    await page.locator('input[name="code"]').fill(code);
    await page.locator('input[name="content"]').fill(content);
    await modal.getByRole('button', { name: /save/i }).click();
    await expect(modal).not.toBeVisible({ timeout: 15000 });

    // Wait for message to appear
    await page.waitForTimeout(2000);
    await expect(page.getByText(code)).toBeVisible();

    // Find and click the delete button for this message
    const row = page.locator(`tr:has-text("${code}")`);
    await row.getByRole('button', { name: /delete/i }).click();

    // Wait for confirmation dialog
    const confirmDialog = page.locator('[role="alertdialog"]').or(page.locator('[role="dialog"]'));
    await expect(confirmDialog).toBeVisible();

    // Confirm deletion
    await page
      .getByRole('button', { name: /delete/i })
      .last()
      .click();

    // Wait for dialog to close
    await expect(confirmDialog).not.toBeVisible({ timeout: 15000 });

    // Verify the message is no longer visible
    await page.waitForTimeout(2000);
    await expect(page.getByText(code)).not.toBeVisible();
  });

  test('should validate required fields', async ({ page }) => {
    // Click the "New Message" button
    await page.getByRole('button', { name: /new message/i }).click();

    // Wait for modal
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    // Try to submit without filling fields
    await modal.getByRole('button', { name: /save/i }).click();

    // Modal should stay open due to validation errors
    await expect(modal).toBeVisible();
  });

  test('should prevent duplicate codes', async ({ page }) => {
    // Create first message
    const timestamp = Date.now();
    const duplicateCode = `DUP_${timestamp}`;

    await page.getByRole('button', { name: /new message/i }).click();
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();

    await page.locator('input[name="code"]').fill(duplicateCode);
    await page.locator('input[name="content"]').fill('First message');
    await modal.getByRole('button', { name: /save/i }).click();
    await expect(modal).not.toBeVisible({ timeout: 15000 });

    // Wait for creation
    await page.waitForTimeout(2000);

    // Try to create another message with same code
    await page.getByRole('button', { name: /new message/i }).click();
    await expect(modal).toBeVisible();

    await page.locator('input[name="code"]').fill(duplicateCode);
    await page.locator('input[name="content"]').fill('Second message');
    await modal.getByRole('button', { name: /save/i }).click();

    // Modal should stay open due to duplicate error
    await page.waitForTimeout(1000);
    await expect(modal).toBeVisible();
  });
});
