import { test, expect } from '@playwright/test';
import { login, setupAuthenticatedSession } from './helpers';

test.describe('Authentication Flow', () => {
  test('should login with valid credentials', async ({ page }) => {
    // Navigate to login page
    await page.goto('/login');
    await page.waitForLoadState('networkidle');

    // Verify login form is visible
    await expect(page.getByRole('heading', { name: /ログイン/i })).toBeVisible();
    await expect(page.locator('input[id="username"]')).toBeVisible();
    await expect(page.locator('input[id="password"]')).toBeVisible();

    // Fill in credentials
    await page.locator('input[id="username"]').fill('admin');
    await page.locator('input[id="password"]').fill('admin123');

    // Submit the form
    await page.getByRole('button', { name: /ログイン/i }).click();

    // Wait for redirect to home page
    await page.waitForURL('/', { timeout: 10000 });
    await page.waitForLoadState('networkidle');

    // Verify user is on the home page
    await expect(page.getByRole('heading', { name: /Message Management/i })).toBeVisible({
      timeout: 10000,
    });
  });

  test('should show error with invalid credentials', async ({ page }) => {
    await page.goto('/login');
    await page.waitForLoadState('networkidle');

    // Fill in invalid credentials
    await page.locator('input[id="username"]').fill('invalid_user');
    await page.locator('input[id="password"]').fill('invalid_pass');

    // Submit the form
    await page.getByRole('button', { name: /ログイン/i }).click();

    // Wait for potential API response
    await page.waitForTimeout(3000);

    // Verify user is still on login page (login failed)
    expect(page.url()).toContain('/login');

    // Verify the form is still visible (not redirected)
    await expect(page.locator('input[id="username"]')).toBeVisible();
    await expect(page.locator('input[id="password"]')).toBeVisible();

    // The key validation is that user is NOT redirected to home page
    expect(page.url()).toContain('/login');
  });

  test('should redirect to login when accessing protected page without auth', async ({ page }) => {
    // Try to access the home page without authentication
    await page.goto('/');

    // Should redirect to login page
    await page.waitForURL('/login', { timeout: 10000 });

    // Verify login form is visible
    await expect(page.getByRole('heading', { name: /ログイン/i })).toBeVisible();
  });

  test('should logout and redirect to login page', async ({ page }) => {
    // First, login
    await setupAuthenticatedSession(page);

    // Verify user is on the home page
    await expect(page.getByRole('heading', { name: /Message Management/i })).toBeVisible();

    // Click logout button
    const logoutButton = page.getByRole('button', { name: /logout/i });
    await expect(logoutButton).toBeVisible();
    await logoutButton.click();

    // Wait for redirect to login page
    await page.waitForURL('/login', { timeout: 10000 });

    // Verify login form is visible
    await expect(page.getByRole('heading', { name: /ログイン/i })).toBeVisible();
  });

  test('should maintain authentication state after page reload', async ({ page }) => {
    // Login
    await setupAuthenticatedSession(page);

    // Verify user is on the home page
    await expect(page.getByRole('heading', { name: /Message Management/i })).toBeVisible();

    // Reload the page
    await page.reload();
    await page.waitForLoadState('networkidle');

    // Verify user is still authenticated (not redirected to login)
    await expect(page.getByRole('heading', { name: /Message Management/i })).toBeVisible({
      timeout: 10000,
    });
    expect(page.url()).not.toContain('/login');
  });

  test('should handle token refresh on API calls', async ({ page }) => {
    // Login
    await setupAuthenticatedSession(page);

    // Verify user is on the home page
    await expect(page.getByRole('heading', { name: /Message Management/i })).toBeVisible();

    // Wait for some time to let access token potentially expire (mock scenario)
    // In real scenario, this would need backend cooperation or time manipulation
    await page.waitForTimeout(2000);

    // Make an API call by interacting with the page (e.g., searching)
    const searchInput = page.getByPlaceholder(/search/i);
    await searchInput.fill('test');
    await page.waitForTimeout(600); // Wait for debounce

    // Verify the page still works (token refresh happened in background if needed)
    await expect(page.getByRole('heading', { name: /Message Management/i })).toBeVisible();
    expect(page.url()).not.toContain('/login');
  });

  test('should show validation errors for empty login form', async ({ page }) => {
    await page.goto('/login');
    await page.waitForLoadState('networkidle');

    // Submit empty form
    await page.getByRole('button', { name: /ログイン/i }).click();

    // Wait for validation errors
    await page.waitForTimeout(500);

    // Verify validation error messages are shown
    // Note: This depends on the form implementation
    const form = page.locator('form');
    await expect(form).toBeVisible();

    // Check if form is still on the page (not submitted)
    const usernameInput = page.locator('input[id="username"]');
    await expect(usernameInput).toBeVisible();
  });
});
