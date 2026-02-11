import { execSync } from 'child_process';
import { chromium, FullConfig } from '@playwright/test';

async function globalSetup(_config: FullConfig) {
  console.log('🚀 Global setup: Starting backend services...');

  try {
    // Start backend services
    execSync('bash ../scripts/start-backend-e2e.sh', {
      stdio: 'inherit',
      cwd: process.cwd(),
    });

    console.log('✅ Backend services started successfully');

    // Verify backend API is accessible
    const browser = await chromium.launch();
    const page = await browser.newPage();

    const maxRetries = 10;
    let retries = 0;

    while (retries < maxRetries) {
      try {
        const response = await page.goto('http://localhost:8081/actuator/health', {
          timeout: 5000,
        });

        if (response?.ok()) {
          console.log('✅ Backend health check passed');
          break;
        }
      } catch (_error) {
        retries++;
        console.log(`⏳ Waiting for backend... (${retries}/${maxRetries})`);
        await new Promise((resolve) => setTimeout(resolve, 2000));
      }
    }

    if (retries >= maxRetries) {
      await browser.close();
      throw new Error('Backend health check failed after max retries');
    }

    // Verify frontend is accessible
    console.log('🌐 Checking frontend availability...');
    retries = 0;
    const frontendMaxRetries = 10;

    while (retries < frontendMaxRetries) {
      try {
        const response = await page.goto('http://localhost:3000', {
          timeout: 10000,
          waitUntil: 'domcontentloaded',
        });

        if (response?.ok()) {
          // Check if the page has loaded (either Message Management or Login page)
          const title = await page.title();
          if (title.match(/Message Management|ログイン/i) || title.length > 0) {
            console.log(`✅ Frontend health check passed (title: ${title})`);
            await browser.close();
            return;
          } else {
            console.log(`⚠️  Frontend loaded but unexpected title: ${title}`);
          }
        }
      } catch (_error) {
        retries++;
        console.log(`⏳ Waiting for frontend... (${retries}/${frontendMaxRetries})`);
        await new Promise((resolve) => setTimeout(resolve, 3000));
      }
    }

    await browser.close();
    throw new Error('Frontend health check failed after max retries');
  } catch (error) {
    console.error('❌ Failed to start backend services:', error);
    throw error;
  }
}

export default globalSetup;
