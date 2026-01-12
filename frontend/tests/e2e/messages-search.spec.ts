import { test, expect } from '@playwright/test';
import {
  waitForFrontend,
  createMessage,
  getSearchInput,
  performSearch,
  clearSearch,
} from './helpers';

test.describe('Messages Search and Filter', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the messages page
    await page.goto('/');
    await waitForFrontend(page);

    // Create test messages for searching
    const testMessages = [
      { code: `SEARCH_TEST_1_${Date.now()}`, content: 'Apple product information' },
      { code: `SEARCH_TEST_2_${Date.now()}`, content: 'Banana product information' },
      { code: `SEARCH_TEST_3_${Date.now()}`, content: 'Cherry product information' },
    ];

    for (const msg of testMessages) {
      await createMessage(page, msg.code, msg.content);
    }

    // Reload to ensure all messages are loaded
    await page.reload();
    await waitForFrontend(page);
  });

  test('should search messages by code', async ({ page }) => {
    // Perform search using helper
    await performSearch(page, 'SEARCH_TEST_1');

    // Verify only matching messages are visible
    await expect(page.getByRole('table').locator('text=/SEARCH_TEST_1/i').first()).toBeVisible();
    await expect(page.getByRole('table').locator('text=/SEARCH_TEST_2/i')).not.toBeVisible();
    await expect(page.getByRole('table').locator('text=/SEARCH_TEST_3/i')).not.toBeVisible();
  });

  test('should search messages by content', async ({ page }) => {
    // Perform search using helper
    await performSearch(page, 'Apple');

    // Verify only matching messages are visible
    await expect(page.getByRole('table').locator('text=/Apple/i').first()).toBeVisible();
    await expect(page.getByRole('table').locator('text=/Banana/i')).not.toBeVisible();
    await expect(page.getByRole('table').locator('text=/Cherry/i')).not.toBeVisible();
  });

  test('should show all messages when search is cleared', async ({ page }) => {
    // Perform a search
    await performSearch(page, 'SEARCH_TEST_1');

    // Verify filtered results
    await expect(page.getByRole('table').locator('text=/SEARCH_TEST_1/i').first()).toBeVisible();

    // Clear the search
    await clearSearch(page);

    // Verify all test messages are visible again
    await expect(page.getByRole('table').locator('text=/SEARCH_TEST_1/i').first()).toBeVisible();
    await expect(page.getByRole('table').locator('text=/SEARCH_TEST_2/i').first()).toBeVisible();
    await expect(page.getByRole('table').locator('text=/SEARCH_TEST_3/i').first()).toBeVisible();
  });

  test('should show no results message for non-existent search', async ({ page }) => {
    // Search for non-existent message
    await performSearch(page, 'NONEXISTENT_MESSAGE_12345');

    // Verify no test messages are visible
    await expect(page.getByRole('table').locator('text=/SEARCH_TEST/i')).not.toBeVisible();

    // Check for "no results" or "not found" message
    const noResultsMessage = page.locator('text=/no results|not found|no messages|empty/i');
    await expect(noResultsMessage).toBeVisible({ timeout: 5000 });
  });

  test('should handle case-insensitive search', async ({ page }) => {
    // Search with lowercase
    await performSearch(page, 'apple');

    // Verify the message with "Apple" (capitalized) is still found
    await expect(page.getByRole('table').locator('text=/Apple/i').first()).toBeVisible();
  });

  test('should debounce search input', async ({ page }) => {
    // Get search input
    const searchInput = await getSearchInput(page);

    // Type rapidly without waiting
    await searchInput.pressSequentially('SEARCH_TEST_1', { delay: 50 });

    // Wait for debounce to complete
    await page.waitForTimeout(600);

    // Verify results are now filtered
    await expect(page.getByRole('table').locator('text=/SEARCH_TEST_1/i').first()).toBeVisible();
  });
});
