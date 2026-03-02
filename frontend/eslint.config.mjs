// For more info, see https://github.com/storybookjs/eslint-plugin-storybook#configuration-flat-config-format
import storybook from 'eslint-plugin-storybook';
import nextVitals from 'eslint-config-next/core-web-vitals';
import importPlugin from 'eslint-plugin-import';
import playwright from 'eslint-plugin-playwright';

const eslintConfig = [
  {
    ignores: [
      'node_modules/**',
      '.next/**',
      'out/**',
      'dist/**',
      'build/**',
      'coverage/**',
      'storybook-static/**',
      'playwright-report/**',
      'test-results/**',
      '.turbo/**',
      '*.config.js',
      '*.config.mjs',
    ],
  },
  ...nextVitals,
  ...storybook.configs['flat/recommended'],
  {
    files: [
      '**/*.test.{ts,tsx}',
      '**/*.spec.{ts,tsx}',
      '**/tests/**/*.{ts,tsx}',
      'playwright/**/*.{ts,tsx}',
    ],
    rules: {
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-unused-vars': 'off',
    },
  },
  {
    // E2E tests: enforce locale-independent locators
    files: ['tests/e2e/**/*.{ts,tsx}', '**/*.spec.{ts,tsx}'],
    plugins: { playwright },
    rules: {
      // Prefer getByTestId, getByRole, getByLabel over CSS/text selectors
      'playwright/prefer-native-locators': 'error',
    },
  },
  // Enforce named exports in components, hooks, and contexts (excluding stories)
  {
    files: [
      'src/components/**/*.{ts,tsx}',
      'src/hooks/**/*.{ts,tsx}',
      'src/contexts/**/*.{ts,tsx}',
    ],
    ignores: ['**/*.stories.{ts,tsx}'],
    plugins: { import: importPlugin },
    rules: {
      'import/no-default-export': 'error',
    },
  },
];

export default eslintConfig;
