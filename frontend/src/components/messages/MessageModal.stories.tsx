import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import MessageModal from './MessageModal';
import { MessageResponse } from '@/lib/api/generated/models';
import { useState } from 'react';

const meta = {
  title: 'Messages/MessageModal',
  component: MessageModal,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof MessageModal>;

export default meta;
type Story = StoryObj<typeof meta>;

const mockMessage: MessageResponse = {
  id: 1,
  code: 'TEST001',
  content: 'This is a test message for editing.',
  createdAt: '2026-01-29T00:00:00Z',
  updatedAt: '2026-01-29T00:00:00Z',
};

export const CreateMode: Story = {
  args: {
    open: true,
    onOpenChange: () => {},
    onSubmit: (data) => console.log('Submit:', data),
    mode: 'create',
    isSubmitting: false,
  },
  render: (args) => {
    const [open, setOpen] = useState(args.open);
    return (
      <MessageModal
        {...args}
        open={open}
        onOpenChange={setOpen}
        onSubmit={(data) => {
          console.log('Submit:', data);
          setOpen(false);
        }}
      />
    );
  },
};

export const EditMode: Story = {
  args: {
    open: true,
    onOpenChange: () => {},
    onSubmit: (data) => console.log('Submit:', data),
    mode: 'edit',
    initialData: mockMessage,
    isSubmitting: false,
  },
  render: (args) => {
    const [open, setOpen] = useState(args.open);
    return (
      <MessageModal
        {...args}
        open={open}
        onOpenChange={setOpen}
        onSubmit={(data) => {
          console.log('Submit:', data);
          setOpen(false);
        }}
      />
    );
  },
};

export const Submitting: Story = {
  args: {
    open: true,
    onOpenChange: () => {},
    onSubmit: (data) => console.log('Submit:', data),
    mode: 'create',
    isSubmitting: true,
  },
  render: (args) => {
    const [open, setOpen] = useState(args.open);
    return <MessageModal {...args} open={open} onOpenChange={setOpen} />;
  },
};
