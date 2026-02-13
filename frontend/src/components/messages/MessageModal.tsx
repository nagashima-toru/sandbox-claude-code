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
import { usePermission } from '@/hooks/usePermission';

interface MessageModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: MessageFormData) => void;
  initialData?: MessageResponse;
  isSubmitting?: boolean;
  mode: 'create' | 'edit';
  error?: unknown;
  isReadOnly?: boolean;
}

export default function MessageModal({
  open,
  onOpenChange,
  onSubmit,
  initialData,
  isSubmitting = false,
  mode,
  error,
  isReadOnly = false,
}: MessageModalProps) {
  const { canCreate } = usePermission();

  const handleCancel = () => {
    onOpenChange(false);
  };

  // VIEWER ロールの場合、作成モードでモーダルを表示しない
  if (mode === 'create' && !canCreate) {
    return null;
  }

  const title =
    mode === 'create' ? 'Create New Message' : isReadOnly ? 'View Message' : 'Edit Message';
  const description =
    mode === 'create'
      ? 'Fill in the details to create a new message.'
      : isReadOnly
        ? 'Message details (read-only)'
        : 'Update the message details below.';

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]" data-testid="message-modal">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>
        <MessageForm
          onSubmit={onSubmit}
          initialData={initialData}
          isSubmitting={isSubmitting}
          onCancel={handleCancel}
          error={error}
          disabled={isReadOnly}
        />
      </DialogContent>
    </Dialog>
  );
}
