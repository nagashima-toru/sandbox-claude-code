# Frontend Performance Monitoring

This document describes the frontend performance monitoring setup for continuous tracking of bundle sizes and performance metrics.

## Overview

The frontend performance monitoring system automatically analyzes Next.js bundle sizes on every pull request, helping detect performance regressions early.

## Features

### 1. Bundle Size Analysis

- **Tool**: `@next/bundle-analyzer`
- **Trigger**: Automatically runs on pull requests affecting frontend code
- **Output**: Interactive HTML reports showing bundle composition

#### What It Monitors

- Total build size (.next directory)
- Individual page bundle sizes
- Shared chunk sizes
- Largest files in the build
- Dependencies contributing to bundle size

### 2. Automated Reporting

The GitHub Actions workflow generates reports in multiple formats:

- **GitHub Step Summary**: Quick overview of build size and largest files
- **PR Comments**: Detailed bundle size report with recommendations
- **Artifacts**: Downloadable HTML reports for detailed analysis

### 3. Threshold Warnings

The system warns when bundle size exceeds predefined thresholds:

- **Total build size threshold**: 50MB
- **Warning**: Displays in workflow output and GitHub annotations

## Usage

### Local Bundle Analysis

To analyze bundle sizes locally:

```bash
cd frontend
pnpm build:analyze
```

This command:
1. Builds the Next.js application with bundle analyzer enabled
2. Generates HTML reports in `frontend/.next/analyze/`
3. Opens interactive visualizations showing:
   - Client-side bundle composition
   - Server-side bundle composition
   - Treemap of all modules

### Interpreting Reports

#### Bundle Analyzer Report

The interactive HTML reports show:

- **Module sizes**: How much each dependency contributes
- **Parsed size**: Actual size of the file after parsing
- **Gzipped size**: Size after gzip compression (most relevant for production)

#### PR Comments

Each PR receives an automated comment with:

- Total build size
- List of largest files
- Performance recommendations
- Instructions for local analysis

### GitHub Actions Workflow

The workflow (`frontend-performance.yml`) runs on:
- Pull requests that modify frontend code
- Pull requests that modify the workflow itself

**Workflow steps:**

1. Setup environment (pnpm, Node.js)
2. Install dependencies
3. Generate API client from OpenAPI spec
4. Build with bundle analyzer enabled
5. Upload analysis reports as artifacts
6. Generate summary in GitHub Step Summary
7. Post detailed report as PR comment
8. Check bundle size against thresholds

## Performance Optimization Tips

### 1. Identify Large Dependencies

Use the bundle analyzer to find large dependencies:

```bash
cd frontend
pnpm build:analyze
```

Look for:
- Dependencies larger than 100KB
- Unused portions of large libraries
- Duplicate dependencies

### 2. Code Splitting

Split large pages into smaller chunks:

```typescript
// Dynamic imports for heavy components
const HeavyComponent = dynamic(() => import('./HeavyComponent'), {
  loading: () => <Loading />,
});
```

### 3. Optimize Images

- Use Next.js Image component for automatic optimization
- Use WebP format where supported
- Implement lazy loading for below-the-fold images

### 4. Tree Shaking

Ensure tree shaking works by:
- Using ES modules (import/export)
- Importing only what you need:
  ```typescript
  // Good
  import { Button } from '@/components/ui/button';

  // Bad (imports entire library)
  import * as UI from '@/components/ui';
  ```

### 5. Remove Unused Dependencies

Regularly audit and remove unused dependencies:

```bash
cd frontend
pnpm audit
```

## Thresholds

Current thresholds:

| Metric | Threshold | Action |
|--------|-----------|--------|
| Total build size | 50MB | Warning (does not fail build) |

### Updating Thresholds

To modify thresholds, edit `.github/workflows/frontend-performance.yml`:

```yaml
- name: Check bundle size thresholds
  run: |
    THRESHOLD_MB=50  # Change this value
```

## Troubleshooting

### Bundle Analyzer Not Running

If bundle analysis doesn't run:

1. Check that the workflow triggers on your PR:
   ```yaml
   paths:
     - 'frontend/**'
   ```

2. Verify `@next/bundle-analyzer` is installed:
   ```bash
   cd frontend
   pnpm list @next/bundle-analyzer
   ```

3. Ensure `next.config.js` includes the bundle analyzer configuration

### Reports Not Generated

If HTML reports aren't generated:

1. Check build succeeded: `pnpm build:analyze`
2. Verify environment variable: `ANALYZE=true`
3. Check for errors in workflow logs

### Large Bundle Size

If bundle size is unexpectedly large:

1. Run local analysis to identify culprits
2. Check for:
   - Dev dependencies in production build
   - Unoptimized images in public folder
   - Large unused dependencies
   - Missing tree shaking

## Future Enhancements

Potential improvements to consider:

### Lighthouse CI (Optional)

Add Lighthouse CI for comprehensive performance metrics:

```yaml
# .github/workflows/lighthouse.yml
- name: Run Lighthouse CI
  run: |
    npm install -g @lhci/cli
    lhci autorun
```

This would measure:
- Performance score
- Accessibility score
- Best practices score
- SEO score

### Bundle Size Comparison

Compare bundle sizes between base and PR branches:

- Show size diff in PR comments
- Fail CI if size increases by >10%
- Track size trends over time

### Performance Budgets

Set strict performance budgets:

```javascript
// next.config.js
module.exports = {
  experimental: {
    performanceBudgets: {
      totalSize: 500000, // 500KB
      maxInitialLoad: 250000, // 250KB
    }
  }
}
```

## References

- [@next/bundle-analyzer Documentation](https://www.npmjs.com/package/@next/bundle-analyzer)
- [Next.js Bundle Analysis](https://nextjs.org/docs/advanced-features/measuring-performance)
- [Web.dev Performance Best Practices](https://web.dev/fast/)
- [Lighthouse CI](https://github.com/GoogleChrome/lighthouse-ci)

## Related Issues

- [#6 - CI/CD: フロントエンド（Next.js）の継続的インテグレーション設定](https://github.com/nagashima-toru/sandbox-claude-code/issues/6)
- [#15 - CI/CD: フロントエンドのパフォーマンスモニタリング (Phase 5)](https://github.com/nagashima-toru/sandbox-claude-code/issues/15)