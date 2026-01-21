# Frontend - Message Management Application

[![Frontend CI](https://github.com/nagashima-toru/sandbox-claude-code/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/nagashima-toru/sandbox-claude-code/actions/workflows/frontend-ci.yml)
[![Coverage: Statements](https://img.shields.io/badge/Coverage-Statements%20%E2%89%A5%2080%25-brightgreen)](https://github.com/nagashima-toru/sandbox-claude-code/actions/workflows/frontend-ci.yml)
[![Coverage: Branches](https://img.shields.io/badge/Coverage-Branches%20%E2%89%A5%2070%25-brightgreen)](https://github.com/nagashima-toru/sandbox-claude-code/actions/workflows/frontend-ci.yml)

Next.js-based message management application with full CRUD functionality.

## Quick Start

### Prerequisites

- Node.js 20+ (recommended: use volta or nvm)
- pnpm (package manager)
- Backend API running on port 8080

### Installation

```bash
# Install dependencies
pnpm install

# Copy environment file
cp .env.local.example .env.local

# Generate API client from OpenAPI spec
pnpm generate:api

# Start development server
pnpm dev
```

Visit [http://localhost:3000](http://localhost:3000)

## Scripts

```bash
pnpm dev              # Start development server
pnpm build            # Build for production
pnpm start            # Start production server
pnpm lint             # Run ESLint
pnpm type-check       # Run TypeScript type checking
pnpm generate:api     # Generate API client from OpenAPI spec
pnpm test             # Run unit & component tests
pnpm test:ui          # Run tests with UI
pnpm test:e2e         # Run E2E tests
pnpm test:e2e:ui      # Run E2E tests with UI
pnpm storybook        # Start Storybook development server
pnpm build-storybook  # Build Storybook for production
```

## Tech Stack

- **Framework**: Next.js 15+ (App Router)
- **Language**: TypeScript
- **UI Library**: shadcn/ui
- **Styling**: Tailwind CSS
- **API Code Generation**: Orval
- **HTTP Client**: axios
- **State Management**: TanStack Query (React Query)
- **Form Management**: React Hook Form + Zod
- **Testing**: Vitest + Testing Library + Playwright
- **Component Development**: Storybook 10+ with MSW integration

## API Code Generation

This project uses Orval to automatically generate TypeScript API client code from the backend's OpenAPI specification.

### Generate API Client

```bash
# Make sure backend is generating OpenAPI spec
cd ../backend
./mvnw verify

# Generate frontend API client
cd ../frontend
pnpm generate:api
```

Generated files will be in `src/lib/api/generated/` (DO NOT EDIT manually).

## Storybook

### Overview

This project uses Storybook for component development, documentation, and visual testing. Storybook provides an isolated environment to develop and test UI components without running the full application.

### Getting Started

```bash
# Start Storybook development server (runs on http://localhost:6006)
pnpm storybook

# Build Storybook for production
pnpm build-storybook
```

Visit [http://localhost:6006](http://localhost:6006) to view the Storybook UI.

### Creating Stories

Stories are located alongside their components with the `.stories.tsx` extension:

```typescript
// src/components/ui/button.stories.tsx
import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import { Button } from './button';

const meta = {
  title: 'UI/Button',
  component: Button,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
  argTypes: {
    variant: {
      control: 'select',
      options: ['default', 'destructive', 'outline', 'secondary', 'ghost', 'link'],
    },
  },
} satisfies Meta<typeof Button>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    children: 'Button',
  },
};
```

### Story Organization

Stories are organized by component type:

- **UI Components** (`src/components/ui/*.stories.tsx`): shadcn/ui base components
- **Common Components** (`src/components/common/*.stories.tsx`): Shared application components
- **Message Components** (`src/components/messages/*.stories.tsx`): Message-specific components

### Component List

This project includes 17 Storybook stories:

**UI Components (8)**

1. Badge
2. Button
3. Card
4. Dialog
5. Form
6. Input
7. Label
8. Table

**Common Components (3)** 9. ErrorMessage 10. Loading 11. PageHeader

**Message Components (6)** 12. DeleteConfirmDialog 13. MessageForm 14. MessageModal 15. MessageTable 16. Pagination 17. SearchBar

### Features

- **Auto-generated Documentation**: Stories with `tags: ['autodocs']` generate documentation automatically
- **MSW Integration**: Mock Service Worker (MSW) for API mocking in stories
- **React Query Support**: Stories can use React Query hooks with mocked API responses
- **Accessibility Testing**: Built-in a11y addon for accessibility checks
- **Theme Switching**: Light/dark theme toggle support
- **Interactive Controls**: Modify component props in real-time

### Tech Stack

- **Storybook**: 10.1.11
- **Framework Integration**: @storybook/nextjs-vite
- **Addons**:
  - @chromatic-com/storybook: Visual regression testing
  - @storybook/addon-vitest: Vitest integration
  - @storybook/addon-a11y: Accessibility testing
  - @storybook/addon-themes: Theme switching
  - @storybook/addon-docs: Documentation generation
  - msw-storybook-addon: MSW integration for API mocking

### Best Practices

1. **Colocation**: Place `.stories.tsx` files next to their components
2. **Naming**: Use descriptive story names (Default, WithIcon, Disabled, etc.)
3. **Args**: Use args for dynamic props instead of hardcoding
4. **Parameters**: Configure layout, viewport, and other settings per story
5. **MSW Handlers**: Mock API responses for components that fetch data
6. **Accessibility**: Use addon-a11y to catch a11y issues early

## Project Structure

```
frontend/
├── src/
│   ├── app/                    # Next.js App Router
│   ├── components/             # React components
│   │   ├── ui/                 # shadcn/ui components
│   │   ├── messages/           # Message-specific components
│   │   └── common/             # Common components
│   ├── lib/                    # Utility functions
│   │   ├── api/
│   │   │   ├── generated/      # Orval generated (DO NOT EDIT)
│   │   │   └── client.ts       # Axios instance
│   │   ├── validations/        # Zod schemas
│   │   └── utils.ts
│   ├── hooks/                  # Custom React hooks
│   └── types/                  # TypeScript types
├── tests/                      # Test files
├── public/                     # Static files
├── orval.config.ts             # Orval configuration
├── tailwind.config.ts
├── tsconfig.json
└── package.json
```

## Development Workflow

1. **Backend changes** → Regenerate OpenAPI: `cd backend && ./mvnw verify`
2. **Frontend updates** → Regenerate client: `cd frontend && pnpm generate:api`
3. **Type safety** → TypeScript will catch API contract changes

## Environment Variables

Create `.env.local`:

```bash
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

## Documentation

For detailed documentation, see [CLAUDE.md](./CLAUDE.md).

## Features

- Message CRUD operations (Create, Read, Update, Delete)
- Search and filter messages
- Sort by ID, Code, Content
- Pagination
- Responsive design (mobile-friendly)
- Form validation with real-time feedback
- Error handling

## Code Quality

### Linting and Formatting

```bash
# Run ESLint
pnpm lint

# Run TypeScript type checking
pnpm type-check

# Format code with Prettier
pnpm prettier --write "src/**/*.{ts,tsx}"
```

### Testing

```bash
# Run unit and component tests
pnpm test

# Run E2E tests
pnpm test:e2e

# Generate coverage report
pnpm test:coverage
```

**Coverage Thresholds:**

- Statements: ≥ 80%
- Branches: ≥ 70%
- Functions: ≥ 80%
- Lines: ≥ 80%

### Performance

- **Optimized Re-renders**: Uses `useMemo` and `useCallback` to prevent unnecessary re-renders
- **Debounced Search**: 300ms delay to reduce API calls during search
- **Code Splitting**: Automatic code splitting via Next.js
- **Bundle Size**: ~182 kB First Load JS (optimized)

### Browser Compatibility

Tested and compatible with:

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## License

MIT
