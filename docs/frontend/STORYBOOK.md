# Storybook Guide

This document provides detailed guidance for Storybook development, configuration, and best practices.

## Table of Contents

- [Overview](#overview)
- [Commands](#commands)
- [Configuration](#configuration)
- [Story File Structure](#story-file-structure)
- [Story Placement Rules](#story-placement-rules)
- [MSW Integration](#msw-integration)
- [Addons](#addons)
- [Best Practices](#best-practices)
- [Workflow](#workflow)
- [Component Coverage](#component-coverage)
- [Troubleshooting](#troubleshooting)
- [Resources](#resources)

## Overview

Storybook provides an isolated development environment for building, testing, and documenting UI components
independently from the main application. This project uses Storybook 10+ with Next.js integration and MSW for API
mocking.

**Benefits**:

- Develop components in isolation
- Visual testing and documentation
- Interactive component playground
- Accessibility testing with addon-a11y
- API mocking with MSW
- Theme switching (light/dark)
- Automatic documentation generation

## Commands

```bash
# Start Storybook development server
pnpm storybook

# Build Storybook for production (static site)
pnpm build-storybook
```

Access Storybook at [http://localhost:6006](http://localhost:6006)

## Configuration

Storybook is configured in `.storybook/` directory.

### `.storybook/main.ts`

```typescript
import type { StorybookConfig } from '@storybook/nextjs-vite';

const config: StorybookConfig = {
  stories: ['../src/**/*.mdx', '../src/**/*.stories.@(js|jsx|mjs|ts|tsx)'],
  addons: [
    '@chromatic-com/storybook',
    '@storybook/addon-vitest',
    '@storybook/addon-a11y',
    '@storybook/addon-themes',
    '@storybook/addon-docs',
    '@storybook/addon-onboarding',
  ],
  framework: '@storybook/nextjs-vite',
  staticDirs: ['../public'],
  typescript: {
    reactDocgen: 'react-docgen-typescript',
  },
};
export default config;
```

**Key Configuration Points**:

- **stories**: Glob patterns to find story files (`*.stories.tsx`)
- **framework**: Uses `@storybook/nextjs-vite` for Next.js + Vite integration
- **staticDirs**: Serves `public/` folder for static assets
- **typescript.reactDocgen**: Auto-generates prop documentation from TypeScript types

### `.storybook/preview.tsx`

```typescript
import type { Preview } from '@storybook/nextjs-vite';
import { withThemeByClassName } from '@storybook/addon-themes';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { initialize, mswLoader } from 'msw-storybook-addon';
import { handlers } from '../src/mocks/handlers';
import '../src/app/globals.css';

// Initialize MSW
initialize({
  onUnhandledRequest: 'bypass',
});

// Create React Query client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
      refetchOnWindowFocus: false,
    },
  },
});

const preview: Preview = {
  parameters: {
    nextjs: {
      appDirectory: true,
    },
    a11y: {
      test: 'todo',
    },
    msw: {
      handlers,
    },
  },
  decorators: [
    withThemeByClassName({
      themes: {
        light: '',
        dark: 'dark',
      },
      defaultTheme: 'light',
    }),
    (Story) => (
      <QueryClientProvider client={queryClient}>
        <Story />
      </QueryClientProvider>
    ),
  ],
  loaders: [mswLoader],
};

export default preview;
```

**Key Configuration Points**:

- **MSW Initialization**: Mocks API requests in stories
- **React Query Provider**: Wraps all stories with QueryClientProvider
- **Theme Decorator**: Enables light/dark theme switching
- **Global Styles**: Imports Tailwind CSS
- **App Directory Support**: Configures Next.js App Router compatibility

## Story File Structure

Stories follow this naming convention and structure:

```typescript
// src/components/ui/button.stories.tsx
import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import { Button } from './button';

// Meta configuration
const meta = {
  title: 'UI/Button', // Story hierarchy in sidebar
  component: Button, // Component to render
  parameters: {
    layout: 'centered', // Layout: 'centered', 'fullscreen', 'padded'
  },
  tags: ['autodocs'], // Generate documentation automatically
  argTypes: {
    variant: {
      control: 'select',
      options: ['default', 'destructive', 'outline', 'secondary', 'ghost', 'link'],
    },
    size: {
      control: 'select',
      options: ['default', 'sm', 'lg', 'icon'],
    },
  },
} satisfies Meta<typeof Button>;

export default meta;
type Story = StoryObj<typeof meta>;

// Individual stories
export const Default: Story = {
  args: {
    children: 'Button',
  },
};

export const Destructive: Story = {
  args: {
    variant: 'destructive',
    children: 'Delete',
  },
};
```

## Story Placement Rules

Stories are co-located with their components:

```
src/components/
├── ui/
│   ├── button.tsx
│   ├── button.stories.tsx       # Story for button
│   ├── dialog.tsx
│   └── dialog.stories.tsx       # Story for dialog
│
├── common/
│   ├── Loading.tsx
│   └── Loading.stories.tsx      # Story for Loading
│
└── messages/
    ├── MessageTable.tsx
    └── MessageTable.stories.tsx # Story for MessageTable
```

### Naming Convention

- Component: `ComponentName.tsx` (PascalCase)
- Story: `ComponentName.stories.tsx` (PascalCase with `.stories.tsx` suffix)

### Story Titles (sidebar hierarchy)

- UI components: `UI/ComponentName` (e.g., `UI/Button`)
- Common components: `Common/ComponentName` (e.g., `Common/Loading`)
- Message components: `Messages/ComponentName` (e.g., `Messages/MessageTable`)

## MSW Integration

Mock Service Worker (MSW) allows stories to mock API requests without running the backend.

### Setting Up MSW Handlers

```typescript
// src/mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  // Mock GET /api/messages
  http.get('http://localhost:8080/api/messages', () => {
    return HttpResponse.json([
      { id: 1, code: 'MSG001', content: 'Test message 1' },
      { id: 2, code: 'MSG002', content: 'Test message 2' },
    ]);
  }),

  // Mock POST /api/messages
  http.post('http://localhost:8080/api/messages', async ({ request }) => {
    const body = await request.json();
    return HttpResponse.json({
      id: 999,
      ...body,
    });
  }),
];
```

### Using MSW in Stories

```typescript
// src/components/messages/MessageTable.stories.tsx
import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import { http, HttpResponse } from 'msw';
import { MessageTable } from './MessageTable';

const meta = {
  title: 'Messages/MessageTable',
  component: MessageTable,
  parameters: {
    layout: 'fullscreen',
  },
} satisfies Meta<typeof MessageTable>;

export default meta;
type Story = StoryObj<typeof meta>;

// Default story (uses global handlers from src/mocks/handlers.ts)
export const Default: Story = {};

// Override MSW handlers for specific story
export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [
        http.get('http://localhost:8080/api/messages', async () => {
          await new Promise((resolve) => setTimeout(resolve, 3000));
          return HttpResponse.json([]);
        }),
      ],
    },
  },
};

// Error state
export const ErrorState: Story = {
  parameters: {
    msw: {
      handlers: [
        http.get('http://localhost:8080/api/messages', () => {
          return HttpResponse.json(
            { message: 'Internal Server Error' },
            { status: 500 }
          );
        }),
      ],
    },
  },
};
```

### MSW Handler Options

- **Global Handlers**: Defined in `src/mocks/handlers.ts`, applied to all stories
- **Story-specific Handlers**: Override global handlers per story via `parameters.msw.handlers`
- **Response Delay**: Use `await new Promise()` to simulate slow APIs
- **Error States**: Return error status codes to test error handling

## Addons

### Accessibility Testing (addon-a11y)

Automatically tests components for accessibility issues:

```typescript
const meta = {
  title: 'UI/Button',
  component: Button,
  parameters: {
    a11y: {
      test: 'todo', // 'todo', 'error', or 'off'
    },
  },
} satisfies Meta<typeof Button>;
```

- **`test: 'todo'`**: Show violations in Storybook UI (default)
- **`test: 'error'`**: Fail CI on violations
- **`test: 'off'`**: Skip a11y checks

**Common A11y Checks**:

- Color contrast ratios
- ARIA labels
- Keyboard navigation
- Form labels
- Semantic HTML

### Theme Switching (addon-themes)

Switch between light and dark themes:

- Configured in `.storybook/preview.tsx` with `withThemeByClassName`
- Uses Tailwind's `dark:` class strategy
- Theme toggle appears in Storybook toolbar

### Auto-generated Documentation (addon-docs)

```typescript
const meta = {
  title: 'UI/Button',
  component: Button,
  tags: ['autodocs'], // Enable automatic documentation
  argTypes: {
    variant: {
      description: 'Button visual variant',
      control: 'select',
      options: ['default', 'destructive', 'outline', 'secondary', 'ghost', 'link'],
    },
  },
} satisfies Meta<typeof Button>;
```

**Generated Documentation Includes**:

- Component description (from JSDoc comments)
- Prop types table (from TypeScript types)
- Interactive controls (from argTypes)
- Stories with code snippets

## Best Practices

### 1. Co-locate Stories with Components

```
✅ Good: src/components/ui/button.tsx + button.stories.tsx
❌ Bad:  src/components/ui/button.tsx + stories/button.stories.tsx
```

### 2. Use Descriptive Story Names

```typescript
✅ Good: export const DisabledState: Story = { ... }
❌ Bad:  export const Story1: Story = { ... }
```

### 3. Use Args, Not Hardcoded Props

```typescript
✅ Good:
export const Primary: Story = {
  args: {
    variant: 'default',
    children: 'Click me',
  },
};

❌ Bad:
export const Primary: Story = {
  render: () => <Button variant="default">Click me</Button>,
};
```

### 4. Mock API Responses with MSW

```typescript
✅ Good: Use MSW handlers to mock API calls
❌ Bad:  Hardcode data directly in component stories
```

### 5. Test Different States

Create stories for:

- Default state
- Loading state
- Error state
- Empty state
- Disabled state
- Edge cases (long text, no data, etc.)

### 6. Use Tags for Documentation

```typescript
const meta = {
  tags: ['autodocs'], // Generate docs automatically
} satisfies Meta<typeof Component>;
```

## Workflow

### Daily Development with Storybook

1. **Start Storybook**: `pnpm storybook` (port 6006)
2. **Create Component**: Add new component in `src/components/`
3. **Create Story**: Add `.stories.tsx` file next to component
4. **Develop in Isolation**: Use Storybook UI to develop and test
5. **Test Accessibility**: Check a11y panel for violations
6. **Test Themes**: Switch between light/dark themes
7. **Document**: Add JSDoc comments and argTypes for better docs

### Creating a New Story

1. **Create Story File**:

   ```bash
   touch src/components/ui/new-component.stories.tsx
   ```

2. **Define Meta and Stories**:

   ```typescript
   import type { Meta, StoryObj } from '@storybook/nextjs-vite';
   import { NewComponent } from './new-component';

   const meta = {
     title: 'UI/NewComponent',
     component: NewComponent,
     tags: ['autodocs'],
   } satisfies Meta<typeof NewComponent>;

   export default meta;
   type Story = StoryObj<typeof meta>;

   export const Default: Story = {
     args: {
       // component props
     },
   };
   ```

3. **View in Storybook**: Navigate to `UI/NewComponent` in sidebar

### Testing with MSW

1. **Define Handlers**: Add handlers to `src/mocks/handlers.ts`
2. **Use in Story**: Override via `parameters.msw.handlers` if needed
3. **Test Different Responses**: Success, error, loading, empty

## Component Coverage

This project has 17 Storybook stories covering:

### UI Components (8)

1. Badge (`src/components/ui/badge.stories.tsx`)
2. Button (`src/components/ui/button.stories.tsx`)
3. Card (`src/components/ui/card.stories.tsx`)
4. Dialog (`src/components/ui/dialog.stories.tsx`)
5. Form (`src/components/ui/form.stories.tsx`)
6. Input (`src/components/ui/input.stories.tsx`)
7. Label (`src/components/ui/label.stories.tsx`)
8. Table (`src/components/ui/table.stories.tsx`)

### Common Components (3)

9. ErrorMessage (`src/components/common/ErrorMessage.stories.tsx`)
10. Loading (`src/components/common/Loading.stories.tsx`)
11. PageHeader (`src/components/common/PageHeader.stories.tsx`)

### Message Components (6)

12. DeleteConfirmDialog (`src/components/messages/DeleteConfirmDialog.stories.tsx`)
13. MessageForm (`src/components/messages/MessageForm.stories.tsx`)
14. MessageModal (`src/components/messages/MessageModal.stories.tsx`)
15. MessageTable (`src/components/messages/MessageTable.stories.tsx`)
16. Pagination (`src/components/messages/Pagination.stories.tsx`)
17. SearchBar (`src/components/messages/SearchBar.stories.tsx`)

## Troubleshooting

### Storybook Won't Start

```bash
# Clear Storybook cache
pnpm storybook --no-manager-cache

# Reinstall dependencies
rm -rf node_modules pnpm-lock.yaml
pnpm install
```

### MSW Not Working

- Check `src/mocks/handlers.ts` exists
- Verify `initialize()` is called in `.storybook/preview.tsx`
- Check browser console for MSW errors
- Ensure API URL matches handler URLs

### Stories Not Showing

- Check file naming: `*.stories.tsx`
- Verify `stories` glob pattern in `.storybook/main.ts`
- Restart Storybook: `Ctrl+C` and `pnpm storybook`

### Type Errors in Stories

- Ensure `@storybook/nextjs-vite` types are installed
- Check `Meta<typeof Component>` matches component type
- Verify `args` match component props

## Resources

- [Storybook Documentation](https://storybook.js.org/docs)
- [Next.js Integration](https://storybook.js.org/docs/nextjs/get-started/install)
- [MSW Documentation](https://mswjs.io/docs/)
- [Addon A11y](https://storybook.js.org/addons/@storybook/addon-a11y)
