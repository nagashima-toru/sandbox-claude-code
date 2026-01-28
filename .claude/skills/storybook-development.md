# Storybook Development

## Commands

```bash
cd frontend
pnpm storybook      # Start (port 6006)
pnpm build-storybook # Build static
```

## Story Structure

```typescript
import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import { MyComponent } from './MyComponent';

const meta = {
  title: 'Category/MyComponent',
  component: MyComponent,
  tags: ['autodocs'],
} satisfies Meta<typeof MyComponent>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: { /* props */ },
};
```

## Placement

Co-locate with components:
- `ComponentName.tsx`
- `ComponentName.stories.tsx`

## MSW Mocking

```typescript
export const ErrorState: Story = {
  parameters: {
    msw: {
      handlers: [
        http.get('/api/...', () => HttpResponse.json({}, { status: 500 })),
      ],
    },
  },
};
```

## Story States to Cover

- Default, Loading, Error, Empty, Disabled, Edge cases

See [docs/STORYBOOK.md](../../docs/STORYBOOK.md)
