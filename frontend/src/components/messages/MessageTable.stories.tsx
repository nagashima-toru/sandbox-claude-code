import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import MessageTable from './MessageTable';
import { MessageResponse } from '@/lib/api/generated/models';
import { getGetAllMessagesMockHandler } from '@/lib/api/generated/message/message.msw';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { http, HttpResponse, delay } from 'msw';

const meta = {
  title: 'Messages/MessageTable',
  component: MessageTable,
  parameters: {
    layout: 'padded',
  },
  tags: ['autodocs'],
  decorators: [
    (Story) => {
      const queryClient = new QueryClient({
        defaultOptions: {
          queries: {
            retry: false,
          },
        },
      });
      return (
        <QueryClientProvider client={queryClient}>
          <Story />
        </QueryClientProvider>
      );
    },
  ],
} satisfies Meta<typeof MessageTable>;

export default meta;
type Story = StoryObj<typeof meta>;

const mockMessages: MessageResponse[] = [
  {
    id: 1,
    code: 'WELCOME',
    content: 'Welcome to our application!',
  },
  {
    id: 2,
    code: 'ERROR_001',
    content: 'An error occurred while processing your request.',
  },
  {
    id: 3,
    code: 'SUCCESS',
    content: 'Operation completed successfully.',
  },
  {
    id: 4,
    code: 'WARNING',
    content: 'Please review your input before continuing.',
  },
  {
    id: 5,
    code: 'INFO',
    content: 'This is an informational message.',
  },
];

const manyMessages: MessageResponse[] = Array.from({ length: 50 }, (_, i) => ({
  id: i + 1,
  code: `MSG_${String(i + 1).padStart(3, '0')}`,
  content: `This is message number ${i + 1}. Lorem ipsum dolor sit amet, consectetur adipiscing elit.`,
}));

export const Default: Story = {
  args: {
    onEdit: (message) => console.log('Edit:', message),
    onDelete: (message) => console.log('Delete:', message),
  },
  parameters: {
    msw: {
      handlers: [getGetAllMessagesMockHandler(() => mockMessages)],
    },
  },
};

export const Loading: Story = {
  args: {
    onEdit: (message) => console.log('Edit:', message),
    onDelete: (message) => console.log('Delete:', message),
  },
  parameters: {
    msw: {
      handlers: [
        http.get('*/api/messages', async () => {
          await delay(10000);
          return HttpResponse.json(mockMessages);
        }),
      ],
    },
  },
};

export const Empty: Story = {
  args: {
    onEdit: (message) => console.log('Edit:', message),
    onDelete: (message) => console.log('Delete:', message),
  },
  parameters: {
    msw: {
      handlers: [getGetAllMessagesMockHandler(() => [])],
    },
  },
};

export const Error: Story = {
  args: {
    onEdit: (message) => console.log('Edit:', message),
    onDelete: (message) => console.log('Delete:', message),
  },
  parameters: {
    msw: {
      handlers: [
        http.get('*/api/messages', async () => {
          await delay(1000);
          return HttpResponse.json({ message: 'Internal Server Error' }, { status: 500 });
        }),
      ],
    },
  },
};

export const ManyMessages: Story = {
  args: {
    onEdit: (message) => console.log('Edit:', message),
    onDelete: (message) => console.log('Delete:', message),
  },
  parameters: {
    msw: {
      handlers: [getGetAllMessagesMockHandler(() => manyMessages)],
    },
  },
};

export const Mobile: Story = {
  args: {
    onEdit: (message) => console.log('Edit:', message),
    onDelete: (message) => console.log('Delete:', message),
  },
  parameters: {
    viewport: {
      defaultViewport: 'mobile1',
    },
    msw: {
      handlers: [getGetAllMessagesMockHandler(() => mockMessages)],
    },
  },
};
