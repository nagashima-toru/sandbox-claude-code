import { test, expect, type Page } from '@playwright/test';
import { login } from './helpers';

// Helper: clear locale from localStorage and navigate to login page
async function clearLocale(page: Page) {
  await page.goto('/login');
  await page.evaluate(() => localStorage.removeItem('locale'));
  await page.reload();
  await page.waitForLoadState('networkidle');
  await expect(page.getByTestId('language-switcher')).toBeVisible({ timeout: 10000 });
}

// Helper: switch to English (click EN button in Japanese mode, wait for DOM change)
async function switchToEnglish(page: Page) {
  const switcher = page.getByTestId('language-switcher');
  await expect(switcher).toBeVisible({ timeout: 5000 });
  // Button shows "EN" when locale is 'ja'
  if ((await switcher.textContent())?.trim() === 'EN') {
    await switcher.click();
    // Wait until the button changes to "日" (locale is now 'en')
    await expect(switcher).toHaveText('日', { timeout: 3000 });
  }
}

// Helper: switch to Japanese (click 日 button in English mode, wait for DOM change)
async function switchToJapanese(page: Page) {
  const switcher = page.getByTestId('language-switcher');
  await expect(switcher).toBeVisible({ timeout: 5000 });
  // Button shows "日" when locale is 'en'
  if ((await switcher.textContent())?.trim() === '日') {
    await switcher.click();
    // Wait until the button changes to "EN" (locale is now 'ja')
    await expect(switcher).toHaveText('EN', { timeout: 3000 });
  }
}

test.describe('言語切り替え機能 (Task 4.1)', () => {
  test.beforeEach(async ({ page }) => {
    await clearLocale(page);
  });

  test('ログイン画面に言語切り替えボタンが表示される', async ({ page }) => {
    await expect(page.getByTestId('language-switcher')).toBeVisible({ timeout: 10000 });
  });

  test('メイン画面に言語切り替えボタンが表示される', async ({ page }) => {
    await login(page);
    await expect(page.getByTestId('language-switcher')).toBeVisible({ timeout: 10000 });
  });

  test('日本語選択時にログイン画面のUIテキストが日本語で表示される', async ({ page }) => {
    // After clearLocale, default is Japanese - verify switcher shows "EN"
    await expect(page.getByTestId('language-switcher')).toHaveText('EN');

    await expect(page.getByRole('heading', { name: 'ログイン' })).toBeVisible();
    await expect(page.getByText('ユーザー名')).toBeVisible();
    await expect(page.getByText('パスワード')).toBeVisible();
    await expect(page.getByTestId('login-submit-button')).toHaveText('ログイン');
  });

  test('英語選択時にログイン画面のUIテキストが英語で表示される', async ({ page }) => {
    await switchToEnglish(page);

    await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible();
    await expect(page.getByText('Username')).toBeVisible();
    await expect(page.getByText('Password')).toBeVisible();
    await expect(page.getByTestId('login-submit-button')).toHaveText('Login');
  });

  test('英語選択時にメイン画面のUIテキストが英語で表示される', async ({ page }) => {
    await login(page);
    await page.waitForLoadState('networkidle');

    await switchToEnglish(page);

    await expect(page.getByRole('heading', { name: 'Message Management' })).toBeVisible();
    await expect(page.getByText('Manage all your messages in one place')).toBeVisible();
    await expect(page.getByTestId('create-message-button')).toContainText('New Message');
    await expect(page.getByRole('button', { name: /Logout/i })).toBeVisible();
  });

  test('日本語選択時にメイン画面のUIテキストが日本語で表示される', async ({ page }) => {
    await login(page);
    await page.waitForLoadState('networkidle');

    // Switch to English first, then back to Japanese
    await switchToEnglish(page);
    await switchToJapanese(page);

    await expect(page.getByRole('heading', { name: 'メッセージ管理' })).toBeVisible();
    await expect(page.getByText('すべてのメッセージを一元管理します')).toBeVisible();
    await expect(page.getByTestId('create-message-button')).toContainText('新規作成');
    await expect(page.getByRole('button', { name: /ログアウト/i })).toBeVisible();
  });

  test('テーブルヘッダーが選択言語で表示される', async ({ page }) => {
    await login(page);
    await page.waitForLoadState('networkidle');

    await switchToEnglish(page);

    const header = page.getByTestId('message-table-header');
    await expect(header).toBeVisible({ timeout: 10000 });
    // Table headers in English: ID, Code, Content, Actions
    await expect(header.getByRole('button', { name: /Sort by ID|ID/ }).first()).toBeVisible();
    await expect(header.getByRole('button', { name: /Sort by Code|Code/ }).first()).toBeVisible();
    await expect(
      header.getByRole('button', { name: /Sort by Content|Content/ }).first()
    ).toBeVisible();
    await expect(header.getByText('Actions')).toBeVisible();
  });

  test('ダイアログのテキストが英語で表示される', async ({ page }) => {
    await login(page);
    await page.waitForLoadState('networkidle');

    await switchToEnglish(page);

    // Open create modal
    const createButton = page.getByTestId('create-message-button');
    await expect(createButton).toBeVisible({ timeout: 10000 });
    await createButton.click();

    const modal = page.getByTestId('message-modal');
    await expect(modal).toBeVisible({ timeout: 10000 });

    // Modal title should be in English
    await expect(modal.getByText('Create Message')).toBeVisible();
    // Save button
    await expect(page.getByTestId('message-form-submit')).toContainText('Save');
    // Cancel button
    await expect(page.getByTestId('message-form-cancel')).toContainText('Cancel');

    // Close modal and verify it closes
    await page.getByTestId('message-form-cancel').click();
    await expect(modal).not.toBeVisible({ timeout: 10000 });
  });

  test('ログインエラーメッセージが英語で表示される', async ({ page }) => {
    await switchToEnglish(page);

    // Fill in invalid credentials
    await page.getByTestId('login-username-input').fill('wronguser');
    await page.getByTestId('login-password-input').fill('wrongpassword');
    await page.getByTestId('login-submit-button').click();

    // Error message should appear in English
    // Use form [role="alert"] to avoid matching Next.js route announcer (#__next-route-announcer__)
    const errorAlert = page.locator('form [role="alert"]');
    await expect(errorAlert).toBeVisible({ timeout: 10000 });
    await expect(errorAlert).toContainText('Login failed');
  });
});

test.describe('言語設定の永続化 (Task 4.2)', () => {
  test.beforeEach(async ({ page }) => {
    await clearLocale(page);
  });

  test('英語選択後にページをリロードしても英語が維持される', async ({ page }) => {
    await switchToEnglish(page);
    await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible();

    // Reload
    await page.reload();
    await page.waitForLoadState('networkidle');

    // Should still be in English
    await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId('language-switcher')).toHaveText('日'); // Shows "日" when in English mode
  });

  test('日本語に戻した後にページをリロードしても日本語が維持される', async ({ page }) => {
    // Switch to English first
    await switchToEnglish(page);
    await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible();

    // Switch back to Japanese
    await switchToJapanese(page);
    await expect(page.getByRole('heading', { name: 'ログイン' })).toBeVisible();

    // Reload
    await page.reload();
    await page.waitForLoadState('networkidle');

    // Should still be in Japanese
    await expect(page.getByRole('heading', { name: 'ログイン' })).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId('language-switcher')).toHaveText('EN'); // Shows "EN" when in Japanese mode
  });

  test('ログイン後も選択言語が引き継がれる', async ({ page }) => {
    // Select English on login page
    await switchToEnglish(page);
    await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible();

    // Login
    await page.getByTestId('login-username-input').fill('admin');
    await page.getByTestId('login-password-input').fill('admin123');
    await page.getByTestId('login-submit-button').click();
    await page.waitForURL('/', { timeout: 10000 });
    await page.waitForLoadState('networkidle');

    // Main page should be in English
    await expect(page.getByRole('heading', { name: 'Message Management' })).toBeVisible({
      timeout: 10000,
    });
    await expect(page.getByTestId('language-switcher')).toHaveText('日'); // Shows "日" when in English mode
  });

  test('ログアウト後も選択言語が引き継がれる', async ({ page }) => {
    // Login first
    await login(page);
    await page.waitForLoadState('networkidle');

    // Switch to English on main page
    await switchToEnglish(page);
    await expect(page.getByRole('heading', { name: 'Message Management' })).toBeVisible();

    // Logout
    const logoutButton = page.getByRole('button', { name: /Logout/i });
    await expect(logoutButton).toBeVisible();
    await logoutButton.click();
    await page.waitForURL('/login', { timeout: 10000 });
    await page.waitForLoadState('networkidle');

    // Login page should still be in English
    await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId('language-switcher')).toHaveText('日'); // Shows "日" when in English mode
  });

  test('初回アクセス時のデフォルト言語は日本語である', async ({ page }) => {
    // localStorage is already cleared in beforeEach (clearLocale)
    // Default should be Japanese
    await expect(page.getByRole('heading', { name: 'ログイン' })).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId('language-switcher')).toHaveText('EN'); // Shows "EN" when in Japanese mode
  });

  test('言語設定がlocalStorageに保存される', async ({ page }) => {
    await switchToEnglish(page);

    // Verify localStorage
    const locale = await page.evaluate(() => localStorage.getItem('locale'));
    expect(locale).toBe('en');
  });
});
