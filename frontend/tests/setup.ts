import '@testing-library/jest-dom';
import { expect, afterEach, vi } from 'vitest';
import { cleanup } from '@testing-library/react';

// Cleanup after each test
afterEach(() => {
  cleanup();
});

// Mock environment variables
vi.mock('process', () => ({
  env: {
    NEXT_PUBLIC_API_URL: 'http://localhost:8080/api',
  },
}));
