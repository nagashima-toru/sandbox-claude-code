import { defineConfig, devices } from '@playwright/test';

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',

  // Increase test timeout from default 30s to 60s
  timeout: 60000,

  // Increase expect timeout from default 5s to 15s
  expect: {
    timeout: 15000,
  },

  // Global setup/teardown for backend services
  globalSetup: require.resolve('./playwright/global-setup.ts'),
  globalTeardown: require.resolve('./playwright/global-teardown.ts'),

  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',

    // Increase action timeout from default 0 (no timeout) to 30s
    actionTimeout: 30000,

    // Increase navigation timeout to 30s
    navigationTimeout: 30000,
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },

    // Only run chromium in CI to speed up tests
    // Uncomment below for local cross-browser testing
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },
    //
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },
    //
    // // Mobile viewports
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] },
    // },
  ],

  // Run local dev server before starting tests
  webServer: {
    command: 'pnpm dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 120000, // Wait up to 2 minutes for dev server to start
    stdout: 'pipe', // Show dev server output for debugging
    stderr: 'pipe',
  },
});
