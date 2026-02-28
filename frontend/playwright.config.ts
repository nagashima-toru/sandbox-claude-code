import { defineConfig, devices } from '@playwright/test';

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 2 : undefined,
  reporter: 'html',

  // Test timeout: 45s (reduced from 60s for faster failure detection)
  timeout: 45000,

  // Expect timeout: 10s (reduced from 15s)
  expect: {
    timeout: 10000,
  },

  // Global setup/teardown for backend services
  globalSetup: require.resolve('./playwright/global-setup.ts'),
  globalTeardown: require.resolve('./playwright/global-teardown.ts'),

  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    // Disable video recording in CI to reduce artifact size and improve speed
    video: 'off',

    // Action timeout: 20s (reduced from 30s)
    actionTimeout: 20000,

    // Navigation timeout: 20s (reduced from 30s)
    navigationTimeout: 20000,
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
    command: 'NEXT_PUBLIC_API_URL=http://localhost:8081 pnpm dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 120000, // Wait up to 2 minutes for dev server to start
    stdout: 'pipe', // Show dev server output for debugging
    stderr: 'pipe',
  },
});
