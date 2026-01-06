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
    },
  });

  const updateMutation = useUpdateMessage({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['/api/messages'] });
        setIsEditModalOpen(false);
        setSelectedMessage(null);
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
    setSelectedMessage(message);
    setIsEditModalOpen(true);
  };

  const handleDeleteClick = (message: MessageResponse) => {
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
            <Button onClick={() => setIsCreateModalOpen(true)}>
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
        />

        <MessageModal
          open={isEditModalOpen}
          onOpenChange={setIsEditModalOpen}
          onSubmit={handleEditMessage}
          initialData={selectedMessage || undefined}
          isSubmitting={updateMutation.isPending}
          mode="edit"
        />

        <DeleteConfirmDialog
          open={isDeleteDialogOpen}
          onOpenChange={setIsDeleteDialogOpen}
          onConfirm={handleDeleteConfirm}
          message={selectedMessage}
          isDeleting={deleteMutation.isPending}
        />
      </div>
    </main>
  );
}