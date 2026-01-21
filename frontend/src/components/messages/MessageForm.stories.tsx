import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import MessageForm from './MessageForm';
import { MessageResponse } from '@/lib/api/generated/models';

const meta = {
  title: 'Messages/MessageForm',
  component: MessageForm,
  parameters: {
    layout: 'padded',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof MessageForm>;

export default meta;
type Story = StoryObj<typeof meta>;

const mockInitialData: MessageResponse = {
  id: 1,
  code: 'TEST001',
  content: 'This is a test message',
};

export const Create: Story = {
  args: {
    onSubmit: (data) => console.log('Submit:', data),
    onCancel: () => console.log('Cancel'),
    isSubmitting: false,
  },
};

export const Edit: Story = {
  args: {
    onSubmit: (data) => console.log('Submit:', data),
    onCancel: () => console.log('Cancel'),
    initialData: mockInitialData,
    isSubmitting: false,
  },
};

export const Submitting: Story = {
  args: {
    onSubmit: (data) => console.log('Submit:', data),
    onCancel: () => console.log('Cancel'),
    initialData: mockInitialData,
    isSubmitting: true,
  },
};

export const WithError: Story = {
  args: {
    onSubmit: (data) => console.log('Submit:', data),
    onCancel: () => console.log('Cancel'),
    initialData: mockInitialData,
    isSubmitting: false,
    error: {
      response: {
        data: {
          message: 'Failed to save message. Please try again.',
        },
      },
    },
  },
};

export const ValidationError: Story = {
  args: {
    onSubmit: (data) => console.log('Submit:', data),
    onCancel: () => console.log('Cancel'),
    isSubmitting: false,
  },
  play: async ({ canvasElement }) => {
    const canvas = canvasElement as HTMLElement;
    const submitButton = canvas.querySelector('button[type="submit"]') as HTMLButtonElement;
    if (submitButton) {
      submitButton.click();
    }
  },
};
