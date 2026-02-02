# Storybook Development

Start Storybook for component development and testing.

## Commands

```bash
# Start development server
cd frontend && pnpm storybook

# Build for production
cd frontend && pnpm build-storybook
```

Access at http://localhost:6006

## Creating a New Story

1. Create `ComponentName.stories.tsx` next to your component
2. Define meta and stories:

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

See [docs/STORYBOOK.md](../docs/STORYBOOK.md) for detailed documentation.
