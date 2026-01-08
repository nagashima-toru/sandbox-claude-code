import { execSync } from 'child_process';
import { chromium, FullConfig } from '@playwright/test';

async function globalSetup(config: FullConfig) {
  console.log('🚀 Global setup: Starting backend services...');

  try {
    // Start backend services
    execSync('bash ./scripts/start-backend-e2e.sh', {
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
        const response = await page.goto('http://localhost:8080/actuator/health', {
          timeout: 5000,
        });

        if (response?.ok()) {
          console.log('✅ Backend health check passed');
          await browser.close();
          return;
        }
      } catch (error) {
        retries++;
        console.log(`⏳ Waiting for backend... (${retries}/${maxRetries})`);
        await new Promise((resolve) => setTimeout(resolve, 2000));
      }
    }

    await browser.close();
    throw new Error('Backend health check failed after max retries');
  } catch (error) {
    console.error('❌ Failed to start backend services:', error);
    throw error;
  }
}

export default globalSetup;
