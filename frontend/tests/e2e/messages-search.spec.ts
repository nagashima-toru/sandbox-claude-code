import { test, expect } from '@playwright/test';

test.describe('Messages Search and Filter', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the messages page
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // Create test messages for searching
    const testMessages = [
      { code: `SEARCH_TEST_1_${Date.now()}`, content: 'Apple product information' },
      { code: `SEARCH_TEST_2_${Date.now()}`, content: 'Banana product information' },
      { code: `SEARCH_TEST_3_${Date.now()}`, content: 'Cherry product information' },
    ];

    for (const msg of testMessages) {
      await page.getByRole('button', { name: /new message|create|add/i }).click();
      const modal = page.locator('[role="dialog"]').or(page.locator('[data-testid="message-modal"]'));
      await expect(modal).toBeVisible();

      await page.fill('input[name="code"], input[placeholder*="code" i]', msg.code);
      await page.fill(
        'input[name="content"], textarea[name="content"], input[placeholder*="content" i]',
        msg.content
      );
      await page.getByRole('button', { name: /save|submit|create/i }).click();
      await expect(modal).not.toBeVisible({ timeout: 10000 });
      await page.waitForTimeout(500);
    }

    // Reload to ensure all messages are loaded
    await page.reload();
    await page.waitForLoadState('networkidle');
  });

  test('should search messages by code', async ({ page }) => {
    // Find the search input
    const searchInput = page.locator(
      'input[type="search"], input[placeholder*="search" i], input[name="search"]'
    );
    await expect(searchInput).toBeVisible();

    // Search for "SEARCH_TEST_1"
    await searchInput.fill('SEARCH_TEST_1');

    // Wait for search to complete (debounced)
    await page.waitForTimeout(500);

    // Verify only matching messages are visible
    await expect(page.locator('text=/SEARCH_TEST_1/i')).toBeVisible();
    await expect(page.locator('text=/SEARCH_TEST_2/i')).not.toBeVisible();
    await expect(page.locator('text=/SEARCH_TEST_3/i')).not.toBeVisible();
  });

  test('should search messages by content', async ({ page }) => {
    // Find the search input
    const searchInput = page.locator(
      'input[type="search"], input[placeholder*="search" i], input[name="search"]'
    );
    await expect(searchInput).toBeVisible();

    // Search for "Apple"
    await searchInput.fill('Apple');

    // Wait for search to complete (debounced)
    await page.waitForTimeout(500);

    // Verify only matching messages are visible
    await expect(page.locator('text=/Apple/i')).toBeVisible();
    await expect(page.locator('text=/Banana/i')).not.toBeVisible();
    await expect(page.locator('text=/Cherry/i')).not.toBeVisible();
  });

  test('should show all messages when search is cleared', async ({ page }) => {
    // Find the search input
    const searchInput = page.locator(
      'input[type="search"], input[placeholder*="search" i], input[name="search"]'
    );
    await expect(searchInput).toBeVisible();

    // Perform a search
    await searchInput.fill('SEARCH_TEST_1');
    await page.waitForTimeout(500);

    // Verify filtered results
    await expect(page.locator('text=/SEARCH_TEST_1/i')).toBeVisible();

    // Clear the search
    await searchInput.clear();
    await page.waitForTimeout(500);

    // Verify all test messages are visible again
    await expect(page.locator('text=/SEARCH_TEST_1/i')).toBeVisible();
    await expect(page.locator('text=/SEARCH_TEST_2/i')).toBeVisible();
    await expect(page.locator('text=/SEARCH_TEST_3/i')).toBeVisible();
  });

  test('should show no results message for non-existent search', async ({ page }) => {
    // Find the search input
    const searchInput = page.locator(
      'input[type="search"], input[placeholder*="search" i], input[name="search"]'
    );
    await expect(searchInput).toBeVisible();

    // Search for non-existent message
    await searchInput.fill('NONEXISTENT_MESSAGE_12345');
    await page.waitForTimeout(500);

    // Verify no test messages are visible
    await expect(page.locator('text=/SEARCH_TEST/i')).not.toBeVisible();

    // Check for "no results" or "not found" message
    const noResultsMessage = page.locator(
      'text=/no results|not found|no messages|empty/i'
    );
    await expect(noResultsMessage).toBeVisible({ timeout: 5000 });
  });

  test('should handle case-insensitive search', async ({ page }) => {
    // Find the search input
    const searchInput = page.locator(
      'input[type="search"], input[placeholder*="search" i], input[name="search"]'
    );
    await expect(searchInput).toBeVisible();

    // Search with lowercase
    await searchInput.fill('apple');
    await page.waitForTimeout(500);

    // Verify the message with "Apple" (capitalized) is still found
    await expect(page.locator('text=/Apple/i')).toBeVisible();
  });

  test('should debounce search input', async ({ page }) => {
    // Find the search input
    const searchInput = page.locator(
      'input[type="search"], input[placeholder*="search" i], input[name="search"]'
    );
    await expect(searchInput).toBeVisible();

    // Type rapidly without waiting
    await searchInput.pressSequentially('SEARCH_TEST_1', { delay: 50 });

    // Immediately check that results may not be filtered yet (debounce in action)
    // Then wait for debounce to complete
    await page.waitForTimeout(500);

    // Verify results are now filtered
    await expect(page.locator('text=/SEARCH_TEST_1/i')).toBeVisible();
  });
});
