import { test, expect } from '@playwright/test';

test.describe('Messages CRUD Operations', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the messages page
    await page.goto('/');
    await page.waitForLoadState('networkidle');
  });

  test('should display messages list', async ({ page }) => {
    // Wait for the page to load
    await expect(page).toHaveTitle(/Messages/i);

    // Check if the table or messages container is visible
    const messagesContainer = page
      .locator('[data-testid="messages-table"]')
      .or(page.locator('table'));
    await expect(messagesContainer).toBeVisible();
  });

  test('should create a new message', async ({ page }) => {
    // Click the "New Message" button
    const newButton = page.getByRole('button', { name: /new message|create|add/i });
    await newButton.click();

    // Wait for modal to open
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    // Fill in the form
    const timestamp = Date.now();
    const code = `TEST_${timestamp}`;
    const content = `Test message created at ${new Date().toISOString()}`;

    await page.fill('input[name="code"], input[placeholder*="code" i]', code);
    await page.fill(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
      content
    );

    // Submit the form
    const saveButton = page.getByRole('button', { name: /save|submit|create/i });
    await saveButton.click();

    // Wait for modal to close
    await expect(modal).not.toBeVisible({ timeout: 10000 });

    // Verify the message appears in the list
    await page.waitForTimeout(1000); // Wait for the list to update
    await expect(page.locator(`text=${code}`)).toBeVisible();
    await expect(page.locator(`text=${content}`)).toBeVisible();
  });

  test('should edit an existing message', async ({ page }) => {
    // First, create a message to edit
    const timestamp = Date.now();
    const originalCode = `EDIT_${timestamp}`;
    const originalContent = `Original content ${timestamp}`;

    // Create message via UI
    await page.getByRole('button', { name: /new message|create|add/i }).click();
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    await page.fill('input[name="code"], input[placeholder*="code" i]', originalCode);
    await page.fill(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
      originalContent
    );
    await page.getByRole('button', { name: /save|submit|create/i }).click();
    await expect(modal).not.toBeVisible({ timeout: 10000 });

    // Wait for message to appear
    await page.waitForTimeout(1000);
    await expect(page.locator(`text=${originalCode}`)).toBeVisible();

    // Find and click the edit button for this message
    const row = page.locator(
      `tr:has-text("${originalCode}"), [data-testid="message-row"]:has-text("${originalCode}")`
    );
    const editButton = row.getByRole('button', { name: /edit/i });
    await editButton.click();

    // Wait for edit modal
    await expect(modal).toBeVisible();

    // Update the content
    const updatedContent = `Updated content ${Date.now()}`;
    const contentInput = page.locator(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]'
    );
    await contentInput.clear();
    await contentInput.fill(updatedContent);

    // Save changes
    await page.getByRole('button', { name: /save|update/i }).click();
    await expect(modal).not.toBeVisible({ timeout: 10000 });

    // Verify the updated content appears
    await page.waitForTimeout(1000);
    await expect(page.locator(`text=${updatedContent}`)).toBeVisible();
    await expect(page.locator(`text=${originalContent}`)).not.toBeVisible();
  });

  test('should delete a message', async ({ page }) => {
    // First, create a message to delete
    const timestamp = Date.now();
    const code = `DELETE_${timestamp}`;
    const content = `Message to delete ${timestamp}`;

    // Create message via UI
    await page.getByRole('button', { name: /new message|create|add/i }).click();
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    await page.fill('input[name="code"], input[placeholder*="code" i]', code);
    await page.fill(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
      content
    );
    await page.getByRole('button', { name: /save|submit|create/i }).click();
    await expect(modal).not.toBeVisible({ timeout: 10000 });

    // Wait for message to appear
    await page.waitForTimeout(1000);
    await expect(page.locator(`text=${code}`)).toBeVisible();

    // Find and click the delete button for this message
    const row = page.locator(
      `tr:has-text("${code}"), [data-testid="message-row"]:has-text("${code}")`
    );
    const deleteButton = row.getByRole('button', { name: /delete/i });
    await deleteButton.click();

    // Wait for confirmation dialog
    const confirmDialog = page.locator('[role="alertdialog"], [role="dialog"]');
    await expect(confirmDialog).toBeVisible();

    // Confirm deletion
    const confirmButton = page.getByRole('button', { name: /delete|confirm|yes/i });
    await confirmButton.click();

    // Wait for dialog to close
    await expect(confirmDialog).not.toBeVisible({ timeout: 10000 });

    // Verify the message is no longer visible
    await page.waitForTimeout(1000);
    await expect(page.locator(`text=${code}`)).not.toBeVisible();
  });

  test('should validate required fields', async ({ page }) => {
    // Click the "New Message" button
    await page.getByRole('button', { name: /new message|create|add/i }).click();

    // Wait for modal
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    // Try to submit without filling fields
    const saveButton = page.getByRole('button', { name: /save|submit|create/i });
    await saveButton.click();

    // Check for validation errors
    const errorMessages = page.locator('text=/required|must|cannot be empty/i');
    await expect(errorMessages.first()).toBeVisible({ timeout: 5000 });
  });

  test('should prevent duplicate codes', async ({ page }) => {
    // Create first message
    const timestamp = Date.now();
    const duplicateCode = `DUP_${timestamp}`;

    await page.getByRole('button', { name: /new message|create|add/i }).click();
    const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
    await expect(modal).toBeVisible();

    await page.fill('input[name="code"], input[placeholder*="code" i]', duplicateCode);
    await page.fill(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
      'First message'
    );
    await page.getByRole('button', { name: /save|submit|create/i }).click();
    await expect(modal).not.toBeVisible({ timeout: 10000 });

    // Wait for creation
    await page.waitForTimeout(1000);

    // Try to create another message with same code
    await page.getByRole('button', { name: /new message|create|add/i }).click();
    await expect(modal).toBeVisible();

    await page.fill('input[name="code"], input[placeholder*="code" i]', duplicateCode);
    await page.fill(
      'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
      'Second message'
    );
    await page.getByRole('button', { name: /save|submit|create/i }).click();

    // Check for error message about duplicate code
    const errorMessage = page.locator('text=/already exists|duplicate|conflict/i');
    await expect(errorMessage).toBeVisible({ timeout: 5000 });
  });
});
