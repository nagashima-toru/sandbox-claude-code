import { Page, Locator, expect } from '@playwright/test';

/**
 * Wait for a modal/dialog to be visible and ready for interaction
 */
export async function waitForModal(page: Page): Promise<Locator> {
  const modal = page.locator('[role="dialog"]');
  await expect(modal).toBeVisible({ timeout: 10000 });
  // Wait a bit for any animations to complete
  await page.waitForTimeout(300);
  return modal;
}

/**
 * Fill a form field with proper visibility and enabled checks
 * Handles both input and textarea elements
 */
export async function fillFormField(
  page: Page,
  fieldName: 'code' | 'content',
  value: string
): Promise<void> {
  // For 'content' field, it's a textarea; for 'code' it's an input
  const selector =
    fieldName === 'content' ? `textarea[name="${fieldName}"]` : `input[name="${fieldName}"]`;

  const field = page.locator(selector).first();

  // Wait for field to be visible and enabled
  await expect(field).toBeVisible({ timeout: 10000 });
  await expect(field).toBeEnabled({ timeout: 5000 });

  // Clear and fill the field
  await field.clear();
  await field.fill(value);

  // Verify the value was set
  await expect(field).toHaveValue(value);
}

/**
 * Fill multiple form fields in a modal
 */
export async function fillMessageForm(
  page: Page,
  data: { code: string; content: string }
): Promise<void> {
  await fillFormField(page, 'code', data.code);
  await fillFormField(page, 'content', data.content);
}

/**
 * Get the search input locator
 * The search bar has type="text" and a specific placeholder
 */
export async function getSearchInput(page: Page): Promise<Locator> {
  const searchInput = page.getByPlaceholder(/search/i);
  await expect(searchInput).toBeVisible({ timeout: 10000 });
  return searchInput;
}

/**
 * Perform a search and wait for results to update
 */
export async function performSearch(page: Page, searchTerm: string): Promise<void> {
  const searchInput = await getSearchInput(page);
  await searchInput.clear();
  await searchInput.fill(searchTerm);

  // Wait for debounce (300ms) + a bit more for render
  await page.waitForTimeout(600);
}

/**
 * Clear search and wait for all results to return
 */
export async function clearSearch(page: Page): Promise<void> {
  const searchInput = await getSearchInput(page);
  await searchInput.clear();

  // Wait for debounce + render
  await page.waitForTimeout(600);
}

/**
 * Open the create message modal
 */
export async function openCreateModal(page: Page): Promise<Locator> {
  const newButton = page.getByRole('button', { name: /new message/i });
  await expect(newButton).toBeVisible({ timeout: 5000 });
  await newButton.click();

  return await waitForModal(page);
}

/**
 * Save the form in a modal
 */
export async function saveModalForm(page: Page): Promise<void> {
  const modal = page.locator('[role="dialog"]');
  const saveButton = modal.getByRole('button', { name: /save/i });
  await expect(saveButton).toBeVisible();
  await expect(saveButton).toBeEnabled();
  await saveButton.click();
}

/**
 * Cancel the form in a modal
 */
export async function cancelModalForm(page: Page): Promise<void> {
  const modal = page.locator('[role="dialog"]');
  const cancelButton = modal.getByRole('button', { name: /cancel/i });
  await expect(cancelButton).toBeVisible();
  await expect(cancelButton).toBeEnabled();
  await cancelButton.click();
}

/**
 * Wait for modal to close
 */
export async function waitForModalToClose(page: Page): Promise<void> {
  const modal = page.locator('[role="dialog"]');
  await expect(modal).not.toBeVisible({ timeout: 15000 });
}

/**
 * Create a message via UI (complete flow)
 */
export async function createMessage(page: Page, code: string, content: string): Promise<void> {
  await openCreateModal(page);
  await fillMessageForm(page, { code, content });
  await saveModalForm(page);
  await waitForModalToClose(page);

  // Wait for the list to update and network to be idle
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
}

/**
 * Edit a message by finding its row
 */
export async function editMessage(
  page: Page,
  originalCode: string,
  newContent: string
): Promise<void> {
  const row = page.locator(`tr:has-text("${originalCode}")`);

  // Try to find the row; if not visible, search for it
  const isVisible = await row.isVisible().catch(() => false);
  if (!isVisible) {
    const searchInput = page.getByPlaceholder(/search/i);
    await searchInput.fill(originalCode);
    await page.waitForTimeout(600); // Wait for debounce
  }

  await row.getByRole('button', { name: /edit/i }).click();

  await waitForModal(page);
  await fillFormField(page, 'content', newContent);
  await saveModalForm(page);
  await waitForModalToClose(page);

  // Wait for update and network to be idle
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
}

/**
 * Delete a message by finding its row
 */
export async function deleteMessage(page: Page, code: string): Promise<void> {
  const row = page.locator(`tr:has-text("${code}")`);

  // Try to find the row; if not visible, search for it
  const isVisible = await row.isVisible().catch(() => false);
  if (!isVisible) {
    const searchInput = page.getByPlaceholder(/search/i);
    await searchInput.fill(code);
    await page.waitForTimeout(600); // Wait for debounce
  }

  await row.getByRole('button', { name: /delete/i }).click();

  // Wait for confirmation dialog
  const confirmDialog = page.locator('[role="alertdialog"]').or(page.locator('[role="dialog"]'));
  await expect(confirmDialog).toBeVisible({ timeout: 5000 });

  // Confirm deletion (use last() to get the delete button in the dialog, not the table)
  await page
    .getByRole('button', { name: /delete/i })
    .last()
    .click();

  // Wait for dialog to close
  await expect(confirmDialog).not.toBeVisible({ timeout: 15000 });

  // Wait for list update and network to be idle
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
}

/**
 * Wait for the frontend to be ready
 */
export async function waitForFrontend(page: Page): Promise<void> {
  await page.waitForLoadState('networkidle');
  await page.waitForLoadState('domcontentloaded');

  // Wait for the page title to load
  await expect(page).toHaveTitle(/Message Management/i, { timeout: 10000 });

  // Wait for the search input to be visible, which confirms messages have loaded
  // (SearchBar is only rendered after React Query finishes loading)
  const searchInput = page.getByPlaceholder(/search/i);
  await expect(searchInput).toBeVisible({ timeout: 15000 });
}
