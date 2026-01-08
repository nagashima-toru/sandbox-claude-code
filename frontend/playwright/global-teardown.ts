import { execSync } from 'child_process';
import { FullConfig } from '@playwright/test';

async function globalTeardown(config: FullConfig) {
  console.log('🛑 Global teardown: Stopping backend services...');

  try {
    execSync('bash ./scripts/stop-backend-e2e.sh', {
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
