'use client';

import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '@/components/common/PageHeader';
import MessageTable from '@/components/messages/MessageTable';
import MessageModal from '@/components/messages/MessageModal';
import DeleteConfirmDialog from '@/components/messages/DeleteConfirmDialog';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import {
  useCreateMessage,
  useUpdateMessage,
  useDeleteMessage,
} from '@/lib/api/generated/message/message';
import { MessageFormData } from '@/lib/validations/message';
import { MessageResponse } from '@/lib/api/generated/models';

export default function Home() {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [selectedMessage, setSelectedMessage] = useState<MessageResponse | null>(null);
  const queryClient = useQueryClient();

  const createMutation = useCreateMessage({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['/api/messages'] });
        setIsCreateModalOpen(false);
      },
      onError: (error: any) => {
        console.error('Failed to create message:', error);
        if (error?.response?.status === 409) {
          console.error('Duplicate code error');
        }
      },
    },
  });

  const updateMutation = useUpdateMessage({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['/api/messages'] });
        setIsEditModalOpen(false);
        setSelectedMessage(null);
      },
      onError: (error: any) => {
        console.error('Failed to update message:', error);
        if (error?.response?.status === 404) {
          console.error('Message not found');
        }
      },
    },
  });

  const deleteMutation = useDeleteMessage({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['/api/messages'] });
        setIsDeleteDialogOpen(false);
        setSelectedMessage(null);
      },
      onError: (error: any) => {
        console.error('Failed to delete message:', error);
        if (error?.response?.status === 404) {
          console.error('Message not found');
        }
      },
    },
  });

  const handleCreateMessage = (data: MessageFormData) => {
    createMutation.mutate({ data });
  };

  const handleEditMessage = (data: MessageFormData) => {
    if (selectedMessage?.id) {
      updateMutation.mutate({ id: selectedMessage.id, data });
    }
  };

  const handleEditClick = (message: MessageResponse) => {
    updateMutation.reset(); // Clear previous errors
    setSelectedMessage(message);
    setIsEditModalOpen(true);
  };

  const handleDeleteClick = (message: MessageResponse) => {
    deleteMutation.reset(); // Clear previous errors
    setSelectedMessage(message);
    setIsDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = () => {
    if (selectedMessage?.id) {
      deleteMutation.mutate({ id: selectedMessage.id });
    }
  };

  return (
    <main className="min-h-screen p-8">
      <div className="max-w-7xl mx-auto space-y-6">
        <PageHeader
          title="Message Management"
          description="Manage all your messages in one place"
          action={
            <Button onClick={() => {
              createMutation.reset(); // Clear previous errors
              setIsCreateModalOpen(true);
            }}>
              <Plus className="h-4 w-4 mr-2" />
              New Message
            </Button>
          }
        />
        <MessageTable onEdit={handleEditClick} onDelete={handleDeleteClick} />

        <MessageModal
          open={isCreateModalOpen}
          onOpenChange={setIsCreateModalOpen}
          onSubmit={handleCreateMessage}
          isSubmitting={createMutation.isPending}
          mode="create"
          error={createMutation.error}
        />

        <MessageModal
          open={isEditModalOpen}
          onOpenChange={setIsEditModalOpen}
          onSubmit={handleEditMessage}
          initialData={selectedMessage || undefined}
          isSubmitting={updateMutation.isPending}
          mode="edit"
          error={updateMutation.error}
        />

        <DeleteConfirmDialog
          open={isDeleteDialogOpen}
          onOpenChange={setIsDeleteDialogOpen}
          onConfirm={handleDeleteConfirm}
          message={selectedMessage}
          isDeleting={deleteMutation.isPending}
          error={deleteMutation.error}
        />
      </div>
    </main>
  );
}
