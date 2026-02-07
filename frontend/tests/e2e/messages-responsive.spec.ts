import { test, expect } from '@playwright/test';
import { setupAuthenticatedSession, createMessage, openCreateModal } from './helpers';

test.describe('Messages Responsive Behavior', () => {
  test.describe('Desktop viewport', () => {
    test('should display table layout on desktop', async ({ page }) => {
      await page.setViewportSize({ width: 1280, height: 720 });
      await setupAuthenticatedSession(page);

      // Desktop should show table
      const table = page.locator('table');
      await expect(table).toBeVisible({ timeout: 10000 });

      // Verify table headers are visible
      await expect(page.getByRole('columnheader', { name: /id/i })).toBeVisible();
      await expect(page.getByRole('columnheader', { name: /code/i })).toBeVisible();
      await expect(page.getByRole('columnheader', { name: /content/i })).toBeVisible();
      await expect(page.getByRole('columnheader', { name: /actions/i })).toBeVisible();
    });
  });

  test.describe('Mobile viewport', () => {
    test('should display card layout on mobile', async ({ page }) => {
      // iPhone 12 viewport
      await page.setViewportSize({ width: 390, height: 844 });
      await setupAuthenticatedSession(page);

      // Create a test message to verify card display
      const timestamp = Date.now();
      const code = `MOBILE_${timestamp}`;
      const content = `Mobile test ${timestamp}`;
      await createMessage(page, code, content);

      // Search for the message to make sure it's visible
      const searchInput = page.getByTestId('search-input');
      await searchInput.fill(code);
      await page.waitForTimeout(600);

      // Mobile should NOT show table (hidden class applied)
      const table = page.locator('table');
      await expect(table).not.toBeVisible();

      // Verify the mobile card layout is used (check for mobile-specific structure)
      // Mobile view has divs with p-4 class instead of table
      const mobileView = page.getByTestId('message-table-mobile');
      await expect(mobileView).toBeVisible();
    });

    test('should have touch-friendly buttons on mobile', async ({ page }) => {
      // iPhone 12 viewport
      await page.setViewportSize({ width: 390, height: 844 });
      await setupAuthenticatedSession(page);

      // Create a test message
      const timestamp = Date.now();
      const code = `MOBILE_BTN_${timestamp}`;
      await createMessage(page, code, 'Mobile button test');

      // Search for the message
      const searchInput = page.getByTestId('search-input');
      await searchInput.fill(code);
      await page.waitForTimeout(600);

      // Verify edit and delete buttons are visible in mobile view
      const mobileView = page.getByTestId('message-table-mobile');

      // Find the message row within mobile view and extract ID from data-testid
      const row = mobileView.locator(`[data-testid^="message-row-"]:has-text("${code}")`).first();
      await expect(row).toBeVisible();

      const rowTestId = await row.getAttribute('data-testid');
      const messageId = rowTestId?.replace('message-row-', '') || '';

      // Use data-testid for buttons within the row
      const editButton = row.getByTestId(`edit-message-button-${messageId}`);
      const deleteButton = row.getByTestId(`delete-message-button-${messageId}`);

      await expect(editButton).toBeVisible();
      await expect(deleteButton).toBeVisible();

      // Buttons should be touch-friendly (check if they're clickable)
      await expect(editButton).toBeEnabled();
      await expect(deleteButton).toBeEnabled();
    });

    test('should display modal in full screen on mobile', async ({ page }) => {
      // iPhone 12 viewport
      await page.setViewportSize({ width: 390, height: 844 });
      await setupAuthenticatedSession(page);

      // Open create modal
      await openCreateModal(page);

      // Modal should be visible
      const modal = page.getByTestId('message-modal');
      await expect(modal).toBeVisible();

      // Modal should take significant portion of viewport on mobile
      const modalBox = await modal.boundingBox();
      const viewportSize = page.viewportSize();

      if (modalBox && viewportSize) {
        // Modal width should be close to viewport width (allowing for small margins)
        const widthRatio = modalBox.width / viewportSize.width;
        expect(widthRatio).toBeGreaterThan(0.8); // At least 80% of viewport width
      }
    });
  });

  test.describe('Tablet viewport', () => {
    test('should display appropriate layout on tablet', async ({ page }) => {
      // iPad viewport
      await page.setViewportSize({ width: 768, height: 1024 });
      await setupAuthenticatedSession(page);

      // Tablet should show table (iPad is typically >= 768px)
      const table = page.locator('table');
      await expect(table).toBeVisible({ timeout: 10000 });
    });

    test('should handle orientation changes', async ({ page }) => {
      // Start in portrait
      await page.setViewportSize({ width: 768, height: 1024 });
      await setupAuthenticatedSession(page);

      // Verify table is visible in portrait
      const table = page.locator('table');
      await expect(table).toBeVisible();

      // Switch to landscape
      await page.setViewportSize({ width: 1024, height: 768 });
      await page.waitForTimeout(500);

      // Table should still be visible in landscape
      await expect(table).toBeVisible();
    });
  });

  test.describe('Small mobile viewport', () => {
    test('should be usable on small screens', async ({ page }) => {
      // iPhone SE viewport
      await page.setViewportSize({ width: 375, height: 667 });
      await setupAuthenticatedSession(page);

      // Create button should be visible and accessible
      const createButton = page.getByTestId('create-message-button');
      await expect(createButton).toBeVisible();

      // Search should be visible
      const searchInput = page.getByTestId('search-input');
      await expect(searchInput).toBeVisible();

      // Open create modal
      await openCreateModal(page);

      // Form fields should be visible and usable (scope to modal to avoid conflicts)
      const modal = page.getByTestId('message-modal');
      const codeInput = modal.getByLabel(/code/i);
      const contentInput = modal.getByLabel(/content/i);

      await expect(codeInput).toBeVisible();
      await expect(contentInput).toBeVisible();

      // Should be able to type in inputs
      await codeInput.fill('TEST');
      await contentInput.fill('Test content');

      expect(await codeInput.inputValue()).toBe('TEST');
      expect(await contentInput.inputValue()).toBe('Test content');
    });
  });

  test.describe('Breakpoint transitions', () => {
    test('should adapt layout when resizing from desktop to mobile', async ({ page }) => {
      // Start at desktop size
      await page.setViewportSize({ width: 1280, height: 720 });
      await setupAuthenticatedSession(page);

      // Verify table is visible
      let table = page.locator('table');
      await expect(table).toBeVisible();

      // Resize to mobile
      await page.setViewportSize({ width: 375, height: 667 });
      await page.waitForTimeout(500);

      // Table should now be hidden
      table = page.locator('table');
      await expect(table).not.toBeVisible();
    });

    test('should adapt layout when resizing from mobile to desktop', async ({ page }) => {
      // Start at mobile size
      await page.setViewportSize({ width: 375, height: 667 });
      await setupAuthenticatedSession(page);

      // Table should be hidden
      let table = page.locator('table');
      await expect(table).not.toBeVisible();

      // Resize to desktop
      await page.setViewportSize({ width: 1280, height: 720 });
      await page.waitForTimeout(500);

      // Table should now be visible
      table = page.locator('table');
      await expect(table).toBeVisible();
    });
  });
});
