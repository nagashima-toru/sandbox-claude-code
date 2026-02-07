'use client';

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import MessageForm from './MessageForm';
import { MessageFormData } from '@/lib/validations/message';
import { MessageResponse } from '@/lib/api/generated/models';

interface MessageModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: MessageFormData) => void;
  initialData?: MessageResponse;
  isSubmitting?: boolean;
  mode: 'create' | 'edit';
  error?: unknown;
}

export default function MessageModal({
  open,
  onOpenChange,
  onSubmit,
  initialData,
  isSubmitting = false,
  mode,
  error,
}: MessageModalProps) {
  const handleCancel = () => {
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]" data-testid="message-modal">
        <DialogHeader>
          <DialogTitle>{mode === 'create' ? 'Create New Message' : 'Edit Message'}</DialogTitle>
          <DialogDescription>
            {mode === 'create'
              ? 'Fill in the details to create a new message.'
              : 'Update the message details below.'}
          </DialogDescription>
        </DialogHeader>
        <MessageForm
          onSubmit={onSubmit}
          initialData={initialData}
          isSubmitting={isSubmitting}
          onCancel={handleCancel}
          error={error}
        />
      </DialogContent>
    </Dialog>
  );
}
