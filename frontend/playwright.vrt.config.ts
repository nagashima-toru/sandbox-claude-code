import { defineConfig, devices } from '@playwright/test';

const STORYBOOK_PORT = 6007;

/**
 * Visual regression tests for screen-level stories (tag: 'screen').
 * Baselines are the approved design from the Spec PR (ADR-0002).
 * Canonical environment: Playwright Docker image in CI (.github/workflows/frontend-vrt.yml).
 */
export default defineConfig({
  testDir: './tests/vrt',
  // No {platform} suffix: baselines are generated only in the CI container
  snapshotPathTemplate: '{testDir}/__screenshots__/{arg}{ext}',
  forbidOnly: !!process.env.CI,
  retries: 0,
  reporter: process.env.CI ? [['html', { open: 'never' }], ['github']] : 'list',
  timeout: 120000,
  expect: {
    toHaveScreenshot: {
      animations: 'disabled',
      caret: 'hide',
      maxDiffPixelRatio: 0.001,
    },
  },
  use: {
    baseURL: `http://localhost:${STORYBOOK_PORT}`,
    ...devices['Desktop Chrome'],
    viewport: { width: 1280, height: 800 },
    deviceScaleFactor: 1,
    locale: 'ja-JP',
    timezoneId: 'Asia/Tokyo',
  },
  webServer: {
    command: `pnpm exec storybook dev --ci --no-open -p ${STORYBOOK_PORT}`,
    url: `http://localhost:${STORYBOOK_PORT}/index.json`,
    reuseExistingServer: !process.env.CI,
    timeout: 180000,
  },
});
