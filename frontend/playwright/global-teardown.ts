import { execSync } from 'child_process';
import { FullConfig } from '@playwright/test';

async function globalTeardown(_config: FullConfig) {
  console.log('🛑 Global teardown: Stopping backend services...');

  // Skip backend teardown in CI (GitHub Actions manages it with Docker Compose)
  if (process.env.CI === 'true') {
    console.log('ℹ️  CI environment detected, skipping backend teardown (managed by workflow)');
    return;
  }

  try {
    execSync('bash ../scripts/stop-backend-e2e.sh', {
      stdio: 'inherit',
      cwd: process.cwd(),
    });

    console.log('✅ Backend services stopped successfully');
  } catch (error) {
    console.error('❌ Failed to stop backend services:', error);
    // Don't throw to allow cleanup to continue
  }
}

export default globalTeardown;
