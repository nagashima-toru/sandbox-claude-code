'use client';

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { MessageResponse } from '@/lib/api/generated/models';
import { AlertCircle } from 'lucide-react';
import { getApiErrorMessage } from '@/lib/utils/errorHandling';

interface DeleteConfirmDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: () => void;
  message: MessageResponse | null;
  isDeleting?: boolean;
  error?: unknown;
}

export default function DeleteConfirmDialog({
  open,
  onOpenChange,
  onConfirm,
  message,
  isDeleting = false,
  error,
}: DeleteConfirmDialogProps) {
  const handleCancel = () => {
    onOpenChange(false);
  };

  const errorMessage = getApiErrorMessage(error);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <AlertCircle className="h-5 w-5 text-destructive" />
            Delete Message
          </DialogTitle>
          <DialogDescription>
            Are you sure you want to delete this message? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>

        {errorMessage && (
          <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive border border-destructive/20">
            {errorMessage}
          </div>
        )}

        {message && (
          <div className="rounded-lg border border-muted bg-muted/50 p-4 space-y-2">
            <div>
              <span className="text-sm font-medium text-muted-foreground">Code:</span>
              <div className="mt-1">
                <code className="bg-background px-2 py-1 rounded text-sm">{message.code}</code>
              </div>
            </div>
            <div>
              <span className="text-sm font-medium text-muted-foreground">Content:</span>
              <p className="mt-1 text-sm">{message.content}</p>
            </div>
          </div>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={handleCancel} disabled={isDeleting}>
            Cancel
          </Button>
          <Button variant="destructive" onClick={onConfirm} disabled={isDeleting}>
            {isDeleting ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
